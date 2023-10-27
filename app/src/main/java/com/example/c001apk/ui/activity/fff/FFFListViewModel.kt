package com.example.c001apk.ui.activity.fff

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.network.Repository

class FFFListViewModel : ViewModel() {

    var type = ""
    var uid = ""
    var page = 1
    var isRefreh = true
    var isEnd = false
    var isLoadMore = false

    val dataList = ArrayList<HomeFeedResponse.Data>()

    private val getDataListData = MutableLiveData<String>()

    val listData = getDataListData.switchMap {
        when (type) {
            "feed" -> Repository.getFeedList(uid, page)
            "follow" -> Repository.getFollowList(uid, page)
            else -> Repository.getFansList(uid, page)
        }
    }

    fun getFeedList() {
        getDataListData.value = getDataListData.value
    }

}