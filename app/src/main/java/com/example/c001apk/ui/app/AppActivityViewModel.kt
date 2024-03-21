package com.example.c001apk.ui.app

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.adapter.ItemListener
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.constant.Constants.LOADING_FAILED
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.repository.BlackListRepo
import com.example.c001apk.logic.repository.NetworkRepo
import com.example.c001apk.util.Event
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = AppActivityViewModel.Factory::class)
class AppActivityViewModel @AssistedInject constructor(
    @Assisted private val id: String,
    private val repository: BlackListRepo,
    private val networkRepo: NetworkRepo
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(id: String): AppActivityViewModel
    }

    var isInit: Boolean = true
    var tabList: List<String>? = null
    var errMsg: String? = null
    var downloadUrl: String? = null

    // get download link params
    var appId: String? = null
    private var packageName: String? = null
    private var versionCode: String? = null
    val download = MutableLiveData<Event<Boolean>>()

    var appData: HomeFeedResponse.Data? = null
    val loadingState = MutableLiveData<LoadingState>()
    val blockState = MutableLiveData<Event<Boolean>>()
    val followState = MutableLiveData<Event<Int?>>()
    val searchState = MutableLiveData<Event<Unit>>()
    val toastText = MutableLiveData<Event<Int>>()

    fun fetchAppInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getAppInfo(id)
                .collect { result ->
                    val appInfo = result.getOrNull()
                    if (appInfo != null) {
                        if (appInfo.message != null) {
                            loadingState.postValue(LoadingState.LoadingError(appInfo.message))
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
                            loadingState.postValue(LoadingState.LoadingDone)
                        }
                    } else {
                        loadingState.postValue(LoadingState.LoadingFailed(LOADING_FAILED))
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

    fun onGetFollow(followUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getFollow(followUrl, null, appId)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        response.data?.follow?.let {
                            appData?.userAction?.follow = it
                            checkFollow()
                            toastText.postValue(Event(it))
                        }
                    } else {
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    fun saveTopic(title: String) {
        viewModelScope.launch {
            repository.saveTopic(title)
        }
    }

    fun checkApp(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            blockState.postValue(Event((repository.checkTopic(title))))
        }
    }

    private fun checkFollow() {
        viewModelScope.launch(Dispatchers.IO) {
            followState.postValue(Event(appData?.userAction?.follow))

        }
    }

    fun deleteTopic(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTopic(title)
        }
    }

    inner class ItemClickListener : ItemListener

}