package com.example.c001apk.ui.activity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.adapter.Event
import com.example.c001apk.constant.Constants
import com.example.c001apk.logic.network.Repository.checkLoginInfo
import com.example.c001apk.logic.network.Repository.getAppInfo
import com.example.c001apk.util.CookieUtil
import com.example.c001apk.util.PrefManager
import kotlinx.coroutines.launch
import java.net.URLEncoder

class MainViewModel : ViewModel() {

    var badge: Int = 0
    var isInit: Boolean = true
    val setBadge = MutableLiveData<Event<Boolean>>()

    fun fetchAppInfo(id: String) {
        viewModelScope.launch {
            getAppInfo(id)
                .collect { result ->
                    val appInfo = result.getOrNull()
                    if (appInfo?.data != null) {
                        try {
                            PrefManager.VERSION_NAME = appInfo.data.apkversionname
                            PrefManager.API_VERSION = "13"
                            PrefManager.VERSION_CODE = appInfo.data.apkversioncode
                            PrefManager.USER_AGENT =
                                "Dalvik/2.1.0 (Linux; U; Android ${PrefManager.ANDROID_VERSION}; ${PrefManager.MODEL} ${PrefManager.BUILDNUMBER}) (#Build; ${PrefManager.BRAND}; ${PrefManager.MODEL}; ${PrefManager.BUILDNUMBER}; ${PrefManager.ANDROID_VERSION}) +CoolMarket/${appInfo.data.apkversionname}-${appInfo.data.apkversioncode}-${Constants.MODE}"
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    getCheckLoginInfo()
                }
        }
    }

    private fun getCheckLoginInfo() {
        viewModelScope.launch {
            checkLoginInfo()
                .collect { result ->
                    val response = result.getOrNull()
                    response?.let {
                        response.body()?.let {
                            if (response.body()?.data?.token != null) {
                                val login = response.body()?.data!!
                                badge = login.notifyCount.badge
                                CookieUtil.notification = login.notifyCount.notification
                                CookieUtil.contacts_follow = login.notifyCount.contactsFollow
                                CookieUtil.message = login.notifyCount.message
                                CookieUtil.atme = login.notifyCount.atme
                                CookieUtil.atcommentme = login.notifyCount.atcommentme
                                CookieUtil.feedlike = login.notifyCount.feedlike
                                CookieUtil.badge = login.notifyCount.badge
                                PrefManager.isLogin = true
                                PrefManager.uid = login.uid
                                PrefManager.username = URLEncoder.encode(login.username, "UTF-8")
                                PrefManager.token = login.token
                                PrefManager.userAvatar = login.userAvatar
                            } else if (response.body()?.message == "登录信息有误") {
                                PrefManager.isLogin = false
                                PrefManager.uid = ""
                                PrefManager.username = ""
                                PrefManager.token = ""
                                PrefManager.userAvatar = ""
                            }

                            try {
                                val headers = response.headers()
                                val cookies = headers.values("Set-Cookie")
                                val session = cookies[0]
                                val sessionID = session.substring(0, session.indexOf(";"))
                                CookieUtil.SESSID = sessionID
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            if (badge != 0)
                                setBadge.postValue(Event(true))

                        }
                    }
                }
        }
    }

}