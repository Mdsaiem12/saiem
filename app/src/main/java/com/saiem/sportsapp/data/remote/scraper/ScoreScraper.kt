package com.saiem.sportsapp.data.remote.scraper

import com.saiem.sportsapp.data.model.Match
import com.saiem.sportsapp.data.model.MatchStatus
import com.saiem.sportsapp.data.model.SportType
import com.saiem.sportsapp.data.model.Team
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import javax.inject.Inject

/**
 * ScoreScraper – fallback live-score scraper when API quota is exceeded.
 * Attempts to parse BBC Sport, FlashScore, and Sofascore pages.
 */
class ScoreScraper @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    companion object {
        private const val UA =
            "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120 Mobile Safari/537.36"
        private const val FLASHSCORE_FOOTBALL = "https://www.flashscore.com/football/"
        private const val FLASHSCORE_CRICKET  = "https://www.flashscore.com/cricket/"
    }

    suspend fun scrapeLiveFootball(): List<Match> = withContext(Dispatchers.IO) {
        try {
            val html = fetch(FLASHSCORE_FOOTBALL)
            parseFlashscoreMatches(html, SportType.FOOTBALL)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun scrapeLiveCricket(): List<Match> = withContext(Dispatchers.IO) {
        try {
            val html = fetch(FLASHSCORE_CRICKET)
            parseFlashscoreMatches(html, SportType.CRICKET)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun fetch(url: String): String {
        val req = Request.Builder().url(url).header("User-Agent", UA).build()
        return okHttpClient.newCall(req).execute().use { it.body?.string() ?: "" }
    }

    private fun parseFlashscoreMatches(html: String, sport: SportType): List<Match> {
        val doc = Jsoup.parse(html)
        val matches = mutableListOf<Match>()
        // FlashScore uses JS rendering – parse what static HTML gives us
        doc.select("[class*=event__match]").forEach { el ->
            try {
                val homeTeamName = el.selectFirst("[class*=event__homeParticipant]")?.text() ?: return@forEach
                val awayTeamName = el.selectFirst("[class*=event__awayParticipant]")?.text() ?: return@forEach
                val homeScore = el.selectFirst("[class*=event__score--home]")?.text()?.toIntOrNull() ?: 0
                val awayScore = el.selectFirst("[class*=event__score--away]")?.text()?.toIntOrNull() ?: 0
                val minute  = el.selectFirst("[class*=event__stage]")?.text()?.filter { it.isDigit() }?.toIntOrNull() ?: 0
                matches.add(
                    Match(
                        id = el.attr("id").ifBlank { "$homeTeamName-$awayTeamName" },
                        homeTeam = Team(name = homeTeamName),
                        awayTeam = Team(name = awayTeamName),
                        homeScore = homeScore,
                        awayScore = awayScore,
                        status = if (minute > 0) MatchStatus.LIVE else MatchStatus.SCHEDULED,
                        minute = minute,
                        sport = sport
                    )
                )
            } catch (_: Exception) { }
        }
        return matches
    }
}
