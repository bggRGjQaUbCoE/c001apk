package com.example.c001apk.ui.activity.app

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.network.Repository

class AppViewModel : ViewModel() {

    var id = ""
    val baseURL =
        "#/feed/apkCommentList?isIncludeTop=1&withSortCard=1&id="
    var appId = ""
    var page = 1
    var isInit = true
    var isRefreh = true
    var isEnd = false
    var isLoadMore = false

    private val getAppInfoData = MutableLiveData<String>()

    val appInfoData = getAppInfoData.switchMap {
        Repository.getAppInfo(id)
    }

    fun getAppInfo() {
        getAppInfoData.value = getAppInfoData.value
    }


    val appCommentList = ArrayList<HomeFeedResponse.Data>()

    private val getAppCommentData = MutableLiveData<String>()

    val appCommentData = getAppCommentData.switchMap {
        Repository.getAppComment(baseURL + appId, page)
    }

    fun getAppComment() {
        getAppCommentData.value = getAppCommentData.value
    }

}