package com.saiem.sportsapp.data.model

import com.google.gson.annotations.SerializedName

data class Match(
    @SerializedName("id") val id: String = "",
    @SerializedName("homeTeam") val homeTeam: Team = Team(),
    @SerializedName("awayTeam") val awayTeam: Team = Team(),
    @SerializedName("homeScore") val homeScore: Int = 0,
    @SerializedName("awayScore") val awayScore: Int = 0,
    @SerializedName("status") val status: MatchStatus = MatchStatus.SCHEDULED,
    @SerializedName("minute") val minute: Int = 0,
    @SerializedName("tournament") val tournament: String = "",
    @SerializedName("tournamentLogo") val tournamentLogo: String = "",
    @SerializedName("startTime") val startTime: Long = 0L,
    @SerializedName("sport") val sport: SportType = SportType.FOOTBALL,
    @SerializedName("hasStreams") val hasStreams: Boolean = false,
    @SerializedName("venue") val venue: String = "",
    @SerializedName("round") val round: String = "",
    @SerializedName("lineups") val lineups: Lineups? = null,
    @SerializedName("stats") val stats: MatchStats? = null,
    @SerializedName("events") val events: List<MatchEvent> = emptyList()
)

data class Team(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("shortName") val shortName: String = "",
    @SerializedName("logo") val logo: String = "",
    @SerializedName("country") val country: String = ""
)

data class Lineups(
    @SerializedName("home") val home: List<Player> = emptyList(),
    @SerializedName("away") val away: List<Player> = emptyList(),
    @SerializedName("homeFormation") val homeFormation: String = "",
    @SerializedName("awayFormation") val awayFormation: String = ""
)

data class Player(
    @SerializedName("id") val id: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("number") val number: Int = 0,
    @SerializedName("position") val position: String = "",
    @SerializedName("photo") val photo: String = "",
    @SerializedName("isCaptain") val isCaptain: Boolean = false,
    @SerializedName("isSubstitute") val isSubstitute: Boolean = false
)

data class MatchStats(
    @SerializedName("homePossession") val homePossession: Int = 0,
    @SerializedName("awayPossession") val awayPossession: Int = 0,
    @SerializedName("homeShots") val homeShots: Int = 0,
    @SerializedName("awayShots") val awayShots: Int = 0,
    @SerializedName("homeShotsOnTarget") val homeShotsOnTarget: Int = 0,
    @SerializedName("awayShotsOnTarget") val awayShotsOnTarget: Int = 0,
    @SerializedName("homeCorners") val homeCorners: Int = 0,
    @SerializedName("awayCorners") val awayCorners: Int = 0,
    @SerializedName("homeFouls") val homeFouls: Int = 0,
    @SerializedName("awayFouls") val awayFouls: Int = 0,
    @SerializedName("homeYellowCards") val homeYellowCards: Int = 0,
    @SerializedName("awayYellowCards") val awayYellowCards: Int = 0,
    @SerializedName("homeRedCards") val homeRedCards: Int = 0,
    @SerializedName("awayRedCards") val awayRedCards: Int = 0
)

data class MatchEvent(
    @SerializedName("type") val type: EventType = EventType.GOAL,
    @SerializedName("minute") val minute: Int = 0,
    @SerializedName("playerName") val playerName: String = "",
    @SerializedName("teamId") val teamId: String = "",
    @SerializedName("detail") val detail: String = ""
)

enum class MatchStatus {
    SCHEDULED, LIVE, HALF_TIME, FINISHED, POSTPONED, CANCELLED
}

enum class SportType {
    FOOTBALL, CRICKET
}

enum class EventType {
    GOAL, YELLOW_CARD, RED_CARD, SUBSTITUTION, VAR, PENALTY
}
