package com.example.c001apk.logic.repository

import androidx.lifecycle.LiveData
import com.example.c001apk.di.BrowseHistory
import com.example.c001apk.di.FeedFavorite
import com.example.c001apk.logic.dao.HistoryFavoriteDao
import com.example.c001apk.logic.model.FeedEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryFavoriteRepo @Inject constructor(
    @BrowseHistory
    private val browseHistoryDao: HistoryFavoriteDao,
    @FeedFavorite
    private val feedFavoriteDao: HistoryFavoriteDao,
) {

    fun loadAllHistoryListLive(): LiveData<List<FeedEntity>> {
        return browseHistoryDao.loadAllListLive()
    }

    suspend fun insertHistory(history: FeedEntity) {
        browseHistoryDao.insert(history)
    }

    suspend fun checkHistory(fid: String): Boolean {
        return browseHistoryDao.isExist(fid)
    }

    suspend fun saveHistory(
        fid: String,
        uid: String,
        uname: String,
        avatar: String,
        device: String,
        message: String,
        pubDate: String
    ) {
        if (!browseHistoryDao.isExist(fid))
            browseHistoryDao.insert(
                FeedEntity(
                    fid,
                    uid,
                    uname,
                    avatar,
                    device,
                    message,
                    pubDate
                )
            )
    }

    suspend fun deleteHistory(fid: String) {
        browseHistoryDao.delete(fid)
    }

    suspend fun deleteAllHistory() {
        browseHistoryDao.deleteAll()
    }

    fun loadAllFavoriteListLive(): LiveData<List<FeedEntity>> {
        return feedFavoriteDao.loadAllListLive()
    }

    suspend fun insertFavorite(favorite: FeedEntity) {
        feedFavoriteDao.insert(favorite)
    }

    suspend fun checkFavorite(fid: String): Boolean {
        return feedFavoriteDao.isExist(fid)
    }

    suspend fun saveFavorite(
        fid: String,
        uid: String,
        uname: String,
        avatar: String,
        device: String,
        message: String,
        pubDate: String
    ) {
        if (!feedFavoriteDao.isExist(fid))
            feedFavoriteDao.insert(
                FeedEntity(
                    fid,
                    uid,
                    uname,
                    avatar,
                    device,
                    message,
                    pubDate
                )
            )
    }

    suspend fun deleteFavorite(fid: String) {
        feedFavoriteDao.delete(fid)
    }

    suspend fun deleteAllFavorite() {
        feedFavoriteDao.deleteAll()
    }

}