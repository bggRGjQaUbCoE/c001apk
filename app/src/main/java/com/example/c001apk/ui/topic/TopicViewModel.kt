package com.example.c001apk.ui.topic

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.constant.Constants.LOADING_FAILED
import com.example.c001apk.logic.model.TopicBean
import com.example.c001apk.logic.repository.BlackListRepo
import com.example.c001apk.logic.repository.NetworkRepo
import com.example.c001apk.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopicViewModel @Inject constructor(
    private val repository: BlackListRepo,
    private val networkRepo: NetworkRepo
) : ViewModel() {

    var postFollowData: HashMap<String, String>? = null
    var productTitle = "最近回复"
    var subtitle: String? = null
    var isFollow: Boolean = false
    var initData: Boolean = true
    var id: String? = null
    var title: String? = null
    var url: String? = null
    var type: String? = null
    var isInit = true
    var tabList = ArrayList<String>()
    val topicList: MutableList<TopicBean> = ArrayList()
    var page = 1

    var tabSelected: Int = 0
    val followState = MutableLiveData<Event<Pair<Boolean, String>>>()
    val blockState = MutableLiveData<Event<Boolean>>()
    val loadingState = MutableLiveData<LoadingState>()

    fun fetchTopicLayout() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getTopicLayout(url.toString())
                .collect { result ->
                    val data = result.getOrNull()
                    if (data != null) {
                        if (!data.message.isNullOrEmpty()) {
                            loadingState.postValue(LoadingState.LoadingError(data.message))
                            return@collect
                        } else if (data.data != null) {
                            isFollow = data.data.userAction?.follow == 1
                            if (tabList.isEmpty()) {
                                id = data.data.id
                                type = data.data.entityType
                                subtitle = data.data.intro

                                data.data.tabList.forEach {
                                    tabList.add(it.title)
                                    topicList.add(TopicBean(it.url, it.title))
                                }
                                run breaking@{
                                    data.data.tabList.forEachIndexed { index, tab ->
                                        if (data.data.selectedTab == tab.pageName) {
                                            tabSelected = index
                                            return@breaking
                                        }
                                    }
                                }
                            }
                            loadingState.postValue(LoadingState.LoadingDone)
                        }
                    } else {
                        loadingState.postValue(LoadingState.LoadingFailed(LOADING_FAILED))
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }


    fun fetchProductLayout() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getProductLayout(id.toString())
                .collect { result ->
                    val data = result.getOrNull()
                    if (data != null) {
                        if (!data.message.isNullOrEmpty()) {
                            loadingState.postValue(LoadingState.LoadingError(data.message))
                            return@collect
                        } else if (data.data != null) {
                            isFollow = data.data.userAction?.follow == 1
                            if (tabList.isEmpty()) {
                                subtitle = data.data.intro

                                data.data.tabList.forEach {
                                    tabList.add(it.title)
                                    topicList.add(TopicBean(it.url, it.title))
                                }
                                run breaking@{
                                    data.data.tabList.forEachIndexed { index, tab ->
                                        if (data.data.selectedTab == tab.pageName) {
                                            tabSelected = index
                                            return@breaking
                                        }
                                    }
                                }
                            }
                            loadingState.postValue(LoadingState.LoadingDone)
                        }
                    } else {
                        loadingState.postValue(LoadingState.LoadingFailed(LOADING_FAILED))
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    fun onGetFollow(followUrl: String, tag: String) {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getFollow(followUrl, tag, null)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (!response.message.isNullOrEmpty()) {
                            if (response.message.contains("关注成功")) {
                                isFollow = !isFollow
                                followState.postValue(Event(Pair(true, response.message)))
                            } else
                                followState.postValue(Event(Pair(false, response.message)))
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
                networkRepo.postFollow(it)
                    .collect { result ->
                        val response = result.getOrNull()
                        if (response != null) {
                            if (!response.message.isNullOrEmpty()) {
                                if (response.message.contains("手机吧成功")) {
                                    isFollow = !isFollow
                                    followState.postValue(Event(Pair(true, response.message)))
                                } else
                                    followState.postValue(Event(Pair(false, response.message)))
                            }
                        } else {
                            result.exceptionOrNull()?.printStackTrace()
                        }
                    }
            }

        }
    }

    fun saveTopic(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveTopic(title)
        }
    }

    fun checkTopic(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (repository.checkTopic(title))
                blockState.postValue(Event(true))
        }
    }

    fun deleteTopic(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTopic(title)
        }
    }

}