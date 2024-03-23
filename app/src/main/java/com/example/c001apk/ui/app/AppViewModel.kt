package com.example.c001apk.ui.app

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.constant.Constants.LOADING_FAILED
import com.example.c001apk.logic.model.HomeFeedResponse
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

@HiltViewModel(assistedFactory = AppViewModel.Factory::class)
class AppViewModel @AssistedInject constructor(
    @Assisted private val id: String,
    blackListRepo: BlackListRepo,
    historyRepo: HistoryFavoriteRepo,
    networkRepo: NetworkRepo
) : BaseAppViewModel(blackListRepo, historyRepo, networkRepo) {

    @AssistedFactory
    interface Factory {
        fun create(id: String): AppViewModel
    }

    var tabList: List<String>? = null
    var errMsg: String? = null
    var downloadUrl: String? = null

    // get download link params
    var appId: String? = null
    private var packageName: String? = null
    private var versionCode: String? = null
    val download = MutableLiveData<Event<Boolean>>()

    var appData: HomeFeedResponse.Data? = null
    val activityState = MutableLiveData<LoadingState>()
    val blockState = MutableLiveData<Event<Boolean>>()

    //val followState = MutableLiveData<Event<Int?>>()
    val searchState = MutableLiveData<Event<Unit>>()
    //val toastText = MutableLiveData<Event<Int?>>()

    fun fetchAppInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getAppInfo(id)
                .collect { result ->
                    val appInfo = result.getOrNull()
                    if (appInfo != null) {
                        if (appInfo.message != null) {
                            activityState.postValue(LoadingState.LoadingError(appInfo.message))
                        } else if (appInfo.data != null) {
                            // get download link params
                            appId = appInfo.data.id
                            packageName = appInfo.data.apkname
                            versionCode = appInfo.data.apkversioncode
                            // appData for App Info
                            appData = appInfo.data
                            if (appInfo.data.commentStatusText == "允许评论" || appInfo.data.entityType == "appForum") {
                                tabList = listOf("最近回复", "最新发布", "热度排序")
                                searchState.postValue(Event(Unit))
                            } else {
                                errMsg = appInfo.data.commentStatusText
                            }
                            checkApp(appInfo.data.title) //menuBlock
                            checkFollow() //menuFollow
                            activityState.postValue(LoadingState.LoadingDone)
                        }
                    } else {
                        activityState.postValue(LoadingState.LoadingFailed(LOADING_FAILED))
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    fun onGetDownloadLink() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getAppDownloadLink(
                packageName.toString(),
                appId.toString(),
                versionCode.toString()
            )
                .collect { result ->
                    val link = result.getOrNull()
                    if (link != null) {
                        downloadUrl = link
                        download.postValue(Event(true))
                    } else {
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }

    }

    fun saveTopic(title: String) {
        viewModelScope.launch {
            blackListRepo.saveTopic(title)
        }
    }

    fun checkApp(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            blockState.postValue(Event((blackListRepo.checkTopic(title))))
        }
    }

    private fun checkFollow() {
        viewModelScope.launch(Dispatchers.IO) {
            //  followState.postValue(Event(appData?.userAction?.follow))
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