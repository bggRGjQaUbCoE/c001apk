package com.example.c001apk.ui.app

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

class AppFragmentViewModel @AssistedInject constructor(
    @Assisted("id") val id: String,
    @Assisted("appCommentSort") val appCommentSort: String,
    @Assisted("appCommentTitle") val appCommentTitle: String,
    val repository: BlackListRepo,
    private val historyFavoriteRepo: HistoryFavoriteRepo,
    private val networkRepo: NetworkRepo
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("id") id: String,
            @Assisted("appCommentSort") appCommentSort: String,
            @Assisted("appCommentTitle") appCommentTitle: String,
        ): AppFragmentViewModel
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun provideFactory(
            assistedFactory: Factory, id: String, appCommentSort: String, appCommentTitle: String,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(id, appCommentSort, appCommentTitle) as T
            }
        }
    }

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
    val appCommentData = MutableLiveData<List<HomeFeedResponse.Data>>()
    val toastText = MutableLiveData<Event<String>>()

    fun fetchAppComment(commentBaseUrl: String = "/page?url=/feed/apkCommentList?id=") {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getDataList(
                commentBaseUrl + id + appCommentSort, appCommentTitle, null, lastItem, page
            )
                .onStart {
                    if (isLoadMore)
                        footerState.postValue(FooterState.Loading)
                }
                .collect { result ->
                    val appCommentList = appCommentData.value?.toMutableList() ?: ArrayList()
                    val comment = result.getOrNull()
                    if (!comment?.message.isNullOrEmpty()) {
                        comment?.message?.let {
                            if (listSize <= 0)
                                loadingState.postValue(LoadingState.LoadingError(it))
                            else
                                footerState.postValue(FooterState.LoadingError(it))
                        }
                        return@collect
                    } else if (!comment?.data.isNullOrEmpty()) {
                        lastItem = comment?.data?.last()?.id
                        if (isRefreshing)
                            appCommentList.clear()
                        if (isRefreshing || isLoadMore) {
                            comment?.data?.let { data ->
                                data.forEach {
                                    if (it.entityType == "feed")
                                        if (!repository.checkUid(
                                                it.userInfo?.uid.toString()
                                            ) && !repository.checkTopic(
                                                it.tags + it.ttitle + it.relationRows?.getOrNull(0)?.title
                                            )
                                        )
                                            appCommentList.add(it)
                                }
                            }
                        }
                        page++
                        if (listSize <= 0)
                            loadingState.postValue(LoadingState.LoadingDone)
                        else
                            footerState.postValue(FooterState.LoadingDone)
                        appCommentData.postValue(appCommentList)
                    } else if (comment?.data?.isEmpty() == true) {
                        isEnd = true
                        if (listSize <= 0)
                            loadingState.postValue(LoadingState.LoadingError(LOADING_EMPTY))
                        else {
                            if (isRefreshing)
                                appCommentData.postValue(emptyList())
                            footerState.postValue(FooterState.LoadingEnd)
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

    fun onDeleteFeed(url: String, id: String, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postDelete(url, id)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.data == "删除成功") {
                            toastText.postValue(Event("删除成功"))
                            val updateList = appCommentData.value?.toMutableList() ?: ArrayList()
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
            val currentList = appCommentData.value?.toMutableList() ?: ArrayList()
            currentList.removeAt(position)
            appCommentData.postValue(currentList)
        }

        override fun onDeleteClicked(entityType: String, id: String, position: Int) {
            onDeleteFeed("/v6/feed/deleteFeed", id, position)
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
                            val currentList = appCommentData.value?.toMutableList() ?: ArrayList()
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