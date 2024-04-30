package com.example.c001apk.ui.feed.reply.attopic

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.c001apk.adapter.FooterState
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.constant.Constants.LOADING_EMPTY
import com.example.c001apk.constant.Constants.LOADING_END
import com.example.c001apk.constant.Constants.LOADING_FAILED
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.repository.NetworkRepo
import com.example.c001apk.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val networkRepo: NetworkRepo
) : BaseViewModel() {

    val dataList = MutableLiveData<List<HomeFeedResponse.Data>>()

    val footerState = MutableLiveData<FooterState>()

    var keyword: String = ""
    var type: String = "user"

    override fun fetchData() {
        if (type == "user")
            onSearchUser()
        else if (type == "topic")
            onSearchTopic()
    }

    private fun onSearchTopic() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getSearchTag(
                keyword, page, "", null, lastItem
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

    private fun onSearchUser() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getSearch(
                type, "all", "default", keyword,
                null, null, page, lastItem
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