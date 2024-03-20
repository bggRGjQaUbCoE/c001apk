package com.example.c001apk.logic.repository

import androidx.lifecycle.LiveData
import com.example.c001apk.di.SearchHistory
import com.example.c001apk.logic.dao.StringEntityDao
import com.example.c001apk.logic.model.StringEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchHistoryRepo @Inject constructor(
    @SearchHistory
    private val searchHistoryDao: StringEntityDao,
) {

    fun loadAllListLive(): LiveData<List<StringEntity>> {
        return searchHistoryDao.loadAllListLive()
    }

    suspend fun insertHistory(history: StringEntity) {
        searchHistoryDao.insert(history)
    }

    suspend fun insertList(list: List<StringEntity>) {
        searchHistoryDao.insertList(list)
    }

    suspend fun saveHistory(history: String) {
        if (!searchHistoryDao.isExist(history)) {
            searchHistoryDao.insert(StringEntity(history))
        }
    }

    suspend fun deleteHistory(history: String) {
        searchHistoryDao.delete(history)
    }

    suspend fun deleteAllUser() {
        searchHistoryDao.deleteAll()
    }

    suspend fun checkHistory(history: String): Boolean {
        return searchHistoryDao.isExist(history)
    }

    suspend fun updateHistory(data: String, newId: Long) {
        searchHistoryDao.updateHistory(data, newId)
    }

}