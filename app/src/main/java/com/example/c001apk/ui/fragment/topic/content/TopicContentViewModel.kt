package com.example.c001apk.ui.fragment.topic.content

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.network.Repository

class TopicContentViewModel : ViewModel() {

    var isPostLikeFeed = false
    var isPostUnLikeFeed = false

    var firstCompletelyVisibleItemPosition = -1
    var lastVisibleItemPosition = -1
    var likePosition = -1

    var isInit = true
    var isNew = true
    val topicDataList = ArrayList<HomeFeedResponse.Data>()

    var isRefreshing = true
    var isLoadMore = false
    var isEnd = false

    var url = ""
    var title = ""
    var subTitle: String? = null
    var page = 1

    private val getTopicDataLiveData = MutableLiveData<String>()

    val topicDataLiveData = getTopicDataLiveData.switchMap {
        Repository.getTopicData(url, title, subTitle, page)
    }

    fun getTopicData() {
        getTopicDataLiveData.value = getTopicDataLiveData.value
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