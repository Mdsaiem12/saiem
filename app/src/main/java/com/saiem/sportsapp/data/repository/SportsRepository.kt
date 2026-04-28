package com.saiem.sportsapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.saiem.sportsapp.data.local.dao.ChannelDao
import com.saiem.sportsapp.data.local.dao.MatchDao
import com.saiem.sportsapp.data.local.entity.ChannelEntity
import com.saiem.sportsapp.data.local.entity.MatchEntity
import com.saiem.sportsapp.data.model.*
import com.saiem.sportsapp.data.remote.api.*
import com.saiem.sportsapp.data.remote.scraper.ScoreScraper
import com.saiem.sportsapp.data.remote.scraper.StreamScraper
import com.saiem.sportsapp.utils.ApiKeyManager
import com.saiem.sportsapp.utils.MatchMapper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SportsRepository – single source of truth for all data.
 *
 * Strategy:
 *  1. Emit cached Room data immediately (offline-first)
 *  2. Fetch from primary API (football-data.org / cricapi)
 *  3. On API failure, fall back to ScoreScraper (Jsoup)
 *  4. Stream sources: Firebase Firestore → StreamApiService → StreamScraper
 */
@Singleton
class SportsRepository @Inject constructor(
    private val footballApi: FootballDataApiService,
    private val cricketApi: CricketApiService,
    private val streamApi: StreamApiService,
    private val firestore: FirebaseFirestore,
    private val remoteConfig: FirebaseRemoteConfig,
    private val scoreScraper: ScoreScraper,
    private val streamScraper: StreamScraper,
    private val matchDao: MatchDao,
    private val channelDao: ChannelDao,
    private val apiKeyManager: ApiKeyManager
) {

    // ─── Football ──────────────────────────────────────────────────────────────

    fun getLiveFootballMatches(): Flow<Resource<List<Match>>> = flow {
        emit(Resource.Loading())

        // 1. Cached data first
        matchDao.getLiveMatches()
            .map { entities -> entities.map(MatchMapper::fromEntity) }
            .collect { cached -> if (cached.isNotEmpty()) emit(Resource.Success(cached)) }

        // 2. Try API
        try {
            val key = apiKeyManager.getFootballApiKey()
            val competitions = listOf("PL", "CL", "PD", "SA", "BL1", "FL1")
            val allMatches = mutableListOf<Match>()

            for (comp in competitions) {
                val resp = footballApi.getMatches(comp, status = "LIVE")
                if (resp.isSuccessful) {
                    resp.body()?.matches?.map(MatchMapper::fromFDMatch)?.let { allMatches.addAll(it) }
                }
            }

            if (allMatches.isNotEmpty()) {
                matchDao.insertMatches(allMatches.map(MatchMapper::toEntity))
                emit(Resource.Success(allMatches))
                return@flow
            }
        } catch (_: Exception) { }

        // 3. Fallback: scraper
        try {
            val scraped = scoreScraper.scrapeLiveFootball()
            if (scraped.isNotEmpty()) {
                matchDao.insertMatches(scraped.map(MatchMapper::toEntity))
                emit(Resource.Success(scraped))
                return@flow
            }
        } catch (_: Exception) { }

        emit(Resource.Error("Unable to fetch live football matches"))
    }

    fun getTodayFootballMatches(): Flow<Resource<List<Match>>> = flow {
        emit(Resource.Loading())
        try {
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                .format(java.util.Date())
            val key = apiKeyManager.getFootballApiKey()
            val competitions = listOf("PL", "CL", "PD", "SA", "BL1", "FL1", "PPL", "DED", "BSA")
            val allMatches = mutableListOf<Match>()

            for (comp in competitions) {
                val resp = footballApi.getMatches(comp, status = "SCHEDULED", dateFrom = today, dateTo = today)
                if (resp.isSuccessful) {
                    resp.body()?.matches?.map(MatchMapper::fromFDMatch)?.let { allMatches.addAll(it) }
                }
            }
            emit(Resource.Success(allMatches))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load today's matches"))
        }
    }

    // ─── Cricket ──────────────────────────────────────────────────────────────

    fun getLiveCricketMatches(): Flow<Resource<List<CricketMatch>>> = flow {
        emit(Resource.Loading())
        try {
            val key = apiKeyManager.getCricketApiKey()
            val resp = cricketApi.getLiveMatches(key)
            if (resp.isSuccessful) {
                val data = resp.body()?.data?.map(MatchMapper::fromCricketData) ?: emptyList()
                emit(Resource.Success(data))
            } else {
                emit(Resource.Error("Cricket API error: ${resp.code()}"))
            }
        } catch (e: Exception) {
            // Fallback to scraper
            try {
                val scraped = scoreScraper.scrapeLiveCricket()
                emit(Resource.Success(scraped.map {
                    CricketMatch(
                        id = it.id,
                        teams = listOf(it.homeTeam.name, it.awayTeam.name),
                        status = it.status.name
                    )
                }))
            } catch (se: Exception) {
                emit(Resource.Error("Failed to load cricket: ${e.message}"))
            }
        }
    }

    // ─── Standings ────────────────────────────────────────────────────────────

    fun getStandings(competitionCode: String): Flow<Resource<StandingTable>> = flow {
        emit(Resource.Loading())
        try {
            val resp = footballApi.getStandings(competitionCode)
            if (resp.isSuccessful) {
                val table = MatchMapper.fromFDStandingResponse(resp.body(), competitionCode)
                emit(Resource.Success(table))
            } else {
                emit(Resource.Error("Standings API error ${resp.code()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load standings"))
        }
    }

    // ─── Streams ──────────────────────────────────────────────────────────────

    /**
     * Multi-source stream fetching:
     * 1. Firebase Firestore (admin-curated)
     * 2. StreamApiService (aggregator API)
     * 3. StreamScraper (web scraping)
     */
    suspend fun getStreamsForMatch(matchId: String, matchKeyword: String): List<StreamSource> {
        val sources = mutableListOf<StreamSource>()

        // 1. Firestore
        try {
            val snapshot = firestore.collection("streams")
                .whereEqualTo("matchId", matchId)
                .whereEqualTo("isActive", true)
                .get().await()
            snapshot.documents.forEach { doc ->
                sources.add(
                    StreamSource(
                        id = doc.id,
                        matchId = matchId,
                        title = doc.getString("title") ?: "Stream",
                        streamUrl = doc.getString("streamUrl") ?: return@forEach,
                        quality = StreamQuality.valueOf(
                            doc.getString("quality") ?: "HD"
                        ),
                        language = doc.getString("language") ?: "EN",
                        priority = (doc.getLong("priority") ?: 100L).toInt(),
                        referer = doc.getString("referer") ?: ""
                    )
                )
            }
        } catch (_: Exception) { }

        // 2. Stream API
        if (sources.size < 3) {
            try {
                val resp = streamApi.getMatchStreams(matchId)
                if (resp.isSuccessful) {
                    resp.body()?.streams?.forEach { s ->
                        sources.add(
                            StreamSource(
                                id = s.id,
                                matchId = matchId,
                                title = s.title,
                                streamUrl = s.url,
                                quality = StreamQuality.valueOf(s.quality),
                                language = s.language,
                                referer = s.referer,
                                priority = s.priority
                            )
                        )
                    }
                }
            } catch (_: Exception) { }
        }

        // 3. Scraper fallback
        if (sources.isEmpty()) {
            try {
                val scraped = streamScraper.scrapeStreamsForMatch(matchKeyword)
                sources.addAll(scraped)
            } catch (_: Exception) { }
        }

        return sources.sortedByDescending { it.priority }
    }

    // ─── TV Channels ──────────────────────────────────────────────────────────

    fun getTvChannels(): Flow<Resource<List<TvChannel>>> = flow {
        emit(Resource.Loading())

        // Cached first
        channelDao.getAllChannels()
            .map { entities -> entities.map(MatchMapper::channelFromEntity) }
            .collect { cached -> if (cached.isNotEmpty()) emit(Resource.Success(cached)) }

        // Firebase Firestore channels
        try {
            val snapshot = firestore.collection("tv_channels")
                .whereEqualTo("isActive", true)
                .get().await()
            val channels = snapshot.documents.mapNotNull { doc ->
                TvChannel(
                    id = doc.id,
                    name = doc.getString("name") ?: return@mapNotNull null,
                    logo = doc.getString("logo") ?: "",
                    streamUrl = doc.getString("streamUrl") ?: return@mapNotNull null,
                    category = ChannelCategory.valueOf(doc.getString("category") ?: "SPORTS"),
                    country = doc.getString("country") ?: "",
                    quality = StreamQuality.valueOf(doc.getString("quality") ?: "HD")
                )
            }
            if (channels.isNotEmpty()) {
                channelDao.insertChannels(channels.map(MatchMapper::channelToEntity))
                emit(Resource.Success(channels))
            }
        } catch (e: Exception) {
            // Try stream API
            try {
                val resp = streamApi.getTvChannels()
                if (resp.isSuccessful) {
                    val apiChannels = resp.body()?.channels?.map { c ->
                        TvChannel(
                            id = c.id,
                            name = c.name,
                            logo = c.logo,
                            streamUrl = c.streamUrl,
                            category = runCatching { ChannelCategory.valueOf(c.category) }
                                .getOrDefault(ChannelCategory.SPORTS),
                            country = c.country
                        )
                    } ?: emptyList()
                    emit(Resource.Success(apiChannels))
                }
            } catch (se: Exception) {
                emit(Resource.Error("Failed to load channels"))
            }
        }
    }
}
