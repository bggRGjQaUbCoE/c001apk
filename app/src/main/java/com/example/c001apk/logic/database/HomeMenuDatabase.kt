package com.example.c001apk.logic.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.c001apk.logic.dao.HomeMenuDao
import com.example.c001apk.logic.model.HomeMenu

@Database(version = 1, entities = [HomeMenu::class])
abstract class HomeMenuDatabase : RoomDatabase() {
    abstract fun homeMenuDao(): HomeMenuDao

    companion object {
        private var instance: HomeMenuDatabase? = null

        @Synchronized
        fun getDatabase(context: Context): HomeMenuDatabase {
            instance?.let {
                return it
            }
            return Room.databaseBuilder(
                context.applicationContext,
                HomeMenuDatabase::class.java, "home_menu_database"
            )
                .build().apply {
                    instance = this
                }
        }
    }
}