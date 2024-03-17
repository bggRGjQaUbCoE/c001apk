package com.example.c001apk.ui.app

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.adapter.FooterAdapter
import com.example.c001apk.adapter.ItemListener
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.network.Repository
import com.example.c001apk.logic.network.Repository.getFollow
import com.example.c001apk.logic.repository.BlackListRepository
import com.example.c001apk.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val repository: BlackListRepository
): ViewModel() {

    var collectionUrl: String? = null
    private var commentStatusText: String? = null
    var tabList: List<String>? = null
    var url: String? = null
    var type: String? = null
    var isFollow: Boolean = false
    var followUrl: String? = null
    var appId: String? = null
    var title: String? = null
    var isInit: Boolean = true
    var uid: String? = null
    var errorMessage: String? = null
    var uname: String? = null
    var lastVisibleItemPosition: Int = 0
    var isRefreshing: Boolean = true
    var isLoadMore: Boolean = false
    var isEnd: Boolean = false
    var page = 1
    var listSize: Int = -1
    var avatar: String? = null
    var cover: String? = null
    var level: String? = null
    var like: String? = null
    var follow: String? = null
    var fans: String? = null
    var packageName: String? = null
    private var versionCode: String? = null
    val showError = MutableLiveData<Event<Boolean>>()
    val changeState = MutableLiveData<Pair<FooterAdapter.LoadState, String?>>()
    val doNext = MutableLiveData<Event<Boolean>>()
    val showAppInfo = MutableLiveData<Event<Boolean>>()
    var appData: HomeFeedResponse.Data? = null
    val download = MutableLiveData<Event<Boolean>>()
    val toastText = MutableLiveData<Event<String>>()

    fun fetchAppInfo(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Repository.getAppInfo(id)
                .collect { result ->

                    val appInfo = result.getOrNull()
                    if (appInfo?.message != null) {
                        errorMessage = appInfo.message
                        showError.postValue(Event(true))
                    } else if (appInfo?.data != null) {
                        isFollow = appInfo.data.userAction?.follow == 1
                        packageName = appInfo.data.apkname
                        versionCode = appInfo.data.apkversioncode
                        commentStatusText = appInfo.data.commentStatusText
                        type = appInfo.data.entityType
                        appId = appInfo.data.id
                        title = appInfo.data.title
                        appData = appInfo.data
                        showAppInfo.postValue(Event(true))

                        if (commentStatusText == "允许评论" || type == "appForum") {
                            tabList = listOf("最近回复", "最新发布", "热度排序")
                            doNext.postValue(Event(true))
                        } else {
                            errorMessage = appInfo.data.commentStatusText
                            doNext.postValue(Event(false))
                        }
                    } else {
                        showAppInfo.postValue(Event(false))
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }


    fun onGetDownloadLink() {
        viewModelScope.launch(Dispatchers.IO) {
            Repository.getAppDownloadLink(
                packageName.toString(),
                appId.toString(),
                versionCode.toString()
            )
                .collect { result ->
                    val link = result.getOrNull()
                    if (link != null) {
                        collectionUrl = link
                        download.postValue(Event(true))
                    } else {
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }

    }

    fun onGetFollow() {
        viewModelScope.launch(Dispatchers.IO) {
            getFollow(followUrl.toString(), null, appId)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        response.data?.follow?.let {
                            isFollow = !isFollow
                            toastText.postValue(
                                Event(
                                    if (response.data.follow == 1) "关注成功"
                                    else "取消关注成功"
                                )
                            )
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


    inner class ItemClickListener : ItemListener

}