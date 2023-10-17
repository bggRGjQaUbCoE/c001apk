package com.example.c001apk.ui.fragment.home.topic

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.network.Repository

class TopicViewModel : ViewModel() {

    val homeTopicTitleList = ArrayList<HomeFeedResponse.Entities>()

    private val getHomeTopicTitleLiveData = MutableLiveData<String>()

    val homeTopicTitleLiveData = getHomeTopicTitleLiveData.switchMap {
        Repository.getHomeTopicTitle()
    }

    fun getHomeTopicTitle() {
        getHomeTopicTitleLiveData.value = getHomeTopicTitleLiveData.value
    }

}