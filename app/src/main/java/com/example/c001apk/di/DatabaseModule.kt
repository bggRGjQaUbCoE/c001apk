package com.example.c001apk.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.c001apk.logic.dao.HistoryFavoriteDao
import com.example.c001apk.logic.dao.HomeMenuDao
import com.example.c001apk.logic.dao.RecentAtUserDao
import com.example.c001apk.logic.dao.StringEntityDao
import com.example.c001apk.logic.database.BrowseHistoryDatabase
import com.example.c001apk.logic.database.FeedFavoriteDatabase
import com.example.c001apk.logic.database.HomeMenuDatabase
import com.example.c001apk.logic.database.RecentAtUserDatabase
import com.example.c001apk.logic.database.RecentEmojiDatabase
import com.example.c001apk.logic.database.SearchHistoryDatabase
import com.example.c001apk.logic.database.TopicBlackListDatabase
import com.example.c001apk.logic.database.UserBlackListDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UserBlackList

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TopicBlackList

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SearchHistory

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RecentEmoji

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BrowseHistory

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class FeedFavorite

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @RecentEmoji
    @Singleton
    @Provides
    fun provideRecentEmojiDao(stringEntityDatabase: RecentEmojiDatabase): StringEntityDao {
        return stringEntityDatabase.recentEmojiDao()
    }

    @Singleton
    @Provides
    fun provideRecentEmojiDatabase(@ApplicationContext context: Context): RecentEmojiDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            RecentEmojiDatabase::class.java, "recent_emoji.db"
        )
            .addMigrations(StringEntityDatabase_MIGRATION_1_2)
            .build()
    }

    @UserBlackList
    @Singleton
    @Provides
    fun provideUserBlackListDao(stringEntityDatabase: UserBlackListDatabase): StringEntityDao {
        return stringEntityDatabase.userBlackListDao()
    }

    @Singleton
    @Provides
    fun provideUserBlackListDatabase(@ApplicationContext context: Context): UserBlackListDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            UserBlackListDatabase::class.java, "user_blacklist.db"
        )
            .addMigrations(StringEntityDatabase_MIGRATION_1_2)
            .build()
    }

    @TopicBlackList
    @Singleton
    @Provides
    fun provideTopicBlackListDao(stringEntityDatabase: TopicBlackListDatabase): StringEntityDao {
        return stringEntityDatabase.topicBlackListDao()
    }

    @Singleton
    @Provides
    fun provideTopicBlackListDatabase(@ApplicationContext context: Context): TopicBlackListDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            TopicBlackListDatabase::class.java, "topic_blacklist.db"
        )
            .addMigrations(StringEntityDatabase_MIGRATION_1_2)
            .build()
    }

    @SearchHistory
    @Singleton
    @Provides
    fun provideSearchHistoryDao(stringEntityDatabase: SearchHistoryDatabase): StringEntityDao {
        return stringEntityDatabase.searchHistoryDao()
    }

    @Singleton
    @Provides
    fun provideSearchHistoryDatabase(@ApplicationContext context: Context): SearchHistoryDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            SearchHistoryDatabase::class.java, "search_history.db"
        )
            .addMigrations(StringEntityDatabase_MIGRATION_1_2)
            .build()
    }

    @BrowseHistory
    @Singleton
    @Provides
    fun provideBrowseHistoryDao(browseHistoryDatabase: BrowseHistoryDatabase): HistoryFavoriteDao {
        return browseHistoryDatabase.browseHistoryDao()
    }

    @Singleton
    @Provides
    fun provideBrowseHistoryDatabase(@ApplicationContext context: Context): BrowseHistoryDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            BrowseHistoryDatabase::class.java, "browse_history.db"
        ).build()
    }

    @FeedFavorite
    @Singleton
    @Provides
    fun provideFeedFavoriteDao(feedFavoriteDatabase: FeedFavoriteDatabase): HistoryFavoriteDao {
        return feedFavoriteDatabase.feedFavoriteDao()
    }

    @Singleton
    @Provides
    fun provideFeedFavoriteDatabase(@ApplicationContext context: Context): FeedFavoriteDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            FeedFavoriteDatabase::class.java, "feed_favorite.db"
        )
            .addMigrations(FeedFavoriteDatabase_MIGRATION_1_2)
            .build()
    }

    @Singleton
    @Provides
    fun provideHomeMenuDao(homeMenuDatabase: HomeMenuDatabase): HomeMenuDao {
        return homeMenuDatabase.homeMenuDao()
    }

    @Singleton
    @Provides
    fun provideHomeMenuDatabase(@ApplicationContext context: Context): HomeMenuDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            HomeMenuDatabase::class.java, "home_menu.db"
        )
            .addMigrations(HomeMenuDatabase_MIGRATION_1_2)
            .addMigrations(HomeMenuDatabase_MIGRATION_2_3)
            .addMigrations(HomeMenuDatabase_MIGRATION_3_4)
            .addMigrations(HomeMenuDatabase_MIGRATION_4_5)
            .build()
    }

    @Singleton
    @Provides
    fun provideRecentAtUserDao(recentAtUserDatabase: RecentAtUserDatabase): RecentAtUserDao {
        return recentAtUserDatabase.recentAtUserDao()
    }

    @Singleton
    @Provides
    fun provideRecentAtUserDatabase(@ApplicationContext context: Context): RecentAtUserDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            RecentAtUserDatabase::class.java, "recent_at_user.db"
        )
            .addMigrations(RecentAtUserDatabase_MIGRATION_1_2)
            .build()
    }

}

object FeedFavoriteDatabase_MIGRATION_1_2 : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE FeedFavorite_new (uid text not null, uname TEXT not null, feedId TEXT not null, avatar TEXT not null, id INTEGER not null, message TEXT not null, device TEXT not null, pubDate TEXT not null, PRIMARY KEY(id))")
        db.execSQL("DROP TABLE FeedFavorite")
        db.execSQL("ALTER TABLE FeedFavorite_new RENAME TO FeedFavorite")
    }
}

object HomeMenuDatabase_MIGRATION_1_2 : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("insert into HomeMenu (title,isEnable) values ('数码',1)")
    }
}

object HomeMenuDatabase_MIGRATION_2_3 : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE HomeMenu_new (position integer not null, title TEXT not null, isEnable integer not null, PRIMARY KEY(position))")
        db.execSQL("DROP TABLE HomeMenu")
        db.execSQL("ALTER TABLE HomeMenu_new RENAME TO HomeMenu")
    }
}

object HomeMenuDatabase_MIGRATION_3_4 : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("insert into HomeMenu (position,title,isEnable) values (6,'酷图',1)")
    }
}

object HomeMenuDatabase_MIGRATION_4_5 : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `HomeMenu_new` (`position` INTEGER NOT NULL, `title` TEXT NOT NULL, `isEnable` INTEGER NOT NULL, PRIMARY KEY(`title`))")
        db.execSQL("INSERT INTO HomeMenu_new (position, title, isEnable) SELECT position, title, isEnable FROM HomeMenu")
        db.execSQL("DROP TABLE HomeMenu")
        db.execSQL("ALTER TABLE HomeMenu_new RENAME TO HomeMenu")
    }
}

object RecentAtUserDatabase_MIGRATION_1_2 : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE RecentAtUser")
        db.execSQL("CREATE TABLE `RecentAtUser` (`id` INTEGER NOT NULL, `group` TEXT NOT NULL, `avatar` TEXT NOT NULL, `username` TEXT NOT NULL, PRIMARY KEY(`username`))")
    }
}

object StringEntityDatabase_MIGRATION_1_2 : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE `StringEntity_new` (`id` INTEGER NOT NULL, `data` TEXT NOT NULL, PRIMARY KEY(`data`))")
        db.execSQL("INSERT INTO StringEntity_new (id, data) SELECT id, data FROM StringEntity")
        db.execSQL("DROP TABLE StringEntity")
        db.execSQL("ALTER TABLE StringEntity_new RENAME TO StringEntity")
    }
}

