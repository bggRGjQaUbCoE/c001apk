package com.example.c001apk.ui.fragment.feed.total

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.c001apk.logic.model.TotalReplyResponse
import com.example.c001apk.logic.network.Repository

class ReplyTotalViewModel : ViewModel() {

    var isNew = true
    var isPostLikeReply = false
    var isPostUnLikeReply = false
    var isPostReply = false

    var id = ""
    var uid = ""
    var position: Int = 0
    var type = ""
    var uname = ""
    var ruid = ""
    var rid = ""
    var rPosition = 0
    var r2rPosition = 0
    var replyAndForward = "0"
    var isPaste = false
    var cursorBefore = -1
    var page = 1

    var lastVisibleItemPosition = -1
    var likePosition = -1

    var replyTextMap: MutableMap<String, String> = HashMap()

    var isEnd = false
    var isLoadMore = false


    val replyTotalList = ArrayList<TotalReplyResponse.Data>()

    private val getReplyTotalLiveData = MutableLiveData<String>()

    val replyTotalLiveData = getReplyTotalLiveData.switchMap {
        Repository.getReply2Reply(id, page)
    }

    fun getReplyTotal() {
        getReplyTotalLiveData.value = getReplyTotalLiveData.value
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

    var replyData = HashMap<String, String>()

    private val postReplyLiveData = MutableLiveData<String>()

    val postReplyData = postReplyLiveData.switchMap {
        Repository.postReply(replyData, rid, type)
    }

    fun postReply() {
        postReplyLiveData.value = postReplyLiveData.value
    }

}