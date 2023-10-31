package com.example.c001apk.ui.fragment.home.follow

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.network.Repository

class FollowViewModel : ViewModel() {

    var isInit = true
    var isNew = true
    var isPostLikeFeed = false
    var isPostUnLikeFeed = false

    var firstCompletelyVisibleItemPosition = -1
    var lastVisibleItemPosition = -1
    var likePosition = -1

    val followFeedList = ArrayList<HomeFeedResponse.Data>()

    var isRefreshing = true
    var isLoadMore = false
    var isEnd = false

    var page = 1
    var lastItem = ""
    val url = "/page?url=V9_HOME_TAB_FOLLOW"
    val title = "关注"

    private val getFollowFeedData = MutableLiveData<String>()

    val followFeedData = getFollowFeedData.switchMap {
        Repository.getDataList(url, title, "", lastItem, page)
    }

    fun getFollowFeed() {
        getFollowFeedData.value = getFollowFeedData.value
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