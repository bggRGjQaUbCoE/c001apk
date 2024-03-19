package com.example.c001apk.ui.message

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.adapter.FooterAdapter
import com.example.c001apk.adapter.ItemListener
import com.example.c001apk.constant.Constants.LOADING_FAILED
import com.example.c001apk.logic.model.MessageResponse
import com.example.c001apk.logic.network.Repository.checkLoginInfo
import com.example.c001apk.logic.network.Repository.getMessage
import com.example.c001apk.logic.network.Repository.getProfile
import com.example.c001apk.logic.network.Repository.postDelete
import com.example.c001apk.logic.repository.BlackListRepository
import com.example.c001apk.logic.repository.HistoryFavoriteRepository
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
    private val repository: BlackListRepository,
    private val historyFavoriteRepository: HistoryFavoriteRepository
) : ViewModel() {

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
        viewModelScope.launch(Dispatchers.IO) {
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
                        PrefManager.username =
                            withContext(Dispatchers.IO) {
                                URLEncoder.encode(data.data.username, "UTF-8")
                            }
                        PrefManager.userAvatar = data.data.userAvatar
                        PrefManager.level = data.data.level
                        PrefManager.experience = data.data.experience.toString()
                        PrefManager.nextLevelExperience = data.data.nextLevelExperience.toString()
                        doWhat.postValue(Event("showProfile"))
                        doWhat.postValue(Event("countList"))

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
        viewModelScope.launch(Dispatchers.IO) {
            checkLoginInfo()
                .collect { result ->
                    val response = result.getOrNull()
                    response?.let {
                        response.body()?.let {
                            if (response.body()?.data?.token != null) {
                                response.body()?.data?.let { login ->
                                    messCountList.apply {
                                        add(login.notifyCount.atme)
                                        add(login.notifyCount.atcommentme)
                                        add(login.notifyCount.feedlike)
                                        add(login.notifyCount.contactsFollow)
                                        add(login.notifyCount.message)
                                    }
                                }
                                doWhat.postValue(Event("messCountList"))
                            }
                        }
                    }
                }
        }
    }


    fun fetchMessage() {
        viewModelScope.launch(Dispatchers.IO) {
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
                                feed.data.forEach {
                                    if (it.entityType == "notification")
                                        if (!repository.checkUid(it.fromuid))
                                            messageList.add(it)
                                }
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
        viewModelScope.launch(Dispatchers.IO) {
            postDelete("/v6/notification/delete", id)
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
            repository.saveUid(uid)
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
            historyFavoriteRepository.saveHistory(
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


    inner class ItemClickListener : ItemListener

}