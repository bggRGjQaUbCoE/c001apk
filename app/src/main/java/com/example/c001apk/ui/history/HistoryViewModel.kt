package com.example.c001apk.ui.history

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.logic.database.BrowseHistoryDatabase
import com.example.c001apk.logic.database.FeedFavoriteDatabase
import com.example.c001apk.util.BlackListUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistoryViewModel : ViewModel() {

    var position: Int? = null
    var isRemove: Boolean = false
    var listSize: Int = -1
    lateinit var type: String

    val browseLiveData: MutableLiveData<List<Any>> = MutableLiveData()

    fun getBrowseList(type: String, context: Context) {
        val newList = ArrayList<Any>()
        viewModelScope.launch(Dispatchers.IO) {
            if (type == "browse") {
                val browseHistoryDao = BrowseHistoryDatabase.getDatabase(context).browseHistoryDao()
                val list = browseHistoryDao.loadAllHistory()
                if (list.isNotEmpty()) {
                    list.forEach {
                        if (!BlackListUtil.checkUid(it.uid))
                            newList.add(it)
                    }
                }
            } else {
                val feedFavoriteDao = FeedFavoriteDatabase.getDatabase(context).feedFavoriteDao()
                val list = feedFavoriteDao.loadAllHistory()
                if (list.isNotEmpty()) {
                    list.forEach {
                        if (!BlackListUtil.checkUid(it.uid))
                            newList.add(it)
                    }
                }
            }
            browseLiveData.postValue(newList)
        }
    }

}