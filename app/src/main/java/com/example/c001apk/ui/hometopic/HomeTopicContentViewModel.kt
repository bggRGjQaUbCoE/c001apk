package com.example.c001apk.ui.hometopic

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.c001apk.adapter.FooterState
import com.example.c001apk.adapter.ItemListener
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.constant.Constants.LOADING_EMPTY
import com.example.c001apk.constant.Constants.LOADING_FAILED
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.repository.BlackListRepo
import com.example.c001apk.logic.repository.NetworkRepo
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class HomeTopicContentViewModel @AssistedInject constructor(
    @Assisted("url") var url: String,
    @Assisted("title") var title: String,
    val repository: BlackListRepo,
    private val networkRepo: NetworkRepo
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("url") url: String, @Assisted("title") title: String
        ): HomeTopicContentViewModel
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun provideFactory(
            assistedFactory: Factory, url: String, title: String,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(url, title) as T
            }
        }
    }

    var page = 1
    var isInit = true
    var listSize = -1
    var isRefreshing: Boolean = false
    var isLoadMore: Boolean = false
    var isEnd: Boolean = false
    var lastVisibleItemPosition: Int = 0
    private var lastItem: String? = null

    val loadingState = MutableLiveData<LoadingState>()
    val footerState = MutableLiveData<FooterState>()
    val topicData = MutableLiveData<List<HomeFeedResponse.Data>>()

    fun fetchTopicData() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getDataList(url, title, null, lastItem, page)
                .onStart {
                    if (isLoadMore)
                        footerState.postValue(FooterState.Loading)
                }
                .collect { result ->
                    val topicDataList = topicData.value?.toMutableList() ?: ArrayList()
                    val data = result.getOrNull()
                    if (!data?.message.isNullOrEmpty()) {
                        data?.message?.let {
                            if (listSize <= 0)
                                loadingState.postValue(LoadingState.LoadingError(it))
                            else
                                footerState.postValue(FooterState.LoadingError(it))
                        }
                        return@collect
                    } else if (!data?.data.isNullOrEmpty()) {
                        if (isRefreshing) topicDataList.clear()
                        if (isRefreshing || isLoadMore) {
                            data?.data?.let {
                                it.forEach { item ->
                                    if (item.entityType == "topic"
                                        || item.entityType == "product"
                                    )
                                        topicDataList.add(
                                            item.also { des ->
                                                des.description = "home"
                                            }
                                        )
                                }
                            }
                            lastItem = topicDataList.last().id
                        }
                        page++
                        if (listSize <= 0)
                            loadingState.postValue(LoadingState.LoadingDone)
                        else
                            footerState.postValue(FooterState.LoadingDone)
                        topicData.postValue(topicDataList)
                    } else if (data?.data?.isEmpty() == true) {
                        isEnd = true
                        if (listSize <= 0)
                            loadingState.postValue(LoadingState.LoadingFailed(LOADING_EMPTY))
                        else {
                            if (isRefreshing)
                                topicData.postValue(emptyList())
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

    inner class ItemClickListener : ItemListener

}