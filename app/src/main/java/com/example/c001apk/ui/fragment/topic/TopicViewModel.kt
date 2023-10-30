package com.example.c001apk.ui.fragment.topic

import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.c001apk.logic.network.Repository

class TopicViewModel : ViewModel() {

    var param1 = ""

    val tabList = ArrayList<String>()
    var fragmentList = ArrayList<Fragment>()

    var tag = ""
    var isInit = true
    var isNew = true

    private val getTopicLayoutLiveData = MutableLiveData<String>()

    val topicLayoutLiveData = getTopicLayoutLiveData.switchMap {
        Repository.getTopicLayout(tag)
    }

    fun getTopicLayout() {
        getTopicLayoutLiveData.value = getTopicLayoutLiveData.value
    }

}