package com.example.c001apk.ui.activity

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.adapter.Event
import com.example.c001apk.adapter.FooterAdapter
import com.example.c001apk.adapter.ItemListener
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.model.Like
import com.example.c001apk.logic.network.Repository
import com.example.c001apk.logic.network.Repository.getFollow
import kotlinx.coroutines.launch

class ApkViewModel : ViewModel() {

    var collectionUrl: String? = null
    var commentStatusText: String? = null
    val tabList = ArrayList<String>()
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
    var firstVisibleItemPosition = 0
    var page = 1
    var listSize: Int = -1
    var avatar: String? = null
    var cover: String? = null
    var level: String? = null
    var like: String? = null
    var follow: String? = null
    var fans: String? = null
    var packageName: String? = null
    var versionCode: String? = null


    val showError = MutableLiveData<Event<Boolean>>()
    val changeState = MutableLiveData<Pair<FooterAdapter.LoadState, String?>>()
    val doNext = MutableLiveData<Event<Boolean>>()
    val showAppInfo = MutableLiveData<Event<Boolean>>()
    var appData: HomeFeedResponse.Data? = null
    val download = MutableLiveData<Event<Boolean>>()
    val toastText = MutableLiveData<Event<String>>()

    fun fetchAppInfo(id: String) {
        viewModelScope.launch {
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
                        /*

                      title = appInfo.data.title
                        version =
                            "版本: ${appInfo.data.version}(${appInfo.data.apkversioncode})"
                        size = "大小: ${appInfo.data.apksize}"
                        lastupdate = if (appInfo.data.lastupdate == null) "更新时间: null"
                        else "更新时间: ${DateUtils.fromToday(appInfo.data.lastupdate)}"
                        logo = appInfo.data.logo

                        showAppInfo()*/
                        appData = appInfo.data
                        showAppInfo.postValue(Event(true))

                        if (commentStatusText == "允许评论" || type == "appForum") {
                            tabList.apply {
                                add("最近回复")
                                add("最新发布")
                                add("热度排序")
                            }
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
        viewModelScope.launch {
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
        viewModelScope.launch {
            getFollow(followUrl.toString(), null, appId)
                .collect{result->
                    val response = result.getOrNull()
                    if (response != null) {
                        response.data?.follow?.let {
                            isFollow = !isFollow
                            toastText.postValue(
                                Event(if (response.data.follow == 1) "关注成功"
                                else "取消关注成功")
                            )
                        }
                    } else {
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }


    inner class ItemClickListener : ItemListener {

    }

}