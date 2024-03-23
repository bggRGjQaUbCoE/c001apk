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

    // var isInit: Boolean = true
    var subtitle: String? = null
    var productTitle = "最近回复"

    var tabSelected: Int? = null
    var topicList: List<TopicBean> = ArrayList()

    //  val loadingState = MutableLiveData<LoadingState>()
    val blockState = MutableLiveData<Event<Boolean>>()
    // val followState = MutableLiveData<Event<Pair<Boolean, String>>>()

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
                            id = data.data.id
                            type = data.data.entityType
                            subtitle = data.data.intro
                            getTopicList(data.data.tabList, data.data.selectedTab)
                            loadingState.postValue(LoadingState.LoadingDone)
                        }
                    } else {
                        loadingState.postValue(LoadingState.LoadingFailed(Constants.LOADING_FAILED))
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
                            subtitle = data.data.intro
                            getTopicList(data.data.tabList, data.data.selectedTab)
                            loadingState.postValue(LoadingState.LoadingDone)
                        }
                    } else {
                        loadingState.postValue(LoadingState.LoadingFailed(Constants.LOADING_FAILED))
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    private fun getTopicList(tabList: List<HomeFeedResponse.TabList>, selectedTab: String) {
        topicList = tabList.map {
            TopicBean(it.url, it.title)
        }
        run breaking@{
            tabList.forEachIndexed { index, tab ->
                if (selectedTab == tab.pageName) {
                    tabSelected = index
                    return@breaking
                }
            }
        }
    }

    fun saveTopic(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            blackListRepo.saveTopic(title)
        }
    }

    fun checkTopic(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (blackListRepo.checkTopic(title))
                blockState.postValue(Event(true))
        }
    }

    fun deleteTopic(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            blackListRepo.deleteTopic(title)
        }
    }

    override fun fetchData() {

    }

}