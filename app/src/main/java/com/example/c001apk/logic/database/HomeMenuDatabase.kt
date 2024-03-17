package com.example.c001apk.logic.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.c001apk.logic.dao.HomeMenuDao
import com.example.c001apk.logic.model.HomeMenu

@Database(version = 4, entities = [HomeMenu::class])
abstract class HomeMenuDatabase : RoomDatabase() {
    abstract fun homeMenuDao(): HomeMenuDao
}