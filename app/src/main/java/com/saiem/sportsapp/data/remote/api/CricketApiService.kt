package com.saiem.sportsapp.data.remote.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

// ── CricAPI / SportMonks Cricket ───────────────────────────────────────────────
interface CricketApiService {

    @GET("currentMatches")
    suspend fun getLiveMatches(
        @Query("apikey") apiKey: String,
        @Query("offset") offset: Int = 0
    ): Response<CricketApiResponse>

    @GET("matches")
    suspend fun getUpcomingMatches(
        @Query("apikey") apiKey: String,
        @Query("offset") offset: Int = 0
    ): Response<CricketApiResponse>

    @GET("match_info")
    suspend fun getMatchInfo(
        @Query("apikey") apiKey: String,
        @Query("id") matchId: String
    ): Response<CricketMatchDetailResponse>
}

data class CricketApiResponse(
    val status: String = "",
    val data: List<CricketMatchData> = emptyList()
)

data class CricketMatchData(
    val id: String = "",
    val name: String = "",
    val matchType: String = "",
    val status: String = "",
    val venue: String = "",
    val date: String = "",
    val teams: List<String> = emptyList(),
    val score: List<CricketScoreData> = emptyList(),
    val series_id: String = ""
)

data class CricketScoreData(
    val r: Int = 0,
    val w: Int = 0,
    val o: Double = 0.0,
    val inning: String = ""
)

data class CricketMatchDetailResponse(
    val status: String = "",
    val data: CricketMatchDetail? = null
)

data class CricketMatchDetail(
    val id: String = "",
    val name: String = "",
    val status: String = "",
    val venue: String = "",
    val toss: String = "",
    val players: List<CricketPlayer> = emptyList(),
    val score: List<CricketScoreData> = emptyList()
)

data class CricketPlayer(
    val id: String = "",
    val name: String = "",
    val country: String = "",
    val role: String = ""
)
