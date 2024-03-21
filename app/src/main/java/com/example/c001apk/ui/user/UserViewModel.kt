package com.example.c001apk.ui.user

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.adapter.FooterState
import com.example.c001apk.adapter.ItemListener
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.constant.Constants
import com.example.c001apk.constant.Constants.LOADING_FAILED
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.model.Like
import com.example.c001apk.logic.model.UserProfileResponse
import com.example.c001apk.logic.repository.BlackListRepo
import com.example.c001apk.logic.repository.HistoryFavoriteRepo
import com.example.c001apk.logic.repository.NetworkRepo
import com.example.c001apk.util.Event
import com.example.c001apk.util.PrefManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = UserViewModel.Factory::class)
class UserViewModel @AssistedInject constructor(
    @Assisted val uid: String,
    val repository: BlackListRepo,
    private val historyFavoriteRepo: HistoryFavoriteRepo,
    private val networkRepo: NetworkRepo
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(uid: String): UserViewModel
    }

    var isInit: Boolean = true

    var lastVisibleItemPosition: Int = 0
    var isRefreshing: Boolean = false
    var isLoadMore: Boolean = false
    var isEnd: Boolean = false
    var page = 1
    var listSize: Int = -1
    var lastItem: String? = null

    var userData: UserProfileResponse.Data? = null
    val feedData = MutableLiveData<List<HomeFeedResponse.Data>>()
    val loadingState = MutableLiveData<LoadingState>()
    val footerState = MutableLiveData<FooterState>()
    var followState = MutableLiveData<Int>()
    val blockState = MutableLiveData<Event<Boolean>>()
    val toastText = MutableLiveData<Event<String>>()

    fun fetchUser() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getUserSpace(uid)
                .collect { result ->
                    val user = result.getOrNull()
                    if (user?.message != null) {
                        loadingState.postValue(LoadingState.LoadingError(user.message))
                        return@collect
                    } else if (user?.data != null) {
                        userData = user.data
                        loadingState.postValue(LoadingState.LoadingDone)

                        isRefreshing = true
                        fetchUserFeed()
                    } else {
                        loadingState.postValue(LoadingState.LoadingFailed(LOADING_FAILED))
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    fun fetchUserFeed() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getUserFeed(uid, page, lastItem)
                .onStart {
                    footerState.postValue(FooterState.Loading)
                }
                .collect { result ->
                    val feedList = feedData.value?.toMutableList() ?: ArrayList()
                    val feed = result.getOrNull()
                    if (feed != null) {
                        if (!feed.message.isNullOrEmpty()) {
                            footerState.postValue(FooterState.LoadingError(feed.message))
                            return@collect
                        } else if (!feed.data.isNullOrEmpty()) {
                            lastItem = feed.data.last().id
                            if (isRefreshing) feedList.clear()
                            if (isRefreshing || isLoadMore) {
                                feed.data.forEach {
                                    if (it.entityType == "feed")
                                        if (!repository.checkUid(it.userInfo?.uid.toString())
                                            && !repository.checkTopic(
                                                it.tags + it.ttitle + it.relationRows?.getOrNull(0)?.title
                                            )
                                        )
                                            feedList.add(it)
                                }
                            }
                            page++
                            footerState.postValue(FooterState.LoadingDone)
                            feedData.postValue(feedList)
                        } else if (feed.data?.isEmpty() == true) {
                            isEnd = true
                            if (isRefreshing)
                                feedData.postValue(emptyList())
                            footerState.postValue(FooterState.LoadingEnd)
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

    fun onPostFollowUnFollow(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postFollowUnFollow(url, uid)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.message != null) {
                            toastText.postValue(Event(response.message))
                        } else {
                            userData?.isFollow = if (userData?.isFollow == 1) 0 else 1
                            followState.postValue(userData?.isFollow ?: 0)
                        }
                    } else {
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    inner class ItemClickListener : ItemListener {
        override fun onViewFeed(
            view: View,
            id: String?,
            uid: String?,
            username: String?,
            userAvatar: String?,
            deviceTitle: String?,
            message: String?,
            dateline: String?,
            rid: Any?,
            isViewReply: Any?
        ) {
            super.onViewFeed(
                view,
                id,
                uid,
                username,
                userAvatar,
                deviceTitle,
                message,
                dateline,
                rid,
                isViewReply
            )
            viewModelScope.launch(Dispatchers.IO) {
                if (!uid.isNullOrEmpty() && PrefManager.isRecordHistory)
                    historyFavoriteRepo.saveHistory(
                        id.toString(), uid.toString(), username.toString(), userAvatar.toString(),
                        deviceTitle.toString(), message.toString(), dateline.toString()
                    )
            }
        }

        override fun onLikeClick(type: String, id: String, position: Int, likeData: Like) {
            if (PrefManager.isLogin) {
                if (PrefManager.SZLMID.isEmpty())
                    toastText.postValue(Event(Constants.SZLM_ID))
                else onPostLikeFeed(id, position, likeData)
            }
        }

        override fun onBlockUser(id: String, uid: String, position: Int) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.saveUid(uid)
            }
            val currentList = feedData.value?.toMutableList() ?: ArrayList()
            currentList.removeAt(position)
            feedData.postValue(currentList)
        }

        override fun onDeleteClicked(entityType: String, id: String, position: Int) {
            onDeleteFeed("/v6/feed/deleteFeed", id, position)
        }
    }

    fun onDeleteFeed(url: String, id: String, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postDelete(url, id)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.data == "删除成功") {
                            toastText.postValue(Event("删除成功"))
                            val updateList = feedData.value?.toMutableList() ?: ArrayList()
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
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postLikeFeed(likeUrl, id)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.data != null) {
                            val count = response.data.count
                            val isLike = if (likeData.isLike.get() == 1) 0 else 1
                            likeData.likeNum.set(count)
                            likeData.isLike.set(isLike)
                            val currentList = feedData.value?.toMutableList() ?: ArrayList()
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

    fun saveUid(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveUid(uid)
        }
    }

    fun checkUid(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (repository.checkUid(uid))
                blockState.postValue(Event(true))
        }
    }

    fun deleteUid(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteUid(uid)
        }
    }

}