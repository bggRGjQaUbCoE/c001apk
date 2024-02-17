package com.example.c001apk.ui.search

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.logic.database.BlackListDatabase
import com.example.c001apk.logic.database.SearchHistoryDatabase
import com.example.c001apk.logic.database.TopicBlackListDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    var type: String? = null
    var listSize: Int = -1
    var title: String? = null
    var pageParam: String? = null
    var pageType: String? = null
    var keyWord: String? = null

    val blackListLiveData: MutableLiveData<List<String>> = MutableLiveData()
    fun getBlackList(type: String, context: Context) {
        val newList: MutableList<String> = ArrayList()
        viewModelScope.launch(Dispatchers.IO) {
            when (type) {
                "history" -> {
                    val searchHistoryDao =
                        SearchHistoryDatabase.getDatabase(context).searchHistoryDao()
                    newList.addAll(searchHistoryDao.loadAllHistory().map {
                        it.keyWord
                    })
                }

                "blacklist" -> {
                    val blackListDao = BlackListDatabase.getDatabase(context).blackListDao()
                    newList.addAll(blackListDao.loadAllList().map {
                        it.keyWord
                    })
                }

                "topicBlacklist" -> {
                    val topicBlacklist = TopicBlackListDatabase.getDatabase(context).blackListDao()
                    newList.addAll(topicBlacklist.loadAllList().map {
                        it.keyWord
                    })
                }
            }
            blackListLiveData.postValue(newList)
        }
    }

}