package com.example.c001apk.ui.user

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.adapter.Event
import com.example.c001apk.adapter.FooterAdapter
import com.example.c001apk.adapter.ItemListener
import com.example.c001apk.constant.Constants
import com.example.c001apk.constant.Constants.LOADING_FAILED
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.model.Like
import com.example.c001apk.logic.model.UserProfileResponse
import com.example.c001apk.logic.network.Repository
import com.example.c001apk.logic.network.Repository.getUserFeed
import com.example.c001apk.logic.network.Repository.getUserSpace
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.TopicBlackListUtil
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    var url: String? = null
    var isInit: Boolean = true
    var uid: String? = null
    var errorMessage: String? = null
    var uname: String? = null
    var lastVisibleItemPosition: Int = 0
    var isRefreshing: Boolean = true
    var isLoadMore: Boolean = false
    var isEnd: Boolean = false
    var page = 1
    var listSize: Int = -1
    var followType: Boolean = false
    var avatar: String? = null
    var cover: String? = null
    var level: String? = null
    var like: String? = null
    var follow: String? = null
    var fans: String? = null

    val showError = MutableLiveData<Event<Boolean>>()
    val showUser = MutableLiveData<Event<Boolean>>()
    val changeState = MutableLiveData<Pair<FooterAdapter.LoadState, String?>>()
    val feedData = MutableLiveData<List<HomeFeedResponse.Data>>()
    var userData: UserProfileResponse.Data? = null
    var afterFollow = MutableLiveData<Event<Boolean>>()

    fun fetchUser() {
        viewModelScope.launch {
            getUserSpace(uid.toString())
                .collect { result ->
                    val user = result.getOrNull()
                    if (user?.message != null) {
                        errorMessage = user.message
                        showError.postValue(Event(true))
                        return@collect
                    } else if (user?.data != null) {
                        uid = user.data.uid
                        followType = user.data.isFollow == 1
                        uname = user.data.username
                        userData = user.data
                        isRefreshing = true
                        showUser.postValue(Event(true))
                        fetchUserFeed()
                    } else {
                        uid = null
                        showError.postValue(Event(false))
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    fun fetchUserFeed() {
        viewModelScope.launch {
            getUserFeed(uid.toString(), page)
                .onStart {
                    if (isLoadMore)
                        changeState.postValue(Pair(FooterAdapter.LoadState.LOADING, null))
                }
                .collect { result ->
                    val feedList = feedData.value?.toMutableList() ?: ArrayList()
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
                            if (isRefreshing) feedList.clear()
                            if (isRefreshing || isLoadMore) {
                                feed.data.forEach {
                                    if (it.entityType == "feed")
                                        if (!BlackListUtil.checkUid(it.userInfo?.uid.toString())
                                            && !TopicBlackListUtil.checkTopic(
                                                it.tags + it.ttitle
                                            )
                                        )
                                            feedList.add(it)
                                }
                            }
                            changeState.postValue(
                                Pair(
                                    FooterAdapter.LoadState.LOADING_COMPLETE, null
                                )
                            )
                        } else if (feed.data?.isEmpty() == true) {
                            if (isRefreshing) feedList.clear()
                            changeState.postValue(Pair(FooterAdapter.LoadState.LOADING_END, null))
                            isEnd = true
                        }
                    } else {
                        changeState.postValue(
                            Pair(
                                FooterAdapter.LoadState.LOADING_ERROR,
                                LOADING_FAILED
                            )
                        )
                        isEnd = true
                        result.exceptionOrNull()?.printStackTrace()
                    }
                    feedData.postValue(feedList)
                }
        }
    }

    fun onPostFollowUnFollow() {
        viewModelScope.launch {
            Repository.postFollowUnFollow(url.toString(), uid.toString())
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        followType = !followType
                        afterFollow.postValue(Event(true))
                    } else {
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }


    val toastText = MutableLiveData<Event<String>>()

    inner class ItemClickListener : ItemListener {
        override fun onLikeClick(type: String, id: String, position: Int, likeData: Like) {
            if (PrefManager.isLogin) {
                if (PrefManager.SZLMID.isEmpty())
                    toastText.postValue(Event(Constants.SZLM_ID))
                else onPostLikeFeed(id, position, likeData)
            }
        }

        override fun onBlockUser(id: String, uid: String, position: Int) {
            super.onBlockUser(id, uid, position)
            val currentList = feedData.value!!.toMutableList()
            currentList.removeAt(position)
            feedData.postValue(currentList)
        }

        override fun onDeleteClicked(entityType: String, id: String, position: Int) {
            onDeleteFeed("/v6/feed/deleteFeed", id, position)
        }
    }

    fun onDeleteFeed(url: String, id: String, position: Int) {
        viewModelScope.launch {
            Repository.postDelete(url, id)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.data == "删除成功") {
                            toastText.postValue(Event("删除成功"))
                            val updateList = feedData.value!!.toMutableList()
                            updateList.removeAt(position)
                            feedData.postValue(updateList)
                        } else if (!response.message.isNullOrEmpty()) {
                            response.message.let {
                                toastText.postValue(Event(it))
                            }
                        }
                    } else {
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    fun onPostLikeFeed(id: String, position: Int, likeData: Like) {
        val likeType = if (likeData.isLike.get() == 1) "unlike" else "like"
        val likeUrl = "/v6/feed/$likeType"
        viewModelScope.launch {
            Repository.postLikeFeed(likeUrl, id)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.data != null) {
                            val count = response.data.count
                            val isLike = if (likeData.isLike.get() == 1) 0 else 1
                            likeData.likeNum.set(count)
                            likeData.isLike.set(isLike)
                            val currentList = feedData.value!!.toMutableList()
                            currentList[position].likenum = count
                            currentList[position].userAction?.like = isLike
                            feedData.postValue(currentList)
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

}