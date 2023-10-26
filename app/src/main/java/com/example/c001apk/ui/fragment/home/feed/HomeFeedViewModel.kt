package com.example.c001apk.ui.fragment.home.feed

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.network.Repository

class HomeFeedViewModel : ViewModel() {

    var isEnd = false
    val homeFeedList = ArrayList<HomeFeedResponse.Data>()

    var isRefreshing = true
    var isLoadMore = false

    var page = 1
    var firstLaunch = 1
    var installTime = ""
    var lastItem = ""

    private val getHomeFeedData = MutableLiveData<String>()

    val homeFeedData = getHomeFeedData.switchMap {
        Repository.getHomeFeed(page, firstLaunch, installTime, lastItem)
    }

    fun getHomeFeed() {
        getHomeFeedData.value = getHomeFeedData.value
    }
}