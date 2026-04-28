package com.saiem.sportsapp.data.remote.api

import com.saiem.sportsapp.data.model.Match
import com.saiem.sportsapp.data.model.StandingTable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// ── Football-Data.org ──────────────────────────────────────────────────────────
interface FootballDataApiService {
    @GET("competitions/{competition}/matches")
    suspend fun getMatches(
        @Path("competition") competition: String,
        @Query("status") status: String = "LIVE",
        @Query("dateFrom") dateFrom: String? = null,
        @Query("dateTo") dateTo: String? = null
    ): Response<FootballDataResponse>

    @GET("competitions/{competition}/standings")
    suspend fun getStandings(
        @Path("competition") competition: String
    ): Response<StandingsResponse>

    @GET("matches/{matchId}")
    suspend fun getMatchDetail(
        @Path("matchId") matchId: String
    ): Response<SingleMatchResponse>
}

data class FootballDataResponse(
    val matches: List<FDMatch> = emptyList(),
    val count: Int = 0
)

data class FDMatch(
    val id: Long = 0,
    val homeTeam: FDTeam = FDTeam(),
    val awayTeam: FDTeam = FDTeam(),
    val score: FDScore = FDScore(),
    val status: String = "",
    val minute: String? = null,
    val competition: FDCompetition = FDCompetition(),
    val utcDate: String = ""
)

data class FDTeam(val id: Long = 0, val name: String = "", val crest: String = "")
data class FDCompetition(val id: Long = 0, val name: String = "", val emblem: String = "")
data class FDScore(
    val fullTime: FDGoals = FDGoals(),
    val halfTime: FDGoals = FDGoals()
)
data class FDGoals(val home: Int? = null, val away: Int? = null)
data class StandingsResponse(val standings: List<FDStanding> = emptyList())
data class FDStanding(val type: String = "", val table: List<FDTableRow> = emptyList())
data class FDTableRow(
    val position: Int = 0,
    val team: FDTeam = FDTeam(),
    val playedGames: Int = 0,
    val won: Int = 0,
    val draw: Int = 0,
    val lost: Int = 0,
    val goalsFor: Int = 0,
    val goalsAgainst: Int = 0,
    val goalDifference: Int = 0,
    val points: Int = 0,
    val form: String = ""
)
data class SingleMatchResponse(val match: FDMatch = FDMatch())
