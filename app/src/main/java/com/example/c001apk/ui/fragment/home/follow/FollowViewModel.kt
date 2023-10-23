package com.example.c001apk.ui.fragment.home.follow

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.network.Repository

class FollowViewModel : ViewModel() {

    var isInit = true
    val followFeedList = ArrayList<HomeFeedResponse.Data>()

    var isRefreshing = true
    var isLoadMore = false

    var page = 1
    var lastItem = ""
    val url = "V9_HOME_TAB_FOLLOW"
    val title = "关注"

    private val getFollowFeedData = MutableLiveData<String>()

    val followFeedData = getFollowFeedData.switchMap {
        Repository.getFollowFeed(url, title, page)
    }

    fun getFollowFeed() {
        getFollowFeedData.value = getFollowFeedData.value
    }
}