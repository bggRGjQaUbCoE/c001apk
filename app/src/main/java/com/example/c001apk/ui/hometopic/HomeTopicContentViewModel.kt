package com.example.c001apk.ui.hometopic

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.adapter.FooterAdapter
import com.example.c001apk.adapter.ItemListener
import com.example.c001apk.constant.Constants.LOADING_FAILED
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.network.Repository.getDataList
import com.example.c001apk.logic.repository.BlackListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeTopicContentViewModel @Inject constructor(
    val repository: BlackListRepository
): ViewModel() {

    var page = 1
    var title: String? = null
    var url: String? = null
    var isInit = true
    var listSize = -1
    var isRefreshing: Boolean = true
    var isLoadMore: Boolean = false
    var isEnd: Boolean = false
    var lastVisibleItemPosition: Int = 0
    private var lastItem: String? = null
    val changeState = MutableLiveData<Pair<FooterAdapter.LoadState, String?>>()
    val topicData = MutableLiveData<List<HomeFeedResponse.Data>>()

    fun fetchTopicData() {
        viewModelScope.launch(Dispatchers.IO) {
            getDataList(url.toString(), title.toString(), null, lastItem, page)
                .onStart {
                    if (isLoadMore)
                        changeState.postValue(Pair(FooterAdapter.LoadState.LOADING, null))
                }
                .collect { result ->
                    val topicDataList = topicData.value?.toMutableList() ?: ArrayList()
                    val data = result.getOrNull()
                    if (!data?.message.isNullOrEmpty()) {
                        changeState.postValue(
                            Pair(FooterAdapter.LoadState.LOADING_ERROR, data?.message)
                        )
                        return@collect
                    } else if (!data?.data.isNullOrEmpty()) {
                        if (isRefreshing) topicDataList.clear()
                        if (isRefreshing || isLoadMore) {
                            for (element in data?.data!!)
                                if (element.entityType == "topic"
                                    || element.entityType == "product"
                                )
                                    topicDataList.add(
                                        element.also {
                                            it.description = "home"
                                        })
                            lastItem = topicDataList.last().id
                        }
                        changeState.postValue(Pair(FooterAdapter.LoadState.LOADING_COMPLETE, null))
                    } else if (data?.data?.isEmpty() == true) {
                        if (isRefreshing) topicDataList.clear()
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
                    topicData.postValue(topicDataList)
                }
        }
    }

    inner class ItemClickListener : ItemListener

}