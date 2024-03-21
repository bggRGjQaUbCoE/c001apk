package com.example.c001apk.ui.follow

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FollowViewModel @Inject constructor(
    val repository: BlackListRepo,
    private val historyFavoriteRepo: HistoryFavoriteRepo,
    private val networkRepo: NetworkRepo
) : ViewModel() {

    var uid: String? = null
    var type: String? = null
    var isEnable: Boolean = false

    val loadingState = MutableLiveData<LoadingState>()
    val footerState = MutableLiveData<FooterState>()
    val dataListData = MutableLiveData<List<HomeFeedResponse.Data>>()

    fun fetchFeedList() {
        url = when (type) {
            "feed" -> "/v6/user/feedList?showAnonymous=0&isIncludeTop=1"
            "follow" -> "/v6/user/followList"
            "fans" -> "/v6/user/fansList"
            "apk" -> {
                uid = ""
                "/v6/user/apkFollowList"
            }

            "forum" -> {
                uid = ""
                "/v6/user/forumFollowList"
            }

            "like" -> "/v6/user/likeList"

            "reply" -> "/v6/user/replyList"

            "replyToMe" -> "/v6/user/replyToMeList"

            "recentHistory" -> "/v6/user/recentHistoryList"

            else -> throw IllegalArgumentException("invalid type: $type")
        }
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getFollowList(url.toString(), uid.toString(), page, lastItem)
                .onStart {
                    if (isLoadMore)
                        footerState.postValue(FooterState.Loading)
                }
                .collect { result ->
                    val dataList = dataListData.value?.toMutableList() ?: ArrayList()
                    val feed = result.getOrNull()
                    if (feed != null) {
                        if (!feed.message.isNullOrEmpty()) {
                            if (listSize <= 0)
                                loadingState.postValue(LoadingState.LoadingError(feed.message))
                            else
                                footerState.postValue(FooterState.LoadingError(feed.message))
                            return@collect
                        } else if (!feed.data.isNullOrEmpty()) {
                            lastItem = feed.data.last().id
                            if (isRefreshing) dataList.clear()
                            if (isRefreshing || isLoadMore) {
                                feed.data.forEach {
                                    if (it.entityType == "feed"
                                        || it.entityType == "contacts"
                                        || it.entityType == "apk"
                                        || it.entityType == "feed_reply"
                                        || it.entityType == "recentHistory"
                                    )
                                        if (!repository.checkUid(it.userInfo?.uid.toString())
                                            && !repository.checkTopic(
                                                it.tags + it.ttitle + it.relationRows?.getOrNull(0)?.title
                                            )
                                        )
                                            dataList.add(it)
                                }
                            }
                            page++
                            if (listSize <= 0)
                                loadingState.postValue(LoadingState.LoadingDone)
                            else
                                footerState.postValue(FooterState.LoadingDone)
                            dataListData.postValue(dataList)
                        } else if (feed.data?.isEmpty() == true) {
                            isEnd = true
                            if (listSize <= 0)
                                loadingState.postValue(LoadingState.LoadingFailed(LOADING_EMPTY))
                            else {
                                if (isRefreshing)
                                    dataListData.postValue(emptyList())
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

    fun fetchTopicData() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getDataList(url.toString(), title.toString(), null, lastItem, page)
                .onStart {
                    if (isLoadMore)
                        footerState.postValue(FooterState.Loading)
                }
                .collect { result ->
                    val dataList = dataListData.value?.toMutableList() ?: ArrayList()
                    val data = result.getOrNull()
                    if (data != null) {
                        if (!data.message.isNullOrEmpty()) {
                            if (listSize <= 0)
                                loadingState.postValue(LoadingState.LoadingError(data.message))
                            else
                                footerState.postValue(FooterState.LoadingError(data.message))
                            return@collect
                        } else if (!data.data.isNullOrEmpty()) {
                            lastItem = data.data.last().id
                            if (isRefreshing)
                                dataList.clear()
                            if (isRefreshing || isLoadMore) {
                                data.data.forEach {
                                    if (it.entityType == "feed"
                                        || it.entityType == "topic"
                                        || it.entityType == "product"
                                        || it.entityType == "user"
                                    )
                                        if (!repository.checkUid(it.userInfo?.uid.toString())
                                            && !repository.checkTopic(
                                                it.tags + it.ttitle + it.relationRows?.getOrNull(0)?.title
                                            )
                                        )
                                            dataList.add(it)
                                }
                            }
                            page++
                            if (listSize <= 0)
                                loadingState.postValue(LoadingState.LoadingDone)
                            else
                                footerState.postValue(FooterState.LoadingDone)
                            dataListData.postValue(dataList)
                        } else if (data.data?.isEmpty() == true) {
                            isEnd = true
                            if (listSize <= 0)
                                loadingState.postValue(LoadingState.LoadingFailed(LOADING_EMPTY))
                            else {
                                if (isRefreshing)
                                    dataListData.postValue(emptyList())
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


    val tabList = ArrayList<String>()
    var title: String? = null
    var url: String? = null
    var isInit: Boolean = true
    var listSize: Int = -1
    var listType: String = "lastupdate_desc"
    var page = 1
    var lastItem: String? = null
    var isRefreshing: Boolean = false
    var isLoadMore: Boolean = false
    var isEnd: Boolean = false
    var lastVisibleItemPosition: Int = 0

    val toastText = MutableLiveData<Event<String>>()

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
                else if (type == "feed")
                    onPostLikeFeed(id, position, likeData)
                else
                    onPostLikeReply(id, position, likeData)
            }
        }

        override fun onBlockUser(id: String, uid: String, position: Int) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.saveUid(uid)
            }
            val currentList = dataListData.value?.toMutableList() ?: ArrayList()
            currentList.removeAt(position)
            dataListData.postValue(currentList)
        }

        override fun onDeleteClicked(entityType: String, id: String, position: Int) {
            if (entityType == "feed")
                onDeleteFeed("/v6/feed/deleteFeed", id, position)
            else
                onDeleteFeed("/v6/feed/deleteReply", id, position)
        }
    }

    fun onPostLikeReply(id: String, position: Int, likeData: Like) {
        val likeType = if (likeData.isLike.get() == 1) "unLikeReply"
        else "likeReply"
        val likeUrl = "/v6/feed/$likeType"
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postLikeReply(likeUrl, id)
                .catch { err ->
                    err.message?.let {
                        toastText.postValue(Event(it))
                    }
                }
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.data != null) {
                            val count = response.data
                            val isLike = if (likeData.isLike.get() == 1) 0 else 1
                            likeData.likeNum.set(count)
                            likeData.isLike.set(isLike)
                            val currentList = dataListData.value?.toMutableList() ?: ArrayList()
                            currentList[position].likenum = count
                            currentList[position].userAction?.like = isLike
                            dataListData.postValue(currentList)
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

    fun onDeleteFeed(url: String, id: String, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postDelete(url, id)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.data == "删除成功") {
                            toastText.postValue(Event("删除成功"))
                            val updateList = dataListData.value?.toMutableList() ?: ArrayList()
                            updateList.removeAt(position)
                            dataListData.postValue(updateList)
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
                            val currentList = dataListData.value?.toMutableList() ?: ArrayList()
                            currentList[position].likenum = count
                            currentList[position].userAction?.like = isLike
                            dataListData.postValue(currentList)
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