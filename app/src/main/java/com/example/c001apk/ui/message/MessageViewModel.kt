package com.example.c001apk.ui.message

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.adapter.Event
import com.example.c001apk.adapter.FooterAdapter
import com.example.c001apk.adapter.ItemListener
import com.example.c001apk.constant.Constants.LOADING_FAILED
import com.example.c001apk.logic.model.MessageResponse
import com.example.c001apk.logic.network.Repository.checkLoginInfo
import com.example.c001apk.logic.network.Repository.getMessage
import com.example.c001apk.logic.network.Repository.getProfile
import com.example.c001apk.logic.network.Repository.postDelete
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.PrefManager
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.net.URLEncoder

class MessageViewModel : ViewModel() {

    var isInit: Boolean = true
    var type: String? = null
    var listSize: Int = -1
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


    private var url = "/v6/notification/list"
    val countList = ArrayList<String>()
    val messCountList = ArrayList<Int>()
    val changeState = MutableLiveData<Pair<FooterAdapter.LoadState, String?>>()
    val messageData = MutableLiveData<List<MessageResponse.Data>>()
    val doWhat = MutableLiveData<Event<String>>()
    val toastText = MutableLiveData<Event<String>>()

    fun fetchProfile() {
        viewModelScope.launch {
            getProfile(uid.toString())
                .collect { result ->
                    val data = result.getOrNull()
                    if (data?.data != null) {
                        countList.clear()
                        countList.apply {
                            add(data.data.feed)
                            add(data.data.follow)
                            add(data.data.fans)
                        }
                        PrefManager.username = URLEncoder.encode(data.data.username, "UTF-8")
                        PrefManager.userAvatar = data.data.userAvatar
                        PrefManager.level = data.data.level
                        PrefManager.experience = data.data.experience.toString()
                        PrefManager.nextLevelExperience = data.data.nextLevelExperience.toString()
                        doWhat.postValue(Event("showProfile"))
                        doWhat.postValue(Event("countList"))
                        //showProfile()
                        // notify

                        fetchMessage()
                    } else {
                        isEnd = true
                        isRefreshing = false
                        isLoadMore = false
                        doWhat.postValue(Event("isRefreshing"))
                        //binding.swipeRefresh.isRefreshing = false
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }


    fun fetchCheckLoginInfo() {
        viewModelScope.launch {
            checkLoginInfo()
                .collect { result ->
                    val response = result.getOrNull()
                    response?.let {
                        response.body()?.let {
                            if (response.body()?.data?.token != null) {
                                val login = response.body()?.data!!
                                messCountList.apply {
                                    add(login.notifyCount.atme)
                                    add(login.notifyCount.atcommentme)
                                    add(login.notifyCount.feedlike)
                                    add(login.notifyCount.contactsFollow)
                                    add(login.notifyCount.message)
                                }
                                doWhat.postValue(Event("messCountList"))
                                // notify
                            }
                        }
                    }
                }
        }
    }


    fun fetchMessage() {
        viewModelScope.launch {
            getMessage(url, page, lastItem)
                .onStart {
                    if (isLoadMore)
                        changeState.postValue(Pair(FooterAdapter.LoadState.LOADING, null))
                }
                .collect { result ->
                    val messageList = messageData.value?.toMutableList() ?: ArrayList()
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
                            lastItem = feed.data.last().id
                            if (isRefreshing)
                                messageList.clear()
                            if (isRefreshing || isLoadMore) {
                                for (element in feed.data)
                                    if (element.entityType == "notification")
                                        if (!BlackListUtil.checkUid(element.fromuid))
                                            messageList.add(element)
                            }
                            changeState.postValue(
                                Pair(
                                    FooterAdapter.LoadState.LOADING_COMPLETE, null
                                )
                            )
                        } else if (feed.data?.isEmpty() == true) {
                            if (isRefreshing)
                                messageList.clear()
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
                    messageData.postValue(messageList)
                }
        }
    }

    fun onPostDelete(position: Int, id: String) {
        viewModelScope.launch {
            postDelete("/v6/notification/delete", id)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.data == "删除成功") {
                            val messList = messageData.value!!.toMutableList()
                            messList.removeAt(position)
                            messageData.postValue(messList)
                            toastText.postValue(Event(response.data))
                        } else if (!response.message.isNullOrEmpty()) {
                            toastText.postValue(Event(response.message))
                        }
                    } else {
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }


    inner class ItemClickListener : ItemListener

}