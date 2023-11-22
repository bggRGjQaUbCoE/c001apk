package com.example.c001apk.logic.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.c001apk.logic.dao.FeedFavoriteDao
import com.example.c001apk.logic.model.FeedFavorite

@Database(version = 1, entities = [FeedFavorite::class])
abstract class FeedFavoriteDatabase : RoomDatabase() {
    abstract fun feedFavoriteDao(): FeedFavoriteDao

    companion object {
        private var instance: FeedFavoriteDatabase? = null

        @Synchronized
        fun getDatabase(context: Context): FeedFavoriteDatabase {
            instance?.let {
                return it
            }
            return Room.databaseBuilder(
                context.applicationContext,
                FeedFavoriteDatabase::class.java, "feed_favorite_database"
            )
                .build().apply {
                    instance = this
                }
        }
    }
}