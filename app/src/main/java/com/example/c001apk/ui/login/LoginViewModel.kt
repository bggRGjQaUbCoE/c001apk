package com.example.c001apk.ui.login

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.adapter.FooterState
import com.example.c001apk.logic.model.LoginResponse
import com.example.c001apk.logic.repository.NetworkRepo
import com.example.c001apk.util.CookieUtil
import com.example.c001apk.util.Event
import com.example.c001apk.util.LoginUtils.createRequestHash
import com.example.c001apk.util.PrefManager
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val networkRepo: NetworkRepo
): ViewModel() {

    var uid: String? = null
    var url: String? = null
    var listSize: Int = -1
    var type: String? = null
    var isInit: Boolean = true
    var page = 1
    var lastItem: String? = null
    var isRefreshing: Boolean = false
    var isLoadMore: Boolean = false
    var isEnd: Boolean = false
    var lastVisibleItemPosition: Int = 0

    var requestHash: String? = null
    val footerState = MutableLiveData<FooterState>()
    var loginData = HashMap<String, String?>()
    val toastText = MutableLiveData<Event<String>>()
    val showCaptcha = MutableLiveData<Event<Bitmap>>()
    val getCaptcha = MutableLiveData<Event<String>>()
    val afterLogin = MutableLiveData<Event<Boolean>>()

    fun onPreGetLoginParam() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.preGetLoginParam()
                .collect { result ->
                    val response = result.getOrNull()
                    val body = response?.body()?.string()

                    body?.apply {
                        requestHash = Jsoup.parse(this).createRequestHash()
                    }
                    response?.apply {
                        try {
                            val cookies = response.headers().values("Set-Cookie")
                            val session = cookies[0]
                            val sessionID = session.substring(0, session.indexOf(";"))
                            CookieUtil.SESSID = sessionID
                        } catch (e: Exception) {
                            toastText.postValue(Event("无法获取cookie"))
                            e.printStackTrace()
                            return@collect
                        }
                        CookieUtil.isGetLoginParam = true
                        onGetLoginParam()
                    }
                }
        }
    }

    private fun onGetLoginParam() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getLoginParam()
                .collect { result ->
                    val response = result.getOrNull()
                    val body = response?.body()?.string()
                    body?.apply {
                        requestHash = Jsoup.parse(this).createRequestHash()
                    }
                    response?.apply {
                        try {
                            val cookies = response.headers().values("Set-Cookie")
                            val session = cookies[0]
                            val sessionID = session.substring(0, session.indexOf(";"))
                            CookieUtil.SESSID = sessionID
                        } catch (e: Exception) {
                            toastText.postValue(Event("无法获取cookie"))
                            e.printStackTrace()
                            return@collect
                        }
                    }
                }
        }
    }

    fun onGetCaptcha() {
        val timeStamp = System.currentTimeMillis().toString()
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getCaptcha("/auth/showCaptchaImage?$timeStamp")
                .collect { result ->
                    val response = result.getOrNull()
                    response?.let {
                        val responseBody = response.body()
                        val bitmap = BitmapFactory.decodeStream(responseBody?.byteStream())
                        showCaptcha.postValue(Event(bitmap))
                    }
                }
        }
    }

    fun onTryLogin() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.tryLogin(loginData)
                .collect { result ->
                    val response = result.getOrNull()
                    response?.body()?.let {
                        val login: LoginResponse = Gson().fromJson(
                            response.body()?.string(),
                            LoginResponse::class.java
                        )
                        if (login.status == 1) {
                            val headers = response.headers()
                            val cookies = headers.values("Set-Cookie")
                            val uid =
                                cookies[cookies.size - 3].substring(
                                    4,
                                    cookies[cookies.size - 3].indexOf(";")
                                )
                            val name =
                                cookies[cookies.size - 2].substring(
                                    9,
                                    cookies[cookies.size - 2].indexOf(";")
                                )
                            val token =
                                cookies[cookies.size - 1].substring(
                                    6,
                                    cookies[cookies.size - 1].indexOf(";")
                                )
                            PrefManager.isLogin = true
                            PrefManager.uid = uid
                            PrefManager.username = name
                            PrefManager.token = token
                            this@LoginViewModel.uid = uid
                            onGetProfile()
                        } else {
                            login.message?.let {
                                getCaptcha.postValue(Event(it))
                            }
                        }
                    }
                }
        }
    }

    private fun onGetProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getProfile(uid.toString())
                .collect { result ->
                    val data = result.getOrNull()
                    data?.data.let {
                        PrefManager.userAvatar = data?.data?.userAvatar.toString()
                        PrefManager.level = data?.data?.level.toString()
                        PrefManager.experience = data?.data?.experience.toString()
                        PrefManager.nextLevelExperience = data?.data?.nextLevelExperience.toString()
                        afterLogin.postValue(Event(true))
                    }
                }
        }


    }

}