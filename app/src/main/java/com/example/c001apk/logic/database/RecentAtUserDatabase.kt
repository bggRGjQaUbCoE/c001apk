package com.example.c001apk.logic.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.c001apk.logic.dao.RecentAtUserDao
import com.example.c001apk.logic.model.RecentAtUser

@Database(version = 2, entities = [RecentAtUser::class])
abstract class RecentAtUserDatabase : RoomDatabase() {
    abstract fun recentAtUserDao(): RecentAtUserDao
}