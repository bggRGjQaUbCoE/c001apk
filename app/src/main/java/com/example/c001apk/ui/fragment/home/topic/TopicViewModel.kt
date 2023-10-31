package com.example.c001apk.ui.fragment.home.topic

import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.network.Repository

class TopicViewModel : ViewModel() {

    var titleList = ArrayList<String>()
    var fragmentList = ArrayList<Fragment>()

    var isInit = true
    var isNew = true

    val homeTopicTitleList = ArrayList<HomeFeedResponse.Entities>()

    private val getHomeTopicTitleLiveData = MutableLiveData<String>()

    val homeTopicTitleLiveData = getHomeTopicTitleLiveData.switchMap {
        Repository.getDataList("/page?url=V11_VERTICAL_TOPIC", "话题", "", "", 1)
    }

    fun getHomeTopicTitle() {
        getHomeTopicTitleLiveData.value = getHomeTopicTitleLiveData.value
    }

}