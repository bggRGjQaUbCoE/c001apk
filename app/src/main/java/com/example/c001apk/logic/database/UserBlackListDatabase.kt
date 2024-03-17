package com.example.c001apk.logic.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.c001apk.logic.dao.StringEntityDao
import com.example.c001apk.logic.model.StringEntity

@Database(version = 1, entities = [StringEntity::class])
abstract class UserBlackListDatabase : RoomDatabase() {
    abstract fun userBlackListDao(): StringEntityDao
}