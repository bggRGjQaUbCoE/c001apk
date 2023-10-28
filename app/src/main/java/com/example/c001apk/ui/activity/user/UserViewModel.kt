package com.example.c001apk.ui.activity.user

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.network.Repository

class UserViewModel : ViewModel() {

    var id = ""
    var uid = ""
    var page = 1
    var isInit = true
    var isRefreh = true
    var isEnd = false
    var isLoadMore = false

    private val getUserData = MutableLiveData<String>()

    val userData = getUserData.switchMap {
        Repository.getUserSpace(id)
    }

    fun getUser() {
        getUserData.value = getUserData.value
    }


    val feedContentList = ArrayList<HomeFeedResponse.Data>()

    private val getUserFeedData = MutableLiveData<String>()

    val userFeedData = getUserFeedData.switchMap {
        Repository.getUserFeed(uid, page)
    }

    fun getUserFeed() {
        getUserFeedData.value = getUserFeedData.value
    }

}