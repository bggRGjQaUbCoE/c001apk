package com.example.c001apk.ui.fragment

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
import com.example.c001apk.logic.network.Repository
import com.example.c001apk.logic.network.Repository.getDataList
import com.example.c001apk.logic.network.Repository.getFollowList
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.TopicBlackListUtil
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class FollowViewModel : ViewModel() {

    val changeState = MutableLiveData<Pair<FooterAdapter.LoadState, String?>>()
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
        viewModelScope.launch {
            getFollowList(url.toString(), uid.toString(), page)
                .onStart {
                    if (isLoadMore)
                        changeState.postValue(Pair(FooterAdapter.LoadState.LOADING, null))
                }
                .collect { result ->
                    val dataList = dataListData.value?.toMutableList() ?: ArrayList()
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
                            if (isRefreshing) dataList.clear()
                            if (isRefreshing || isLoadMore) {
                                for (element in feed.data)
                                    if (element.entityType == "feed"
                                        || element.entityType == "contacts"
                                        || element.entityType == "apk"
                                        || element.entityType == "feed_reply"
                                        || element.entityType == "recentHistory"
                                    )
                                        if (!BlackListUtil.checkUid(element.userInfo?.uid.toString())
                                            && !TopicBlackListUtil.checkTopic(
                                                element.tags + element.ttitle
                                            )
                                        )
                                            dataList.add(element)
                            }
                            changeState.postValue(
                                Pair(
                                    FooterAdapter.LoadState.LOADING_COMPLETE, null
                                )
                            )
                        } else if (feed.data?.isEmpty() == true) {
                            if (isRefreshing) dataList.clear()
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
                    dataListData.postValue(dataList)
                }
        }
    }

    fun fetchTopicData() {
        viewModelScope.launch {
            getDataList(url.toString(), title.toString(), null, lastItem, page)
                .onStart {
                    if (isLoadMore)
                        changeState.postValue(Pair(FooterAdapter.LoadState.LOADING, null))
                }
                .collect { result ->
                    val dataList = dataListData.value?.toMutableList() ?: ArrayList()
                    val data = result.getOrNull()
                    if (data != null) {
                        if (!data.message.isNullOrEmpty()) {
                            changeState.postValue(
                                Pair(
                                    FooterAdapter.LoadState.LOADING_ERROR, data.message
                                )
                            )
                            return@collect
                        } else if (!data.data.isNullOrEmpty()) {
                            if (isRefreshing)
                                dataList.clear()
                            if (isRefreshing || isLoadMore) {
                                for (element in data.data)
                                    if (element.entityType == "feed"
                                        || element.entityType == "topic"
                                        || element.entityType == "product"
                                        || element.entityType == "user"
                                    )
                                        if (!BlackListUtil.checkUid(element.userInfo?.uid.toString())
                                            && !TopicBlackListUtil.checkTopic(
                                                element.tags + element.ttitle
                                            )
                                        )
                                            dataList.add(element)
                            }
                            changeState.postValue(
                                Pair(
                                    FooterAdapter.LoadState.LOADING_COMPLETE, null
                                )
                            )
                        } else if (data.data?.isEmpty() == true) {
                            if (isRefreshing)
                                dataList.clear()
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
                    dataListData.postValue(dataList)
                }
        }
    }


    var isEnable: Boolean? = null
    val tabList = ArrayList<String>()
    var title: String? = null
    var url: String? = null
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

    val toastText = MutableLiveData<Event<String>>()

    inner class ItemClickListener : ItemListener {
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

        override fun onBlockUser(uid: String, position: Int) {
            super.onBlockUser(uid, position)
            val currentList = dataListData.value!!.toMutableList()
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
        viewModelScope.launch {
            Repository.postLikeReply(likeUrl, id)
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
                            val currentList = dataListData.value!!.toMutableList()
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
        viewModelScope.launch {
            Repository.postDelete(url, id)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.data == "删除成功") {
                            toastText.postValue(Event("删除成功"))
                            val updateList = dataListData.value!!.toMutableList()
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
                            val currentList = dataListData.value!!.toMutableList()
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