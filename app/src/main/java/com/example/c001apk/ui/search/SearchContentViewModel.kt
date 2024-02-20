package com.example.c001apk.ui.search

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
import com.example.c001apk.logic.network.Repository.getSearch
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.TopicBlackListUtil
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class SearchContentViewModel : ViewModel() {

    var tabList: MutableList<String>? = null
    var title: String? = null
    var feedType: String = "all"
    var sort: String = "default" //hot // reply
    var pageParam: String? = null
    var pageType: String? = null
    var keyWord: String? = null
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
    var errorMessage: String? = null
    var id: String? = null

    val toastText = MutableLiveData<Event<String>>()
    var afterFollow = MutableLiveData<Event<Int>>()
    val changeState = MutableLiveData<Pair<FooterAdapter.LoadState, String?>>()
    val searchData = MutableLiveData<List<HomeFeedResponse.Data>>()

    fun fetchSearchData() {
        viewModelScope.launch {
            getSearch(
                type.toString(), feedType, sort, keyWord.toString(),
                pageType.toString(), pageParam.toString(), page,
                -1
            )
                .onStart {
                    if (isLoadMore)
                        changeState.postValue(Pair(FooterAdapter.LoadState.LOADING, null))
                }
                .collect { result ->
                    val searchList = searchData.value?.toMutableList() ?: ArrayList()
                    val search = result.getOrNull()
                    if (search != null) {
                        if (!search.message.isNullOrEmpty()) {
                            changeState.postValue(
                                Pair(
                                    FooterAdapter.LoadState.LOADING_ERROR, search.message
                                )
                            )
                            return@collect
                        } else if (!search.data.isNullOrEmpty()) {
                            if (isRefreshing)
                                searchList.clear()
                            if (isRefreshing || isLoadMore) {
                                if (type == "feed")
                                    for (element in search.data) {
                                        if (element.entityType == "feed")
                                            if (!BlackListUtil.checkUid(element.userInfo?.uid.toString())
                                                && !TopicBlackListUtil.checkTopic(
                                                    element.tags + element.ttitle
                                                )
                                            )
                                                searchList.add(element)
                                    }
                                else
                                    searchList.addAll(search.data)
                            }
                            changeState.postValue(
                                Pair(
                                    FooterAdapter.LoadState.LOADING_COMPLETE,
                                    null
                                )
                            )
                        } else {
                            if (isRefreshing)
                                searchList.clear()
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
                    searchData.postValue(searchList)
                }
        }

    }

    fun onPostFollowUnFollow(url: String, uid: String, followAuthor: Int, position: Int) {
        viewModelScope.launch {
            Repository.postFollowUnFollow(url, uid)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        val isFollow = if (followAuthor == 1) 0
                        else 1
                        val userList = searchData.value?.toMutableList() ?: ArrayList()
                        userList[position].isFollow = isFollow
                        searchData.postValue(userList)
                        afterFollow.postValue(Event(position))
                    } else {
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    inner class ItemClickListener : ItemListener {
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
            super.onBlockUser(id, uid, position)
            val currentList = searchData.value!!.toMutableList()
            currentList.removeAt(position)
            searchData.postValue(currentList)
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
                            val updateList = searchData.value!!.toMutableList()
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
                            val currentList = searchData.value!!.toMutableList()
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