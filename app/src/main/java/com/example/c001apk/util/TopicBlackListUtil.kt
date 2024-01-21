package com.example.c001apk.util

import com.example.c001apk.MyApplication.Companion.context
import com.example.c001apk.logic.database.TopicBlackListDatabase
import com.example.c001apk.logic.model.SearchHistory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    fun checkTopic(tags: String?): Boolean {
        if (tags == null || topicBlackListDao.loadAllList().isEmpty())
            return false
        var isExist = false
        for (element in topicBlackListDao.loadAllList()) {
            if (tags.contains(element.keyWord)) {
                isExist = true
                break
            } else continue
        }
        return isExist
    }

}