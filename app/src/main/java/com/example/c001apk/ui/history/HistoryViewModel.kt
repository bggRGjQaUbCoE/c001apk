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
import kotlinx.coroutines.withContext

class HistoryViewModel : ViewModel() {

    companion object {
        const val BROWSE_TYPE = "browse"
        const val FAVORITE_TYPE = "favorite"
    }

    var position: Int? = null
    var isRemove: Boolean = false
    var listSize: Int = -1
    lateinit var type: String

    val browseLiveData: MutableLiveData<List<Any>> = MutableLiveData()

    fun getBrowseList(type: String, context: Context) {
        viewModelScope.launch {
            val newList = withContext(Dispatchers.IO) {
                return@withContext if (type == BROWSE_TYPE) {
                    BrowseHistoryDatabase.getDatabase(context).browseHistoryDao()
                        .loadAllHistory()
                        .filterNot { BlackListUtil.checkUid(it.uid) }
                } else {
                    FeedFavoriteDatabase.getDatabase(context).feedFavoriteDao()
                        .loadAllHistory()
                        .filterNot { BlackListUtil.checkUid(it.uid) }
                }
            }
            browseLiveData.value = newList
        }
    }
}
