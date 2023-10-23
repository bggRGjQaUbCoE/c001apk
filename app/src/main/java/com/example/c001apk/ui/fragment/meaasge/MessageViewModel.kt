package com.example.c001apk.ui.fragment.meaasge

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.c001apk.logic.network.Repository
import com.example.c001apk.util.PrefManager

class MessageViewModel : ViewModel() {

    private val getProfileDataLiveData = MutableLiveData<String>()

    var uid =
        if (PrefManager.isLogin) PrefManager.uid.substring(4, PrefManager.uid.length)
        else ""

    val profileDataLiveData = getProfileDataLiveData.switchMap {
        Repository.getProfile(uid)
    }

    fun getProfile() {
        getProfileDataLiveData.value = getProfileDataLiveData.value
    }

}