package com.saiem.sportsapp.utils

import com.saiem.sportsapp.data.local.entity.ChannelEntity
import com.saiem.sportsapp.data.local.entity.MatchEntity
import com.saiem.sportsapp.data.model.*
import com.saiem.sportsapp.data.remote.api.*

object MatchMapper {

    fun fromFDMatch(fdMatch: FDMatch): Match = Match(
        id = fdMatch.id.toString(),
        homeTeam = Team(
            id = fdMatch.homeTeam.id.toString(),
            name = fdMatch.homeTeam.name,
            logo = fdMatch.homeTeam.crest
        ),
        awayTeam = Team(
            id = fdMatch.awayTeam.id.toString(),
            name = fdMatch.awayTeam.name,
            logo = fdMatch.awayTeam.crest
        ),
        homeScore = fdMatch.score.fullTime.home ?: 0,
        awayScore = fdMatch.score.fullTime.away ?: 0,
        status = mapFDStatus(fdMatch.status),
        minute = fdMatch.minute?.filter { it.isDigit() }?.toIntOrNull() ?: 0,
        tournament = fdMatch.competition.name,
        tournamentLogo = fdMatch.competition.emblem,
        startTime = parseUtcDate(fdMatch.utcDate),
        sport = SportType.FOOTBALL
    )

    fun fromCricketData(data: CricketMatchData): CricketMatch = CricketMatch(
        id = data.id,
        teams = data.teams,
        score = data.score.map { s ->
            CricketScore(
                team = s.inning,
                runs = s.r,
                wickets = s.w,
                overs = s.o.toFloat()
            )
        },
        status = data.status,
        matchType = data.matchType,
        venue = data.venue,
        series = data.series_id,
        date = data.date
    )

    fun fromFDStandingResponse(resp: StandingsResponse?, leagueCode: String): StandingTable {
        val totalTable = resp?.standings?.firstOrNull { it.type == "TOTAL" }
        val rows = totalTable?.table?.map { row ->
            Standing(
                position = row.position,
                team = Team(
                    id = row.team.id.toString(),
                    name = row.team.name,
                    logo = row.team.crest
                ),
                played = row.playedGames,
                won = row.won,
                drawn = row.draw,
                lost = row.lost,
                goalsFor = row.goalsFor,
                goalsAgainst = row.goalsAgainst,
                goalDifference = row.goalDifference,
                points = row.points,
                form = row.form
            )
        } ?: emptyList()
        return StandingTable(leagueId = leagueCode, leagueName = leagueCode, standings = rows)
    }

    fun toEntity(match: Match): MatchEntity = MatchEntity(
        id = match.id,
        homeTeamName = match.homeTeam.name,
        awayTeamName = match.awayTeam.name,
        homeTeamLogo = match.homeTeam.logo,
        awayTeamLogo = match.awayTeam.logo,
        homeScore = match.homeScore,
        awayScore = match.awayScore,
        status = match.status.name,
        minute = match.minute,
        tournament = match.tournament,
        tournamentLogo = match.tournamentLogo,
        startTime = match.startTime,
        sport = match.sport.name,
        hasStreams = match.hasStreams
    )

    fun fromEntity(entity: MatchEntity): Match = Match(
        id = entity.id,
        homeTeam = Team(name = entity.homeTeamName, logo = entity.homeTeamLogo),
        awayTeam = Team(name = entity.awayTeamName, logo = entity.awayTeamLogo),
        homeScore = entity.homeScore,
        awayScore = entity.awayScore,
        status = runCatching { MatchStatus.valueOf(entity.status) }.getOrDefault(MatchStatus.SCHEDULED),
        minute = entity.minute,
        tournament = entity.tournament,
        tournamentLogo = entity.tournamentLogo,
        startTime = entity.startTime,
        sport = runCatching { SportType.valueOf(entity.sport) }.getOrDefault(SportType.FOOTBALL),
        hasStreams = entity.hasStreams
    )

    fun channelToEntity(ch: TvChannel): ChannelEntity = ChannelEntity(
        id = ch.id,
        name = ch.name,
        logo = ch.logo,
        streamUrl = ch.streamUrl,
        category = ch.category.name,
        country = ch.country,
        quality = ch.quality.name,
        isLive = ch.isLive
    )

    fun channelFromEntity(e: ChannelEntity): TvChannel = TvChannel(
        id = e.id,
        name = e.name,
        logo = e.logo,
        streamUrl = e.streamUrl,
        category = runCatching { ChannelCategory.valueOf(e.category) }.getOrDefault(ChannelCategory.SPORTS),
        country = e.country,
        quality = runCatching { StreamQuality.valueOf(e.quality) }.getOrDefault(StreamQuality.HD),
        isLive = e.isLive
    )

    private fun mapFDStatus(status: String): MatchStatus = when (status.uppercase()) {
        "LIVE", "IN_PLAY" -> MatchStatus.LIVE
        "PAUSED", "HALFTIME" -> MatchStatus.HALF_TIME
        "FINISHED", "AWARDED" -> MatchStatus.FINISHED
        "POSTPONED" -> MatchStatus.POSTPONED
        "CANCELLED" -> MatchStatus.CANCELLED
        else -> MatchStatus.SCHEDULED
    }

    private fun parseUtcDate(utcDate: String): Long = runCatching {
        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
            .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
            .parse(utcDate)?.time ?: 0L
    }.getOrDefault(0L)
}
