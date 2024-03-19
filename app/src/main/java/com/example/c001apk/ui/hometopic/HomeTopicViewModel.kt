package com.example.c001apk.ui.hometopic

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.logic.model.TopicBean
import com.example.c001apk.logic.repository.NetworkRepo
import com.example.c001apk.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeTopicViewModel @Inject constructor(
    private val networkRepo: NetworkRepo
): ViewModel() {

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
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getDataList(url.toString(), title.toString(), null, null, page)
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
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getProductList()
                .collect { result ->
                    val data = result.getOrNull()
                    if (!data?.data.isNullOrEmpty()) {
                        if (tabList.isEmpty()) {
                            data?.data?.let {
                                it.forEach { item ->
                                    tabList.add(item.title)
                                    topicList.add(TopicBean(item.url, item.title))
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