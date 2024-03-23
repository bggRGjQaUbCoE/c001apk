package com.example.c001apk.ui.carousel

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
import com.example.c001apk.logic.model.TopicBean
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

@HiltViewModel(assistedFactory = CarouselViewModel.Factory::class)
class CarouselViewModel @AssistedInject constructor(
    @Assisted("url") val url: String,
    @Assisted("title") val title: String,
    val repository: BlackListRepo,
    private val historyRepo: HistoryFavoriteRepo,
    private val networkRepo: NetworkRepo
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("url") url: String,
            @Assisted("title") title: String
        ): CarouselViewModel
    }

    var isRefreshing: Boolean = false
    var isLoadMore: Boolean = false
    var isEnd: Boolean = false
    var lastVisibleItemPosition: Int = 0
    var lastItem: String? = null
    var page = 1
    var listSize: Int = -1
    var isInit: Boolean = true

    val loadingState = MutableLiveData<LoadingState>()
    val footerState = MutableLiveData<FooterState>()
    val carouselData = MutableLiveData<List<HomeFeedResponse.Data>>()
    val toastText = MutableLiveData<Event<String>>()
    var topicList: List<TopicBean>? = null
    var pageTitle: String? = null
    var tmpList: ArrayList<HomeFeedResponse.Data>? = null


    fun initCarouselList() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getDataList(url, title, null, lastItem, page)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (!response.message.isNullOrEmpty()) {
                            loadingState.postValue(LoadingState.LoadingError(response.message))
                            return@collect
                        } else if (!response.data.isNullOrEmpty()) {
                            val isIconTabLinkGridCard =
                                response.data.find { it.entityTemplate == "iconTabLinkGridCard" }
                            if (isIconTabLinkGridCard != null) {
                                topicList = isIconTabLinkGridCard.entities?.map {
                                    TopicBean(it.url, it.title)
                                }
                            } else {
                                page++
                                tmpList = ArrayList()
                                lastItem = response.data.last().id
                                response.data.forEach {
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
                                            tmpList?.add(it)
                                }
                            }

                            pageTitle = response.data.last().extraDataArr?.pageTitle
                            loadingState.postValue(LoadingState.LoadingDone)
                        } else if (response.data?.isEmpty() == true) {
                            loadingState.postValue(LoadingState.LoadingFailed(LOADING_EMPTY))
                        }
                    } else {
                        loadingState.postValue(LoadingState.LoadingFailed(LOADING_FAILED))
                    }
                }
        }
    }

    fun loadCarouselList() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getDataList(url, title, "", lastItem, page)
                .onStart {
                    if (isLoadMore)
                        footerState.postValue(FooterState.Loading)
                }
                .collect { result ->
                    val topicDataList = carouselData.value?.toMutableList() ?: ArrayList()
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
                                topicDataList.clear()
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
                                            topicDataList.add(it)
                                }
                            }
                            page++
                            if (listSize <= 0) {
                                loadingState.postValue(LoadingState.LoadingDone)
                            } else
                                footerState.postValue(FooterState.LoadingDone)
                            carouselData.postValue(topicDataList)
                        } else if (data.data?.isEmpty() == true) {
                            isEnd = true
                            if (listSize <= 0)
                                loadingState.postValue(LoadingState.LoadingFailed(LOADING_EMPTY))
                            else {
                                if (isRefreshing)
                                    carouselData.postValue(emptyList())
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
                    historyRepo.saveHistory(
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
            val currentList = carouselData.value?.toMutableList() ?: ArrayList()
            currentList.removeAt(position)
            carouselData.postValue(currentList)
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
                            val updateList = carouselData.value?.toMutableList() ?: ArrayList()
                            updateList.removeAt(position)
                            carouselData.postValue(updateList)
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
                            val currentList = carouselData.value?.toMutableList() ?: ArrayList()
                            currentList[position].likenum = count
                            currentList[position].userAction?.like = isLike
                            carouselData.postValue(currentList)
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