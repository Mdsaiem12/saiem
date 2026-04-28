package com.saiem.sportsapp.data.local.dao

import androidx.room.*
import com.saiem.sportsapp.data.local.entity.MatchEntity
import com.saiem.sportsapp.data.local.entity.ChannelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Query("SELECT * FROM cached_matches WHERE sport = :sport ORDER BY startTime ASC")
    fun getMatchesBySport(sport: String): Flow<List<MatchEntity>>

    @Query("SELECT * FROM cached_matches WHERE status = 'LIVE' ORDER BY startTime ASC")
    fun getLiveMatches(): Flow<List<MatchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<MatchEntity>)

    @Query("DELETE FROM cached_matches WHERE cachedAt < :expireTime")
    suspend fun deleteExpired(expireTime: Long)

    @Query("DELETE FROM cached_matches")
    suspend fun clearAll()
}

@Dao
interface ChannelDao {
    @Query("SELECT * FROM cached_channels ORDER BY name ASC")
    fun getAllChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM cached_channels WHERE category = :category ORDER BY name ASC")
    fun getChannelsByCategory(category: String): Flow<List<ChannelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<ChannelEntity>)

    @Query("DELETE FROM cached_channels")
    suspend fun clearAll()
}
