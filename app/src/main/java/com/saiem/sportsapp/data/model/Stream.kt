package com.saiem.sportsapp.data.model

import com.google.gson.annotations.SerializedName

data class StreamSource(
    val id: String = "",
    val matchId: String = "",
    val title: String = "",
    val streamUrl: String = "",
    val quality: StreamQuality = StreamQuality.AUTO,
    val language: String = "English",
    val isWorking: Boolean = true,
    val priority: Int = 0,
    val sourceType: SourceType = SourceType.M3U8,
    val referer: String = "",
    val userAgent: String = DEFAULT_UA
) {
    companion object {
        const val DEFAULT_UA =
            "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 Chrome/120 Mobile Safari/537.36"
    }
}

enum class StreamQuality { HD, SD, AUTO }
enum class SourceType { M3U8, MP4, DASH, RTMP, IFRAME }

// ─── TV Channel ───────────────────────────────────────────────────────────────
data class TvChannel(
    val id: String = "",
    val name: String = "",
    val logo: String = "",
    val streamUrl: String = "",
    val category: ChannelCategory = ChannelCategory.SPORTS,
    val country: String = "",
    val quality: StreamQuality = StreamQuality.HD,
    val isLive: Boolean = true
)

enum class ChannelCategory { SPORTS, NEWS, ENTERTAINMENT, ALL }

// ─── Standing ─────────────────────────────────────────────────────────────────
data class Standing(
    val position: Int = 0,
    val team: Team = Team(),
    val played: Int = 0,
    val won: Int = 0,
    val drawn: Int = 0,
    val lost: Int = 0,
    val goalsFor: Int = 0,
    val goalsAgainst: Int = 0,
    val goalDifference: Int = 0,
    val points: Int = 0,
    val form: String = ""
)

data class StandingTable(
    val leagueId: String = "",
    val leagueName: String = "",
    val season: String = "",
    val standings: List<Standing> = emptyList()
)

// ─── Cricket-specific ─────────────────────────────────────────────────────────
data class CricketMatch(
    val id: String = "",
    val teams: List<String> = emptyList(),
    val score: List<CricketScore> = emptyList(),
    val status: String = "",
    val matchType: String = "",
    val venue: String = "",
    val series: String = "",
    val date: String = "",
    val hasStreams: Boolean = false
)

data class CricketScore(
    val team: String = "",
    val runs: Int = 0,
    val wickets: Int = 0,
    val overs: Float = 0f,
    val isCurrentlyBatting: Boolean = false
)

// ─── Resource wrapper ─────────────────────────────────────────────────────────
sealed class Resource<T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val message: String, val data: T? = null) : Resource<T>()
    class Loading<T> : Resource<T>()
}
