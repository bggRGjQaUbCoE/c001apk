package com.example.c001apk.ui.fragment.meaasge

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.c001apk.logic.network.Repository

class MessageViewModel : ViewModel() {

    val countList = ArrayList<String>()

    private val getProfileDataLiveData = MutableLiveData<String>()

    var uid = ""

    val profileDataLiveData = getProfileDataLiveData.switchMap {
        Repository.getProfile(uid)
    }

    fun getProfile() {
        getProfileDataLiveData.value = getProfileDataLiveData.value
    }

}