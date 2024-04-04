package com.example.c001apk.ui.hometopic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.c001apk.adapter.FooterState
import com.example.c001apk.adapter.ItemListener
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.constant.Constants.LOADING_EMPTY
import com.example.c001apk.constant.Constants.LOADING_END
import com.example.c001apk.constant.Constants.LOADING_FAILED
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

class HomeTopicContentViewModel @AssistedInject constructor(
    @Assisted("url") var url: String,
    @Assisted("title") var title: String,
    blackListRepo: BlackListRepo,
    historyRepo: HistoryFavoriteRepo,
    networkRepo: NetworkRepo
) : BaseAppViewModel(blackListRepo, historyRepo, networkRepo) {

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

    override fun fetchData() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getDataList(url, title, null, lastItem, page)
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
                                    if (item.entityType in listOf("topic", "product"))
                                        topicDataList.add(item.copy(description = "home"))
                                }
                            }
                            lastItem = topicDataList.last().id
                        }
                        page++
                        if (listSize <= 0)
                            loadingState.postValue(LoadingState.LoadingDone)
                        else
                            footerState.postValue(FooterState.LoadingDone)
                        dataList.postValue(topicDataList)
                    } else if (data?.data?.isEmpty() == true) {
                        isEnd = true
                        if (listSize <= 0)
                            loadingState.postValue(LoadingState.LoadingFailed(LOADING_EMPTY))
                        else {
                            if (isRefreshing)
                                dataList.postValue(emptyList())
                            footerState.postValue(FooterState.LoadingEnd(LOADING_END))
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