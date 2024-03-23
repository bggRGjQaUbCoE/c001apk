package com.example.c001apk.ui.topic

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.constant.Constants.LOADING_FAILED
import com.example.c001apk.logic.model.TopicBean
import com.example.c001apk.logic.repository.BlackListRepo
import com.example.c001apk.logic.repository.NetworkRepo
import com.example.c001apk.util.Event
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TopicViewModel @AssistedInject constructor(
    @Assisted("url") var url: String,
    @Assisted("title") val title: String,
    @Assisted("id") var id: String,
    @Assisted("type") var type: String,
    private val repository: BlackListRepo,
    private val networkRepo: NetworkRepo
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("url") url: String,
            @Assisted("title") title: String,
            @Assisted("id") id: String,
            @Assisted("type") type: String
        ): TopicViewModel
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun provideFactory(
            assistedFactory: Factory, url: String, title: String, id: String, type: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(url, title, id, type) as T
            }
        }
    }

    var postFollowData: HashMap<String, String>? = null
    var productTitle = "最近回复"
    var subtitle: String? = null
    var isFollow: Boolean = false
    var initData: Boolean = true
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
            networkRepo.getTopicLayout(url)
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
            networkRepo.getProductLayout(id)
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