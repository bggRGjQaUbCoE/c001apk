package com.example.c001apk.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.c001apk.adapter.FooterState
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

class SearchContentViewModel @AssistedInject constructor(
    @Assisted("keyWord") var keyWord: String,
    @Assisted("type") var type: String,
    @Assisted("pageType") val pageType: String,
    @Assisted("pageParam") var pageParam: String,
    blackListRepo: BlackListRepo,
    historyRepo: HistoryFavoriteRepo,
    networkRepo: NetworkRepo
) : BaseAppViewModel(blackListRepo, historyRepo, networkRepo) {

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

    override fun fetchData() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getSearch(
                type, feedType, sort, keyWord,
                pageType, pageParam, page, lastItem
            )
                .onStart {
                    if (isLoadMore) {
                        if (listSize <= 0)
                            loadingState.postValue(LoadingState.Loading)
                        else
                            footerState.postValue(FooterState.Loading)
                    }
                }
                .collect { result ->
                    val searchList = dataList.value?.toMutableList() ?: ArrayList()
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
                                            if (!blackListRepo.checkUid(it.userInfo?.uid.toString())
                                                && !blackListRepo.checkTopic(
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
                            dataList.postValue(searchList)
                        } else {
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