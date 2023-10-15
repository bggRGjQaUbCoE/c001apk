package com.example.c001apk.ui.fragment.topic

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.c001apk.MyApplication
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.network.Repository

class TopicViewModel : ViewModel() {

    var tag = ""

    private val getTopicLayoutLiveData = MutableLiveData<String>()

    val topicLayoutLiveData = getTopicLayoutLiveData.switchMap {
        Repository.getTopicLayout(tag)
    }

    fun getTopicLayout() {
        getTopicLayoutLiveData.value = getTopicLayoutLiveData.value
    }

}