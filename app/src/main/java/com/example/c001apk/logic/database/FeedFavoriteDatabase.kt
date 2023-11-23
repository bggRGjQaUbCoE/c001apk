package com.example.c001apk.logic.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.c001apk.logic.dao.FeedFavoriteDao
import com.example.c001apk.logic.model.FeedFavorite


@Database(version = 2, entities = [FeedFavorite::class])
abstract class FeedFavoriteDatabase : RoomDatabase() {
    abstract fun feedFavoriteDao(): FeedFavoriteDao

    companion object {
        private var instance: FeedFavoriteDatabase? = null

        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE FeedFavorite_new (uid text not null, uname TEXT not null, feedId TEXT not null, avatar TEXT not null, id INTEGER not null, message TEXT not null, device TEXT not null, pubDate TEXT not null, PRIMARY KEY(id))")
                db.execSQL("DROP TABLE FeedFavorite")
                db.execSQL("ALTER TABLE FeedFavorite_new RENAME TO FeedFavorite")
            }
        }

        @Synchronized
        fun getDatabase(context: Context): FeedFavoriteDatabase {
            instance?.let {
                return it
            }
            return Room.databaseBuilder(
                context.applicationContext,
                FeedFavoriteDatabase::class.java, "feed_favorite_database"
            )
                .addMigrations(MIGRATION_1_2)
                .build().apply {
                    instance = this
                }
        }
    }
}