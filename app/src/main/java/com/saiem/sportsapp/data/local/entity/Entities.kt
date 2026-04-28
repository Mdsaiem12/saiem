package com.saiem.sportsapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_matches")
data class MatchEntity(
    @PrimaryKey val id: String,
    val homeTeamName: String,
    val awayTeamName: String,
    val homeTeamLogo: String,
    val awayTeamLogo: String,
    val homeScore: Int,
    val awayScore: Int,
    val status: String,
    val minute: Int,
    val tournament: String,
    val tournamentLogo: String,
    val startTime: Long,
    val sport: String,
    val hasStreams: Boolean,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_channels")
data class ChannelEntity(
    @PrimaryKey val id: String,
    val name: String,
    val logo: String,
    val streamUrl: String,
    val category: String,
    val country: String,
    val quality: String,
    val isLive: Boolean,
    val cachedAt: Long = System.currentTimeMillis()
)
