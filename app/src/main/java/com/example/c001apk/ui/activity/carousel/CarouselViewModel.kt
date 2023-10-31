package com.example.c001apk.ui.activity.carousel

import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.network.Repository

class CarouselViewModel : ViewModel() {

    var isPostLikeFeed = false
    var isPostUnLikeFeed = false

    var firstCompletelyVisibleItemPosition = -1
    var lastVisibleItemPosition = -1
    var likePosition = -1

    val tabList = ArrayList<String>()
    var fragmentList = ArrayList<Fragment>()

    var isResume = true
    var isNew = true
    var isInit = true
    var isRefreshing = true
    var isLoadMore = false
    var isEnd = false

    var barTitle = ""
    var url = ""
    var title = ""
    var page = 1

    val carouselList = ArrayList<HomeFeedResponse.Data>()

    private val getCarouselData = MutableLiveData<String>()

    val carouselData = getCarouselData.switchMap {
        Repository.getDataList(url, title, "", "", page)
    }

    fun getCarouselList() {
        getCarouselData.value = getCarouselData.value
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