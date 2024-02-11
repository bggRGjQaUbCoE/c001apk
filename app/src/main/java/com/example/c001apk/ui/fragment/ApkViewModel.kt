package com.example.c001apk.ui.fragment

import android.util.Log
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
import com.example.c001apk.logic.network.Repository.postLikeFeed
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.TopicBlackListUtil
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class ApkViewModel : ViewModel() {

    var isInit: Boolean = true
    var type: String? = null
    var appCommentTitle = "最近回复"
    var appCommentSort: String? = null
    var appId: String? = null
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
    var firstVisibleItemPosition = 0
    var id: String? = null

    val changeState = MutableLiveData<Pair<FooterAdapter.LoadState, String?>>()
    val appCommentData = MutableLiveData<List<HomeFeedResponse.Data>>()
    val toastText = MutableLiveData<Event<String>>()

    private val commentBaseUrl = "/page?url=/feed/apkCommentList?id="
    fun fetchAppComment() {
        viewModelScope.launch {
            getDataList(
                commentBaseUrl + appId + appCommentSort, appCommentTitle, null, lastItem, page
            )
                .onStart {
                    changeState.postValue(Pair(FooterAdapter.LoadState.LOADING, null))
                }
                .collect { result ->
                    val appCommentList = appCommentData.value?.toMutableList() ?: ArrayList()
                    val comment = result.getOrNull()
                    if (!comment?.data.isNullOrEmpty()) {
                        if (isRefreshing)
                            appCommentList.clear()
                        if (isRefreshing || isLoadMore) {
                            comment?.data!!.forEach {
                                if (it.entityType == "feed")
                                    if (!BlackListUtil.checkUid(
                                            it.userInfo?.uid.toString()
                                        ) && !TopicBlackListUtil.checkTopic(
                                            it.tags + it.ttitle
                                        )
                                    )
                                        appCommentList.add(it)
                            }
                        }
                        changeState.postValue(Pair(FooterAdapter.LoadState.LOADING_COMPLETE, null))
                    } else if (comment?.data?.isEmpty() == true) {
                        if (isRefreshing) appCommentList.clear()
                        changeState.postValue(Pair(FooterAdapter.LoadState.LOADING_END, null))
                        isEnd = true
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
                    appCommentData.postValue(appCommentList)
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
                            val updateList = appCommentData.value!!.toMutableList()
                            updateList.removeAt(position)
                            appCommentData.postValue(updateList)
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

    inner class ItemClickListener : ItemListener {
        override fun onLikeClick(type: String, id: String, position: Int, likeData: Like) {
            if (PrefManager.isLogin) {
                if (PrefManager.SZLMID.isEmpty())
                    toastText.postValue(Event(Constants.SZLM_ID))
                else onPostLikeFeed(id, position, likeData)
            }
        }

        override fun onBlockUser(uid: String, position: Int) {
            super.onBlockUser(uid, position)
            val currentList = appCommentData.value!!.toMutableList()
            currentList.removeAt(position)
            appCommentData.postValue(currentList)
        }

        override fun onDeleteClicked(entityType:String, id: String, position: Int) {
            onDeleteFeed("/v6/feed/deleteFeed", id, position)
        }
    }

     fun onPostLikeFeed(id: String, position: Int, likeData: Like) {
        val likeType = if (likeData.isLike.get() == 1) "unlike" else "like"
        val likeUrl = "/v6/feed/$likeType"
        viewModelScope.launch {
            postLikeFeed(likeUrl, id)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.data != null) {
                            val count = response.data.count
                            val isLike = if (likeData.isLike.get() == 1) 0 else 1
                            likeData.likeNum.set(count)
                            likeData.isLike.set(isLike)
                            val currentList = appCommentData.value!!.toMutableList()
                            currentList[position].likenum = count
                            currentList[position].userAction?.like = isLike
                            appCommentData.postValue(currentList)
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