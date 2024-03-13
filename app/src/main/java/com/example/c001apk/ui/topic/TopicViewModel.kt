package com.example.c001apk.ui.topic

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.adapter.Event
import com.example.c001apk.logic.model.TopicBean
import com.example.c001apk.logic.network.Repository
import com.example.c001apk.logic.network.Repository.getProductLayout
import com.example.c001apk.logic.network.Repository.getTopicLayout
import com.example.c001apk.logic.network.Repository.postFollow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TopicViewModel : ViewModel() {

    var postFollowData: HashMap<String, String>? = null
    var tag: String? = null
    var followUrl: String? = null
    var productTitle = "最近回复"
    var subtitle: String? = null
    var isFollow: Boolean = false
    var isResume: Boolean = true
    var id: String? = null
    var title: String? = null
    var url: String? = null
    var type: String? = null
    var isInit = true
    var tabList = ArrayList<String>()
    var position: Int = 0
    val topicList: MutableList<TopicBean> = ArrayList()
    var page = 1
    var errorMessage: String? = null
    var tabSelected: Int? = null
    val doNext = MutableLiveData<Event<Boolean>>()
    val showError = MutableLiveData<Event<Boolean>>()

    fun fetchTopicLayout() {
        viewModelScope.launch(Dispatchers.IO) {
            getTopicLayout(url.toString())
                .collect { result ->
                    val data = result.getOrNull()
                    if (!data?.message.isNullOrEmpty()) {
                        errorMessage = data?.message
                        showError.postValue(Event(true))
                        return@collect
                    } else if (data?.data != null) {
                        isFollow = data.data.userAction?.follow == 1
                        if (tabList.isEmpty()) {
                            id = data.data.id
                            type = data.data.entityType
                            subtitle = data.data.intro

                            for (element in data.data.tabList) {
                                tabList.add(element.title)
                                topicList.add(TopicBean(element.url, element.title))
                            }
                            tabSelected = 0
                            for (element in data.data.tabList) {
                                if (data.data.selectedTab == element.pageName) break
                                else tabSelected = tabSelected!! + 1
                            }

                        }
                        doNext.postValue(Event(true))
                    } else {
                        doNext.postValue(Event(false))
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }


    fun fetchProductLayout() {
        viewModelScope.launch(Dispatchers.IO) {
            getProductLayout(id.toString())
                .collect { result ->
                    val data = result.getOrNull()
                    if (!data?.message.isNullOrEmpty()) {
                        errorMessage = data?.message
                        showError.postValue(Event(true))
                        return@collect
                    } else if (data?.data != null) {
                        isFollow = data.data.userAction?.follow == 1
                        if (tabList.isEmpty()) {
                            subtitle = data.data.intro

                            for (element in data.data.tabList) {
                                tabList.add(element.title)
                                topicList.add(TopicBean(element.url, element.title))
                            }
                            tabSelected = 0
                            for (element in data.data.tabList) {
                                if (data.data.selectedTab == element.pageName) break
                                else tabSelected = tabSelected!! + 1
                            }
                        }
                        doNext.postValue(Event(true))
                    } else {
                        doNext.postValue(Event(false))
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    val afterFollow = MutableLiveData<Event<Pair<Boolean, String>>>()
    fun onGetFollow() {
        viewModelScope.launch(Dispatchers.IO) {
            Repository.getFollow(followUrl.toString(), tag, null)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (!response.message.isNullOrEmpty()) {
                            if (response.message.contains("关注成功")) {
                                isFollow = !isFollow
                                afterFollow.postValue(Event(Pair(true, response.message)))
                            } else
                                afterFollow.postValue(Event(Pair(false, response.message)))
                        }
                    } else {
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    fun onPostFollow() {
        viewModelScope.launch(Dispatchers.IO) {
            postFollowData?.let {
                postFollow(it)
                    .collect { result ->
                        val response = result.getOrNull()
                        if (response != null) {
                            if (!response.message.isNullOrEmpty()) {
                                if (response.message.contains("手机吧成功")) {
                                    isFollow = !isFollow
                                    afterFollow.postValue(Event(Pair(true, response.message)))
                                } else
                                    afterFollow.postValue(Event(Pair(false, response.message)))
                            }
                        } else {
                            result.exceptionOrNull()?.printStackTrace()
                        }

                    }
            }

        }
    }

}