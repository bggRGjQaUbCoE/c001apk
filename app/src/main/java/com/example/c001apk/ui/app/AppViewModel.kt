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
    val blockState = MutableLiveData<Event<Boolean>>()

    val searchState = MutableLiveData<Event<Unit>>()
    val followState = MutableLiveData<Event<Int>>()

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
                            } else {
                                errMsg = appInfo.data.commentStatusText
                            }
                            checkBlock(appInfo.data.title ?: "") //menuBlock
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

    fun onGetFollowApk(followUrl: String, tag: String?, id: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getFollow(followUrl, tag, id)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (!response.message.isNullOrEmpty()) {
                            toastText.postValue(Event(response.message))
                        } else if (response.data?.follow != null) {
                            response.data.follow.let {
                                appData?.userAction?.follow = it
                                checkFollow()
                                val text = if (it == 1) "关注成功"
                                else "取消关注成功"
                                toastText.postValue(Event(text))
                            }
                        }
                    } else {
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

    private fun checkBlock(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            blockState.postValue(Event((blackListRepo.checkTopic(title))))
        }
    }

    private fun checkFollow() {
        viewModelScope.launch(Dispatchers.IO) {
            appData?.userAction?.follow?.let {
                followState.postValue(Event(it))
            }
        }
    }

    override fun fetchData() {}

    fun checkMenuState() {
        checkFollow()
        searchState.postValue(Event(Unit))
        appData?.title?.let {
            checkBlock(it)
        }
    }

}