package com.example.c001apk.ui.feed.reply.reply2reply

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.adapter.FooterState
import com.example.c001apk.constant.Constants.LOADING_END
import com.example.c001apk.constant.Constants.LOADING_FAILED
import com.example.c001apk.logic.model.TotalReplyResponse
import com.example.c001apk.logic.repository.BlackListRepo
import com.example.c001apk.logic.repository.HistoryFavoriteRepo
import com.example.c001apk.logic.repository.NetworkRepo
import com.example.c001apk.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class Reply2ReplyBottomSheetViewModel @Inject constructor(
    private val blackListRepo: BlackListRepo,
    private val historyRepo: HistoryFavoriteRepo,
    private val networkRepo: NetworkRepo
) : ViewModel() {

    var uname: String? = null
    var ruid: String? = null
    var rid: String? = null
    var position: Int? = null
    var fuid: String? = null
    var listSize: Int = -1
    var page = 1
    var lastItem: String? = null
    var id: String? = null
    var uid: String? = null
    var isInit: Boolean = true
    var isRefreshing: Boolean = false
    var isLoadMore: Boolean = false
    var isEnd: Boolean = false

    val footerState = MutableLiveData<FooterState>()
    val totalReplyData = MutableLiveData<List<TotalReplyResponse.Data>>()
    var oriReply: ArrayList<TotalReplyResponse.Data> = ArrayList()

    fun fetchReplyTotal() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getReply2Reply(id.toString(), page, lastItem)
                .onStart {
                    if (isLoadMore)
                        footerState.postValue(FooterState.Loading)
                }
                .collect { result ->
                    val replyTotalList = totalReplyData.value?.toMutableList() ?: ArrayList()
                    val reply = result.getOrNull()
                    if (reply != null) {
                        if (reply.message != null) {
                            footerState.postValue(FooterState.LoadingError(reply.message))
                            return@collect
                        } else if (!reply.data.isNullOrEmpty()) {
                            lastItem = reply.data.last().id
                            if (!isLoadMore) {
                                replyTotalList.clear()
                                replyTotalList.addAll(oriReply)
                            }
                            listSize = replyTotalList.size
                            reply.data.forEach {
                                if (it.entityType == "feed_reply")
                                    if (!blackListRepo.checkUid(it.uid)) {
                                        it.username = generateName(it)
                                        replyTotalList.add(it)
                                    }
                            }
                            page++
                            totalReplyData.postValue(replyTotalList)
                            footerState.postValue(FooterState.LoadingDone)
                        } else if (reply.data?.isEmpty() == true) {
                            isEnd = true
                            if (replyTotalList.isEmpty())
                                totalReplyData.postValue(oriReply)
                            footerState.postValue(FooterState.LoadingEnd(LOADING_END))
                        }
                    } else {
                        isEnd = true
                        if (replyTotalList.isEmpty())
                            totalReplyData.postValue(oriReply)
                        footerState.postValue(FooterState.LoadingError(LOADING_FAILED))
                        result.exceptionOrNull()?.printStackTrace()
                    }
                    isRefreshing = false
                    isLoadMore = false
                }
        }

    }

    private fun generateName(data: TotalReplyResponse.Data): String = run {
        val replyTag =
            when (data.uid) {
                fuid -> " [楼主] "
                uid -> " [层主] "
                else -> ""
            }

        val rReplyTag =
            when (data.ruid) {
                fuid -> " [楼主] "
                uid -> " [层主] "
                else -> ""
            }

        if (data.ruid == "0")
            """<a class="feed-link-uname" href="/u/${data.uid}">${data.username}$replyTag</a>"""
        else
            """<a class="feed-link-uname" href="/u/${data.uid}">${data.username}$replyTag</a>回复<a class="feed-link-uname" href="/u/${data.rusername}">${data.rusername}$rReplyTag</a>"""
    }

    val toastText = MutableLiveData<Event<String>>()
    fun postDeleteFeedReply(url: String, id: String, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postDelete(url, id)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.data == "删除成功") {
                            toastText.postValue(Event("删除成功"))
                            val replyList =
                                totalReplyData.value?.toMutableList() ?: ArrayList()
                            replyList.removeAt(position)
                            totalReplyData.postValue(replyList)
                        } else if (!response.message.isNullOrEmpty()) {
                            toastText.postValue(Event(response.message))
                        }
                    } else {
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    fun onPostLikeReply(id: String, isLike: Int) {
        val likeType = if (isLike == 1) "unLikeReply"
        else "likeReply"
        val likeUrl = "/v6/feed/$likeType"
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postLikeReply(likeUrl, id)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.data != null) {
                            val currentList = totalReplyData.value?.map {
                                if (it.id == id) {
                                    it.copy(
                                        likenum = response.data,
                                        userAction = it.userAction?.copy(like = if (isLike == 1) 0 else 1)
                                    )
                                } else it
                            } ?: emptyList()
                            totalReplyData.postValue(currentList)
                        } else {
                            response.message?.let {
                                toastText.postValue(Event(it))
                            }
                        }
                    } else {
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    fun saveUid(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            blackListRepo.saveUid(uid)
        }
    }

    fun saveHistory(
        id: String,
        uid: String,
        username: String,
        userAvatar: String,
        deviceTitle: String,
        message: String,
        dateline: String,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            historyRepo.saveHistory(
                id,
                uid,
                username,
                userAvatar,
                deviceTitle,
                message,
                dateline,
            )
        }
    }

    fun updateReply(data: TotalReplyResponse.Data) {
        toastText.postValue(Event("回复成功"))
        val replyTotalList = totalReplyData.value?.toMutableList() ?: ArrayList()
        replyTotalList.add(
            (position ?: 0) + 1,
            data.copy(
                username = generateName(data)
            )
        )
        totalReplyData.postValue(replyTotalList)
    }

}