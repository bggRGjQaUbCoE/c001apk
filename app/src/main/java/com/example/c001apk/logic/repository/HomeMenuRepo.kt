package com.example.c001apk.logic.repository

import androidx.lifecycle.LiveData
import com.example.c001apk.logic.dao.HomeMenuDao
import com.example.c001apk.logic.model.HomeMenu
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeMenuRepo @Inject constructor(
    private val homeMenuDao: HomeMenuDao,
) {

    fun loadAllListLive(): LiveData<List<HomeMenu>> {
        return homeMenuDao.loadAllListLive()
    }

    suspend fun insert(homeMenu: HomeMenu) {
        homeMenuDao.insert(homeMenu)
    }

    suspend fun insertList(list: List<HomeMenu>) {
        homeMenuDao.insertList(list)
    }

    suspend fun updateList(list: List<HomeMenu>) {
        homeMenuDao.updateList(list)
    }

    suspend fun deleteAll() {
        homeMenuDao.deleteAll()
    }

}