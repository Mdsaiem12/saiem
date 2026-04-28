package com.saiem.sportsapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.saiem.sportsapp.data.local.dao.ChannelDao
import com.saiem.sportsapp.data.local.dao.MatchDao
import com.saiem.sportsapp.data.local.entity.ChannelEntity
import com.saiem.sportsapp.data.local.entity.MatchEntity

@Database(
    entities = [MatchEntity::class, ChannelEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SportsDatabase : RoomDatabase() {
    abstract fun matchDao(): MatchDao
    abstract fun channelDao(): ChannelDao
}
