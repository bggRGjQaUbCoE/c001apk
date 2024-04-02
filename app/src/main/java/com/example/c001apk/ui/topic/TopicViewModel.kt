package com.example.c001apk.ui.topic

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.constant.Constants
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.model.TopicBean
import com.example.c001apk.logic.repository.BlackListRepo
import com.example.c001apk.logic.repository.HistoryFavoriteRepo
import com.example.c001apk.logic.repository.NetworkRepo
import com.example.c001apk.ui.base.BaseAppViewModel
import com.example.c001apk.util.Event
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = TopicViewModel.Factory::class)
class TopicViewModel @AssistedInject constructor(
    @Assisted("url") var url: String,
    @Assisted("title") val title: String,
    @Assisted("id") var id: String,
    @Assisted("type") var type: String,
    blackListRepo: BlackListRepo,
    historyRepo: HistoryFavoriteRepo,
    networkRepo: NetworkRepo
) : BaseAppViewModel(blackListRepo, historyRepo, networkRepo) {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("url") url: String,
            @Assisted("title") title: String,
            @Assisted("id") id: String,
            @Assisted("type") type: String
        ): TopicViewModel
    }

    var subtitle: String? = null
    var productTitle = "最近回复"

    var isAInit: Boolean = true
    var postFollowData: HashMap<String, String>? = null
    var isFollow: Boolean = false
    var tabSelected: Int? = null
    var topicList: List<TopicBean> = ArrayList()

    val blockState = MutableLiveData<Event<Boolean>>()
    val followState = MutableLiveData<Event<Boolean>>()

    fun fetchTopicLayout() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getTopicLayout(url)
                .collect { result ->
                    val data = result.getOrNull()
                    if (data != null) {
                        if (!data.message.isNullOrEmpty()) {
                            activityState.postValue(LoadingState.LoadingError(data.message))
                            return@collect
                        } else if (data.data != null) {
                            isFollow = data.data.userAction?.follow == 1
                            id = data.data.id ?: ""
                            type = data.data.entityType
                            subtitle = data.data.intro
                            getTopicList(data.data.tabList, data.data.selectedTab.toString())
                            checkFollow()
                            activityState.postValue(LoadingState.LoadingDone)
                        }
                    } else {
                        activityState.postValue(LoadingState.LoadingFailed(Constants.LOADING_FAILED))
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
                            activityState.postValue(LoadingState.LoadingError(data.message))
                            return@collect
                        } else if (data.data != null) {
                            isFollow = data.data.userAction?.follow == 1
                            subtitle = data.data.intro
                            getTopicList(data.data.tabList, data.data.selectedTab.toString())
                            checkFollow()
                            activityState.postValue(LoadingState.LoadingDone)
                        }
                    } else {
                        activityState.postValue(LoadingState.LoadingFailed(Constants.LOADING_FAILED))
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    private fun getTopicList(tabList: List<HomeFeedResponse.TabList>?, selectedTab: String) {
        topicList = tabList?.map {
            TopicBean(it.url.toString(), it.title.toString())
        } ?: emptyList()
        run breaking@{
            tabList?.forEachIndexed { index, tab ->
                if (selectedTab == tab.pageName) {
                    tabSelected = index
                    return@breaking
                }
            }
        }
    }

    // follow app/topic
    fun onGetFollow(followUrl: String, tag: String?, id: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getFollow(followUrl, tag, id)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (!response.message.isNullOrEmpty()) {
                            if (response.message.contains("关注成功")) {
                                isFollow = !isFollow
                                checkFollow()
                            }
                            toastText.postValue(Event(response.message))
                        }
                    } else {
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    // follow product
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
                                    checkFollow()
                                }
                                toastText.postValue(Event(response.message))
                            }
                        } else {
                            result.exceptionOrNull()?.printStackTrace()
                        }
                    }
            }
        }
    }

    private fun checkTopic(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            blockState.postValue(Event(blackListRepo.checkTopic(title)))
        }
    }

    private fun checkFollow() {
        viewModelScope.launch(Dispatchers.IO) {
            followState.postValue(Event(isFollow))
        }
    }

    override fun fetchData() {}

    fun checkMenuState() {
        checkTopic(title)
        checkFollow()
    }

}