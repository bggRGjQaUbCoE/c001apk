package com.example.coolapk.ui.fragment.home.feed

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.coolapk.logic.model.HomeFeedResponse
import com.example.coolapk.logic.network.Repository
import com.example.coolapk.util.TokenDeviceUtils

class HomeFeedViewModel : ViewModel() {

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