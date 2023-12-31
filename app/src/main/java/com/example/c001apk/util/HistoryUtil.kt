package com.example.c001apk.util

import com.example.c001apk.MyApplication.Companion.context
import com.example.c001apk.logic.database.BrowseHistoryDatabase
import com.example.c001apk.logic.model.BrowseHistory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object HistoryUtil {

    private val browseHistoryDao by lazy {
        BrowseHistoryDatabase.getDatabase(context).browseHistoryDao()
    }

    fun saveHistory(
        fid: String,
        uid: String,
        uname: String,
        avatar: String,
        device: String,
        message: String,
        pubDate: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            if (!browseHistoryDao.isExist(fid))
                browseHistoryDao.insert(
                    BrowseHistory(
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
    }

}