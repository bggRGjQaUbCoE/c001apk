package com.example.c001apk.logic.repository

import androidx.lifecycle.LiveData
import com.example.c001apk.di.RecentEmoji
import com.example.c001apk.logic.dao.StringEntityDao
import com.example.c001apk.logic.model.StringEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecentEmojiRepo @Inject constructor(
    @RecentEmoji
    private val recentEmojiDao: StringEntityDao,
) {

    fun loadAllListLive(): LiveData<List<StringEntity>> {
        return recentEmojiDao.loadAllListLive()
    }

    suspend fun insertEmoji(emoji: StringEntity) {
        recentEmojiDao.insert(emoji)
    }

    suspend fun insertList(list: List<StringEntity>) {
        recentEmojiDao.insertList(list)
    }

    suspend fun saveEmoji(emoji: String) {
        if (!recentEmojiDao.isExist(emoji)) {
            recentEmojiDao.insert(StringEntity(emoji))
        }
    }

    suspend fun deleteEmoji(emoji: String) {
        recentEmojiDao.delete(emoji)
    }

    suspend fun deleteEmoji(emoji: StringEntity) {
        recentEmojiDao.delete(emoji)
    }

    suspend fun deleteAll() {
        recentEmojiDao.deleteAll()
    }

    suspend fun checkEmoji(emoji: String): Boolean {
        return recentEmojiDao.isExist(emoji)
    }

    suspend fun updateEmoji(data: String, newId: Long) {
        recentEmojiDao.updateHistory(data, newId)
    }

    suspend fun updateEmoji(oldData: String, newData: String, newId: Long) {
        recentEmojiDao.updateEmoji(oldData, newData, newId)
    }

}