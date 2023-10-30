package com.example.c001apk.ui.fragment.home.feed

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.network.Repository

class HomeFeedViewModel : ViewModel() {

    var isInit = true
    var isNew = true
    var isPostLikeFeed = false
    var isPostUnLikeFeed = false

    var firstCompletelyVisibleItemPosition = -1
    var lastVisibleItemPosition = -1
    var likePosition = -1

    var isEnd = false
    val homeFeedList = ArrayList<HomeFeedResponse.Data>()

    var isRefreshing = true
    var isLoadMore = false

    var page = 1
    var firstLaunch = 1
    var installTime = ""
    var lastItem = ""

    private val getHomeFeedData = MutableLiveData<Data>()

    data class Data(
        val page: Int,
        val firstLaunch: Int,
        val installTime: String,
        val lastItem: String
    )


    val homeFeedData = getHomeFeedData.switchMap {
        Repository.getHomeFeed(page, firstLaunch, installTime, lastItem)
    }

    fun getHomeFeed(page: Int, firstLaunch: Int, installTime: String, lastItem: String) {
        getHomeFeedData.value = Data(page, firstLaunch, installTime, lastItem)


    }

    //like feed
    var likeFeedId = ""
    private val postLikeFeedData = MutableLiveData<String>()
    val likeFeedData = postLikeFeedData.switchMap {
        Repository.postLikeFeed(likeFeedId)
    }

    fun postLikeFeed() {
        postLikeFeedData.value = postLikeFeedData.value
    }

    //unlike feed
    private val postUnLikeFeedData = MutableLiveData<String>()
    val unLikeFeedData = postUnLikeFeedData.switchMap {
        Repository.postUnLikeFeed(likeFeedId)
    }

    fun postUnLikeFeed() {
        postUnLikeFeedData.value = postUnLikeFeedData.value
    }
}