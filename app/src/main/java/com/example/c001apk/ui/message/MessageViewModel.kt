package com.example.c001apk.ui.message

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.adapter.FooterState
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.constant.Constants.LOADING_END
import com.example.c001apk.constant.Constants.LOADING_FAILED
import com.example.c001apk.logic.model.MessageResponse
import com.example.c001apk.logic.repository.BlackListRepo
import com.example.c001apk.logic.repository.HistoryFavoriteRepo
import com.example.c001apk.logic.repository.NetworkRepo
import com.example.c001apk.util.CookieUtil.atcommentme
import com.example.c001apk.util.CookieUtil.atme
import com.example.c001apk.util.CookieUtil.contacts_follow
import com.example.c001apk.util.CookieUtil.feedlike
import com.example.c001apk.util.Event
import com.example.c001apk.util.PrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val blackListRepo: BlackListRepo,
    private val historyRepo: HistoryFavoriteRepo,
    private val networkRepo: NetworkRepo
) : ViewModel() {

    var isInit: Boolean = true
    var listSize: Int = -1
    var page = 1
    var lastItem: String? = null
    var isRefreshing: Boolean = false
    var isLoadMore: Boolean = false
    var isEnd: Boolean = false
    var lastVisibleItemPosition: Int = 0

    var countList = MutableLiveData<List<String>>()
    var messCountList = MutableLiveData<Event<Unit>>()
    val footerState = MutableLiveData<FooterState>()
    val messageData = MutableLiveData<List<MessageResponse.Data>>()
    val toastText = MutableLiveData<Event<String>>()
    val loadingState = MutableLiveData<LoadingState>()

    fun fetchProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getProfile(PrefManager.uid)
                .collect { result ->
                    val data = result.getOrNull()
                    if (data?.data != null) {
                        countList.postValue(
                            listOf(
                                data.data.feed,
                                data.data.follow,
                                data.data.fans
                            )
                        )
                        PrefManager.username =
                            withContext(Dispatchers.IO) {
                                URLEncoder.encode(data.data.username, "UTF-8")
                            }
                        PrefManager.userAvatar = data.data.userAvatar
                        PrefManager.level = data.data.level
                        PrefManager.experience = data.data.experience.toString()
                        PrefManager.nextLevelExperience = data.data.nextLevelExperience.toString()
                        loadingState.postValue(LoadingState.LoadingDone)

                        fetchMessage()
                    } else {
                        isEnd = true
                        isRefreshing = false
                        isLoadMore = false
                        loadingState.postValue(LoadingState.LoadingFailed(""))
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }


    fun fetchCheckLoginInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.checkLoginInfo()
                .collect { result ->
                    val response = result.getOrNull()
                    response?.body()?.data?.let {
                        atme = it.notifyCount.atme
                        atcommentme = it.notifyCount.atcommentme
                        feedlike = it.notifyCount.feedlike
                        contacts_follow = it.notifyCount.contactsFollow
                        messCountList.postValue(Event(Unit))
                    }
                }
        }
    }


    fun fetchMessage(url: String = "/v6/notification/list") {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getMessage(url, page, lastItem)
                .onStart {
                    if (isLoadMore)
                        footerState.postValue(FooterState.Loading)
                }
                .collect { result ->
                    val messageList = messageData.value?.toMutableList() ?: ArrayList()
                    val feed = result.getOrNull()
                    if (feed != null) {
                        if (!feed.message.isNullOrEmpty()) {
                            footerState.postValue(FooterState.LoadingError(feed.message))
                            return@collect
                        } else if (!feed.data.isNullOrEmpty()) {
                            lastItem = feed.data.last().id
                            if (isRefreshing)
                                messageList.clear()
                            if (isRefreshing || isLoadMore) {
                                feed.data.forEach {
                                    if (it.entityType == "notification")
                                        if (!blackListRepo.checkUid(it.fromuid))
                                            messageList.add(it)
                                }
                            }
                            page++
                            messageData.postValue(messageList)
                            footerState.postValue(FooterState.LoadingDone)
                        } else if (feed.data?.isEmpty() == true) {
                            isEnd = true
                            if (isRefreshing)
                                messageData.postValue(emptyList())
                            footerState.postValue(FooterState.LoadingEnd(LOADING_END))
                        }
                    } else {
                        isEnd = true
                        footerState.postValue(FooterState.LoadingError(LOADING_FAILED))
                        result.exceptionOrNull()?.printStackTrace()
                    }
                    isRefreshing = false
                    isLoadMore = false
                }
        }
    }

    fun onPostDelete(position: Int, id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postDelete("/v6/notification/delete", id)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.data == "删除成功") {
                            val messList = messageData.value?.toMutableList() ?: ArrayList()
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

}