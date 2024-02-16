package com.example.c001apk.ui.activity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.adapter.FooterAdapter
import com.example.c001apk.adapter.ItemListener
import com.example.c001apk.constant.Constants.LOADING_FAILED
import com.example.c001apk.logic.model.MessageResponse
import com.example.c001apk.logic.network.Repository.getMessage
import com.example.c001apk.util.BlackListUtil
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class MessageViewModel : ViewModel() {

    var url: String? = null
    var listSize: Int = -1
    var type: String? = null
    var isInit: Boolean = true
    var listType: String = "lastupdate_desc"
    var page = 1
    var lastItem: String? = null
    var isRefreshing: Boolean = true
    var isLoadMore: Boolean = false
    var isEnd: Boolean = false
    var lastVisibleItemPosition: Int = 0
    var itemCount = 1
    var uid: String? = null
    var avatar: String? = null
    var device: String? = null
    var replyCount: String? = null
    var dateLine: Long? = null
    var feedType: String? = null
    var errorMessage: String? = null
    var id: String? = null
    val changeState = MutableLiveData<Pair<FooterAdapter.LoadState, String?>>()
    val messageListData = MutableLiveData<List<MessageResponse.Data>>()

    fun fetchMessage() {
        viewModelScope.launch {
            getMessage(url.toString(), page)
                .onStart {
                    if (isLoadMore)
                        changeState.postValue(Pair(FooterAdapter.LoadState.LOADING, null))
                }
                .collect { result ->
                    val messageList = messageListData.value?.toMutableList() ?: ArrayList()
                    val feed = result.getOrNull()
                    if (feed != null) {
                        if (!feed.message.isNullOrEmpty()) {
                            changeState.postValue(
                                Pair(
                                    FooterAdapter.LoadState.LOADING_ERROR, feed.message
                                )
                            )
                            return@collect
                        } else if (!feed.data.isNullOrEmpty()) {
                            if (isRefreshing) messageList.clear()
                            if (isRefreshing || isLoadMore) {
                                for (element in feed.data)
                                    if (element.entityType == "feed"
                                        || element.entityType == "feed_reply"
                                        || element.entityType == "notification"
                                    )
                                        if (!BlackListUtil.checkUid(element.uid))
                                            messageList.add(element)
                            }
                            changeState.postValue(
                                Pair(
                                    FooterAdapter.LoadState.LOADING_COMPLETE, null
                                )
                            )
                        } else if (feed.data?.isEmpty() == true) {
                            if (isRefreshing) messageList.clear()
                            changeState.postValue(Pair(FooterAdapter.LoadState.LOADING_END, null))
                            isEnd = true
                        }
                    } else {
                        changeState.postValue(
                            Pair(
                                FooterAdapter.LoadState.LOADING_ERROR, LOADING_FAILED
                            )
                        )
                        isEnd = true
                        result.exceptionOrNull()?.printStackTrace()
                    }
                    messageListData.postValue(messageList)
                }
        }

    }

    inner class ItemClickListener : ItemListener

}