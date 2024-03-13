package com.example.c001apk.util

import com.example.c001apk.MyApplication.Companion.context
import com.example.c001apk.logic.database.TopicBlackListDatabase
import com.example.c001apk.logic.model.SearchHistory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object TopicBlackListUtil {

    private val topicBlackListDao by lazy {
        TopicBlackListDatabase.getDatabase(context).blackListDao()
    }

    fun saveTopic(title: String) {
        CoroutineScope(Dispatchers.IO).launch {
            if (!topicBlackListDao.isExist(title)) {
                topicBlackListDao.insert(SearchHistory(title))
            }
        }
    }

    suspend fun checkTopic(tags: String?): Boolean {
        return withContext(Dispatchers.IO) {
            var isExist = false
            if (tags != null && topicBlackListDao.loadAllList().isNotEmpty())
                for (element in topicBlackListDao.loadAllList()) {
                    if (tags.contains(element.keyWord)) {
                        isExist = true
                        break
                    } else continue
                }
            isExist
        }
    }

}