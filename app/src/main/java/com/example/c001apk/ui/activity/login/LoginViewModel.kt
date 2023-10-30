package com.example.c001apk.ui.activity.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.c001apk.logic.network.Repository

class LoginViewModel : ViewModel() {

    private val getLoginParamData = MutableLiveData<String>()

    val loginParamData = getLoginParamData.switchMap {
        Repository.getLoginParam()
    }

    fun getLoginParam() {
        getLoginParamData.value = getLoginParamData.value
    }

    var loginData = HashMap<String, String?>()

    private val getTryLoginData = MutableLiveData<String>()

    val tryLoginData = getTryLoginData.switchMap {
        Repository.tryLogin(loginData)
    }

    fun tryLogin() {
        getTryLoginData.value = getTryLoginData.value
    }

    private val getCaptchaData = MutableLiveData<String>()

    var timeStamp = 0L
    private val baseUrl = "/auth/showCaptchaImage?"
    val captchaData = getCaptchaData.switchMap {
        Repository.getCaptcha("$baseUrl$timeStamp")
    }

    fun getCaptcha() {
        getCaptchaData.value = getCaptchaData.value
    }

    private val getProfileDataLiveData = MutableLiveData<String>()

    var uid = ""

    val profileDataLiveData = getProfileDataLiveData.switchMap {
        Repository.getProfile(uid)
    }

    fun getProfile() {
        getProfileDataLiveData.value = getProfileDataLiveData.value
    }

}