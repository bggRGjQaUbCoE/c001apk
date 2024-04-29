package com.example.c001apk.logic.repository

import androidx.lifecycle.LiveData
import com.example.c001apk.logic.dao.RecentAtUserDao
import com.example.c001apk.logic.model.RecentAtUser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecentAtUserRepo @Inject constructor(
    private val recentAtUserDao: RecentAtUserDao,
) {

    fun loadAllListLive(): LiveData<List<RecentAtUser>> {
        return recentAtUserDao.loadAllListLive()
    }

    suspend fun insertUser(user: RecentAtUser) {
        recentAtUserDao.insert(user)
    }

    suspend fun insertList(list: List<RecentAtUser>) {
        recentAtUserDao.insertList(list)
    }

    suspend fun deleteUser(user: String) {
        recentAtUserDao.delete(user)
    }

    suspend fun deleteUser(user: RecentAtUser) {
        recentAtUserDao.delete(user)
    }

    suspend fun deleteAll() {
        recentAtUserDao.deleteAll()
    }

    suspend fun checkUser(username: String): Boolean {
        return recentAtUserDao.isExist(username)
    }

    suspend fun updateUser(username: String, newId: Long) {
        recentAtUserDao.updateUser(username, newId)
    }

}