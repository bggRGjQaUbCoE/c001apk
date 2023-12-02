package com.example.c001apk.logic.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.c001apk.logic.dao.HomeMenuDao
import com.example.c001apk.logic.model.HomeMenu

@Database(version = 2, entities = [HomeMenu::class])
abstract class HomeMenuDatabase : RoomDatabase() {
    abstract fun homeMenuDao(): HomeMenuDao

    companion object {
        private var instance: HomeMenuDatabase? = null

        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("insert into HomeMenu (title,isEnable) values ('数码',1)")
            }
        }

        @Synchronized
        fun getDatabase(context: Context): HomeMenuDatabase {
            instance?.let {
                return it
            }
            return Room.databaseBuilder(
                context.applicationContext,
                HomeMenuDatabase::class.java, "home_menu_database"
            )
                .addMigrations(MIGRATION_1_2)
                .build().apply {
                    instance = this
                }
        }
    }
}