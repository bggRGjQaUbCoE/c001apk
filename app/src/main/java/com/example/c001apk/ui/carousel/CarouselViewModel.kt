package com.example.c001apk.ui.carousel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.c001apk.adapter.FooterState
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.constant.Constants.LOADING_EMPTY
import com.example.c001apk.constant.Constants.LOADING_END
import com.example.c001apk.constant.Constants.LOADING_FAILED
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.model.TopicBean
import com.example.c001apk.logic.repository.BlackListRepo
import com.example.c001apk.logic.repository.HistoryFavoriteRepo
import com.example.c001apk.logic.repository.NetworkRepo
import com.example.c001apk.ui.base.BaseAppViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class CarouselViewModel @AssistedInject constructor(
    @Assisted("url") val url: String,
    @Assisted("title") val title: String,
    blackListRepo: BlackListRepo,
    historyRepo: HistoryFavoriteRepo,
    networkRepo: NetworkRepo
) : BaseAppViewModel(blackListRepo, historyRepo, networkRepo) {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("url") url: String,
            @Assisted("title") title: String
        ): CarouselViewModel
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun provideFactory(assistedFactory: Factory, url: String, title: String)
                : ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(url, title) as T
            }
        }
    }

    var isAInit: Boolean = true
    var topicList: List<TopicBean>? = null
    var pageTitle: String? = null
    private var tmpList: ArrayList<HomeFeedResponse.Data>? = null

    override fun fetchData() {
        loadCarouselList()
    }

    fun initCarouselList() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getDataList(url, title, null, lastItem, page)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (!response.message.isNullOrEmpty()) {
                            activityState.postValue(LoadingState.LoadingError(response.message))
                            return@collect
                        } else if (!response.data.isNullOrEmpty()) {
                            val isIconTabLinkGridCard =
                                response.data.find { it.entityTemplate == "iconTabLinkGridCard" }
                            if (isIconTabLinkGridCard != null) {
                                topicList = isIconTabLinkGridCard.entities?.map {
                                    TopicBean(it.url, it.title)
                                }
                            } else {
                                tmpList = ArrayList()
                                lastItem = response.data.last().id
                                response.data.forEach {
                                    if (it.entityType in listOf("feed", "topic", "product", "user"))
                                        if (!blackListRepo.checkUid(it.userInfo?.uid.toString())
                                            && !blackListRepo.checkTopic(
                                                it.tags + it.ttitle + it.relationRows?.getOrNull(0)?.title
                                            )
                                        )
                                            tmpList?.add(it)
                                }
                            }

                            pageTitle = response.data.last().extraDataArr?.pageTitle
                            activityState.postValue(LoadingState.LoadingDone)
                        } else if (response.data?.isEmpty() == true) {
                            activityState.postValue(LoadingState.LoadingFailed(LOADING_EMPTY))
                        }
                    } else {
                        activityState.postValue(LoadingState.LoadingFailed(LOADING_FAILED))
                    }
                }
        }
    }

    private fun loadCarouselList() {
        viewModelScope.launch(Dispatchers.IO) {
            tmpList?.let {
                dataList.postValue(it)
                page++
                isRefreshing = false
                loadingState.postValue(LoadingState.LoadingDone)
                tmpList = null
                return@launch
            }
            networkRepo.getDataList(url, title, "", lastItem, page)
                .onStart {
                    if (isLoadMore) {
                        if (listSize <= 0)
                            loadingState.postValue(LoadingState.Loading)
                        else
                            footerState.postValue(FooterState.Loading)
                    }
                }
                .collect { result ->
                    val topicDataList = dataList.value?.toMutableList() ?: ArrayList()
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
                                    if (it.entityType in listOf("feed", "topic", "product", "user"))
                                        if (!blackListRepo.checkUid(it.userInfo?.uid.toString())
                                            && !blackListRepo.checkTopic(
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
                            dataList.postValue(topicDataList)
                        } else if (data.data?.isEmpty() == true) {
                            isEnd = true
                            if (listSize <= 0)
                                loadingState.postValue(LoadingState.LoadingFailed(LOADING_EMPTY))
                            else {
                                if (isRefreshing)
                                    dataList.postValue(emptyList())
                                footerState.postValue(FooterState.LoadingEnd(LOADING_END))
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

}