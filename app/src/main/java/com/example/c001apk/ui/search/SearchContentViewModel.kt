package com.example.c001apk.ui.search

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.c001apk.adapter.FooterState
import com.example.c001apk.adapter.ItemListener
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.constant.Constants
import com.example.c001apk.constant.Constants.LOADING_EMPTY
import com.example.c001apk.constant.Constants.LOADING_FAILED
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.model.Like
import com.example.c001apk.logic.repository.BlackListRepo
import com.example.c001apk.logic.repository.HistoryFavoriteRepo
import com.example.c001apk.logic.repository.NetworkRepo
import com.example.c001apk.util.Event
import com.example.c001apk.util.PrefManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class SearchContentViewModel @AssistedInject constructor(
    @Assisted("keyWord") var keyWord: String,
    @Assisted("type") var type: String,
    @Assisted("pageType") val pageType: String,
    @Assisted("pageParam") var pageParam: String,
    val repository: BlackListRepo,
    private val historyFavoriteRepo: HistoryFavoriteRepo,
    private val networkRepo: NetworkRepo
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("keyWord") keyWord: String,
            @Assisted("type") type: String,
            @Assisted("pageType") pageType: String,
            @Assisted("pageParam") pageParam: String,
        ): SearchContentViewModel
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun provideFactory(
            assistedFactory: Factory,
            keyWord: String,
            type: String,
            pageType: String,
            pageParam: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(keyWord, type, pageType, pageParam) as T
            }
        }
    }

    var feedType: String = "all"
    var sort: String = "default" //hot // reply
    var isInit: Boolean = true
    var listSize: Int = -1
    var page = 1
    var lastItem: String? = null
    var isRefreshing: Boolean = false
    var isLoadMore: Boolean = false
    var isEnd: Boolean = false
    var lastVisibleItemPosition: Int = 0

    val loadingState = MutableLiveData<LoadingState>()
    val footerState = MutableLiveData<FooterState>()
    val toastText = MutableLiveData<Event<String>>()
    var followState = MutableLiveData<Event<Int>>()
    val searchData = MutableLiveData<List<HomeFeedResponse.Data>>()

    fun fetchSearchData() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getSearch(
                type.toString(), feedType, sort, keyWord,
                pageType, pageParam, page, lastItem
            )
                .onStart {
                    if (isLoadMore)
                        footerState.postValue(FooterState.Loading)
                }
                .collect { result ->
                    val searchList = searchData.value?.toMutableList() ?: ArrayList()
                    val search = result.getOrNull()
                    if (search != null) {
                        if (!search.message.isNullOrEmpty()) {
                            if (listSize <= 0)
                                loadingState.postValue(LoadingState.LoadingError(search.message))
                            else
                                footerState.postValue(FooterState.LoadingError(search.message))
                            return@collect
                        } else if (!search.data.isNullOrEmpty()) {
                            lastItem = search.data.last().id
                            if (isRefreshing)
                                searchList.clear()
                            if (isRefreshing || isLoadMore) {
                                if (type == "feed")
                                    search.data.forEach {
                                        if (it.entityType == "feed")
                                            if (!repository.checkUid(it.userInfo?.uid.toString())
                                                && !repository.checkTopic(
                                                    it.tags + it.ttitle + it.relationRows?.getOrNull(
                                                        0
                                                    )?.title
                                                )
                                            )
                                                searchList.add(it)
                                    }
                                else
                                    searchList.addAll(search.data)
                            }
                            page++
                            if (listSize <= 0)
                                loadingState.postValue(LoadingState.LoadingDone)
                            else
                                footerState.postValue(FooterState.LoadingDone)
                            searchData.postValue(searchList)
                        } else {
                            isEnd = true
                            if (listSize <= 0)
                                loadingState.postValue(LoadingState.LoadingFailed(LOADING_EMPTY))
                            else {
                                if (isRefreshing)
                                    searchData.postValue(emptyList())
                                footerState.postValue(FooterState.LoadingEnd)
                            }
                        }
                    } else {
                        isEnd = true
                        if (listSize <= 0)
                            loadingState.postValue(LoadingState.LoadingFailed(LOADING_FAILED))
                        else
                            footerState.postValue(FooterState.LoadingError(LOADING_FAILED))
                        result.exceptionOrNull()?.printStackTrace()
                    }
                    isRefreshing = false
                    isLoadMore = false
                }
        }

    }

    fun onPostFollowUnFollow(url: String, uid: String, followAuthor: Int, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postFollowUnFollow(url, uid)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.message != null) {
                            toastText.postValue(Event(response.message))
                        } else {
                            val isFollow = if (followAuthor == 1) 0
                            else 1
                            val userList = searchData.value?.toMutableList() ?: ArrayList()
                            userList[position].isFollow = isFollow
                            searchData.postValue(userList)
                            followState.postValue(Event(position))
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

        override fun onFollowUser(uid: String, followAuthor: Int, position: Int) {
            if (PrefManager.isLogin)
                if (followAuthor == 1) {
                    onPostFollowUnFollow("/v6/user/unfollow", uid, followAuthor, position)
                } else {
                    onPostFollowUnFollow("/v6/user/follow", uid, followAuthor, position)
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
            val currentList = searchData.value?.toMutableList() ?: ArrayList()
            currentList.removeAt(position)
            searchData.postValue(currentList)
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
                            val updateList = searchData.value?.toMutableList() ?: ArrayList()
                            updateList.removeAt(position)
                            searchData.postValue(updateList)
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
                            val currentList = searchData.value?.toMutableList() ?: ArrayList()
                            currentList[position].likenum = count
                            currentList[position].userAction?.like = isLike
                            searchData.postValue(currentList)
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