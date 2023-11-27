package com.example.c001apk.util

import com.example.c001apk.MyApplication.Companion.context
import com.example.c001apk.logic.database.TopicBlackListDatabase

object TopicBlackListUtil {

    private val TopicBlackListDao by lazy {
        TopicBlackListDatabase.getDatabase(context).blackListDao()
    }

    fun checkTopic(tags: String?): Boolean {
        if (tags == null || TopicBlackListDao.loadAllList().isEmpty())
            return false
        var isExist = false
        for (element in TopicBlackListDao.loadAllList()) {
            if (tags.contains(element.keyWord)) {
                isExist = true
                break
            } else continue
        }
        return isExist
    }

}