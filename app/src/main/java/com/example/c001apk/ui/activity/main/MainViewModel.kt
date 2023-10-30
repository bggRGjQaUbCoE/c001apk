package com.example.c001apk.ui.activity.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.c001apk.logic.network.Repository

class MainViewModel : ViewModel() {

    var isInit = true
    var isNew = true

    var badge = 0

    private val getCheckLoginInfoData = MutableLiveData<String>()

    val checkLoginInfoData = getCheckLoginInfoData.switchMap {
        Repository.checkLoginInfo()
    }

    fun getCheckLoginInfo() {
        getCheckLoginInfoData.value = getCheckLoginInfoData.value
    }
}