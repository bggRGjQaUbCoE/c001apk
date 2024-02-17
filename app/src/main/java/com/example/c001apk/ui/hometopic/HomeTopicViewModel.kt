package com.example.c001apk.ui.hometopic

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.adapter.Event
import com.example.c001apk.logic.model.TopicBean
import com.example.c001apk.logic.network.Repository.getDataList
import com.example.c001apk.logic.network.Repository.getProductList
import kotlinx.coroutines.launch

class HomeTopicViewModel : ViewModel() {

    var title: String? = null
    var url: String? = null
    var type: String? = null
    var isInit = true
    var tabList = ArrayList<String>()
    var position: Int = 0
    val topicList: MutableList<TopicBean> = ArrayList()
    var page = 1

    val doNext = MutableLiveData<Event<Boolean>>()

    fun fetchTopicList() {
        viewModelScope.launch {
            getDataList(url.toString(), title.toString(), null, null, page)
                .collect { result ->
                    val topic = result.getOrNull()
                    if (!topic?.data.isNullOrEmpty()) {
                        if (tabList.isEmpty()) {
                            topic?.data?.get(0)?.entities?.let { entities ->
                                entities.forEach {
                                    tabList.add(it.title)
                                    topicList.add(TopicBean(it.url, it.title))
                                }
                            }
                            doNext.postValue(Event(true))
                        }
                    } else {
                        doNext.postValue(Event(false))
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    fun fetchProductList() {
        viewModelScope.launch {
            getProductList()
                .collect { result ->
                    val data = result.getOrNull()
                    if (!data?.data.isNullOrEmpty()) {
                        if (tabList.isEmpty()) {
                            data?.data?.let { entities ->
                                entities.forEach {
                                    tabList.add(it.title)
                                    topicList.add(TopicBean(it.url, it.title))
                                }
                            }
                            doNext.postValue(Event(true))
                        }
                    } else {
                        doNext.postValue(Event(false))
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

}