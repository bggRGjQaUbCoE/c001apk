package com.example.c001apk.ui.fragment.feed

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.c001apk.logic.model.FeedContentResponse
import com.example.c001apk.logic.model.TotalReplyResponse
import com.example.c001apk.logic.network.Repository


class FeedContentViewModel : ViewModel() {

    var isNew = true
    var isPostLikeFeed = false
    var isPostUnLikeFeed = false
    var isPostLikeReply = false
    var isPostUnLikeReply = false
    var isPostReply = false

    //feed data
    var id = "" // feed id
    var uid = "" // feed user id
    var uname = "" // feed username //被回复用户name
    var rid = "" // 回复feed/reply id
    var ruid = "" // 被回复用户id
    var type = "" //feed reply

    var rPosition = -1
    var replyAndForward = "0"
    var cursorBefore = -1
    var firstCompletelyVisibleItemPosition = -1
    var lastVisibleItemPosition = -1
    var likeReplyPosition = -1

    var replyTextMap: MutableMap<String, String> = HashMap()
    val feedContentList = ArrayList<FeedContentResponse>()
    val feedReplyList = ArrayList<TotalReplyResponse.Data>()

    var isRefreshing = true
    var isLoadMore = false
    var isEnd = false

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


    //like reply
    var likeReplyId = ""
    private val postLikeReplyData = MutableLiveData<String>()
    val likeReplyData = postLikeReplyData.switchMap {
        Repository.postLikeReply(likeReplyId)
    }

    fun postLikeReply() {
        postLikeReplyData.value = postLikeReplyData.value
    }

    //unlike reply
    private val postUnLikeReplyData = MutableLiveData<String>()
    val unLikeReplyData = postUnLikeReplyData.switchMap {
        Repository.postUnLikeReply(likeReplyId)
    }

    fun postUnLikeReply() {
        postUnLikeReplyData.value = postUnLikeReplyData.value
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

    var replyData = HashMap<String, String>()

    private val postReplyLiveData = MutableLiveData<String>()

    val postReplyData = postReplyLiveData.switchMap {
        Repository.postReply(replyData, rid, type)
    }

    fun postReply() {
        postReplyLiveData.value = postReplyLiveData.value
    }


}