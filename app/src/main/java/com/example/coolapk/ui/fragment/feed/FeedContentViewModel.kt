package com.example.coolapk.ui.fragment.feed

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.coolapk.logic.model.FeedContentResponse
import com.example.coolapk.logic.model.HomeFeedResponse
import com.example.coolapk.logic.network.Repository

class FeedContentViewModel : ViewModel() {

    val feedContentList = ArrayList<FeedContentResponse>()
    val feedReplyList = ArrayList<HomeFeedResponse.Data>()

    var isRefreshing = true
    var isLoadMore = false
    var isEnd = false

    var id = ""

    private val getFeedData = MutableLiveData<String>()

    val feedData = getFeedData.switchMap {
        Repository.getFeedContent(id)
    }

    fun getFeed() {
        getFeedData.value = getFeedData.value
    }

    var page = 1
    var discussMode = 1
    var listType = "lastupdate_desc"

    private val getFeedReplyData = MutableLiveData<String>()

    val feedReplyData = getFeedReplyData.switchMap {
        Repository.getFeedContentReply(id, discussMode, listType, page)
    }

    fun getFeedReply() {
        getFeedReplyData.value = getFeedReplyData.value
    }

}