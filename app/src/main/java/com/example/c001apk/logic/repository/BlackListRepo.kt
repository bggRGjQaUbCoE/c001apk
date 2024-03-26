package com.example.c001apk.logic.repository

import androidx.lifecycle.LiveData
import com.example.c001apk.di.TopicBlackList
import com.example.c001apk.di.UserBlackList
import com.example.c001apk.logic.dao.StringEntityDao
import com.example.c001apk.logic.model.StringEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlackListRepo @Inject constructor(
    @UserBlackList
    private val userBlackListDao: StringEntityDao,
    @TopicBlackList
    private val topicBlackListDao: StringEntityDao,
) {

    fun loadAllUserListLive(): LiveData<List<StringEntity>> {
        return userBlackListDao.loadAllListLive()
    }

    suspend fun insertUid(uid: StringEntity) {
        userBlackListDao.insert(uid)
    }

    suspend fun insertUidList(list: List<StringEntity>) {
        userBlackListDao.insertList(list)
    }

    suspend fun checkUid(uid: String): Boolean {
        return userBlackListDao.isExist(uid)
    }

    suspend fun saveUid(uid: String) {
        if (!userBlackListDao.isExist(uid)) {
            userBlackListDao.insert(StringEntity(uid))
        }
    }

    suspend fun deleteUid(uid: String) {
        userBlackListDao.delete(uid)
    }

    suspend fun deleteAllUser() {
        userBlackListDao.deleteAll()
    }

    fun loadAllTopicListLive(): LiveData<List<StringEntity>> {
        return topicBlackListDao.loadAllListLive()
    }

    suspend fun insertTopic(topic: StringEntity) {
        topicBlackListDao.insert(topic)
    }

    suspend fun insertTopicList(list: List<StringEntity>) {
        topicBlackListDao.insertList(list)
    }

    suspend fun checkTopic(topic: String): Boolean {
        return withContext(Dispatchers.IO) {
            topicBlackListDao.isContain(topic)
        }
    }

    suspend fun saveTopic(topic: String) {
        if (!topicBlackListDao.isExist(topic)) {
            topicBlackListDao.insert(StringEntity(topic))
        }
    }

    suspend fun deleteTopic(topic: String) {
        topicBlackListDao.delete(topic)
    }

    suspend fun deleteAllTopic() {
        topicBlackListDao.deleteAll()
    }

}