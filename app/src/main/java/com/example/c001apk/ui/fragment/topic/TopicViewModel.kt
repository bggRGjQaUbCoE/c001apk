package com.example.c001apk.ui.fragment.topic

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.c001apk.logic.network.Repository

class TopicViewModel : ViewModel() {

    var tag = ""
    var isInit = true

    private val getTopicLayoutLiveData = MutableLiveData<String>()

    val topicLayoutLiveData = getTopicLayoutLiveData.switchMap {
        Repository.getTopicLayout(tag)
    }

    fun getTopicLayout() {
        getTopicLayoutLiveData.value = getTopicLayoutLiveData.value
    }

}