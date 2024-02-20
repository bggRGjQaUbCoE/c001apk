package com.example.c001apk.ui.search

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.logic.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchViewModel(private val context: Context) : ViewModel() {

    var type: String? = null
    var listSize: Int = -1
    var title: String? = null
    var pageParam: String? = null
    var pageType: String? = null
    var keyWord: String? = null

    private val searchHistoryDao by lazy { SearchHistoryDatabase.getDatabase(context).searchHistoryDao() }
    private val blackListDao by lazy { BlackListDatabase.getDatabase(context).blackListDao() }
    private val topicBlackListDao by lazy { TopicBlackListDatabase.getDatabase(context).blackListDao() }

    val blackListLiveData: MutableLiveData<List<String>> = MutableLiveData()

    fun getBlackList(blackListType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newList = when (blackListType) {
                "history" -> getListFromDao(searchHistoryDao)
                "userBlacklist" -> getListFromDao(blackListDao)
                "topicBlacklist" -> getListFromDao(topicBlackListDao)
                else -> emptyList()
            }
            blackListLiveData.postValue(newList)
        }
    }
    
    private fun getListFromDao(dao: BlackListDao): List<String> {
        return dao.loadAllList().map { it.keyWord }
    }
}
