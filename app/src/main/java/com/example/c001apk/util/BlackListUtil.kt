package com.example.c001apk.util

import com.example.c001apk.MyApplication.Companion.context
import com.example.c001apk.logic.database.BlackListDatabase
import com.example.c001apk.logic.model.SearchHistory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object BlackListUtil {

    private val blackListDao by lazy {
        BlackListDatabase.getDatabase(context).blackListDao()
    }

    fun checkUid(uid: String) = blackListDao.isExist(uid)

    fun saveUid(uid: String) {
        CoroutineScope(Dispatchers.IO).launch {
            if (!blackListDao.isExist(uid)) {
                blackListDao.insert(SearchHistory(uid))
            }
        }
    }

}