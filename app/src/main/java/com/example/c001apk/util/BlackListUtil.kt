package com.example.c001apk.util

import com.example.c001apk.MyApplication.Companion.context
import com.example.c001apk.logic.database.BlackListDatabase
import com.example.c001apk.logic.model.SearchHistory
import kotlin.concurrent.thread

object BlackListUtil {

    private val blackListDao by lazy {
        BlackListDatabase.getDatabase(context).blackListDao()
    }

    fun checkUid(uid: String) = blackListDao.isExist(uid)

    fun saveUid(uid: String) {
        thread {
            if (!blackListDao.isExist(uid)) {
                blackListDao.insert(SearchHistory(uid))
            }
        }
    }

}