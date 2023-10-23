package com.example.c001apk.ui.activity.login

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.example.c001apk.constant.Constants
import com.example.c001apk.databinding.ActivityLoginBinding
import com.example.c001apk.logic.model.LoginResponse
import com.example.c001apk.logic.model.ProfileResponse
import com.example.c001apk.ui.activity.BaseActivity
import com.example.c001apk.ui.activity.MainActivity
import com.example.c001apk.util.ActivityCollector
import com.example.c001apk.util.CookieUtil
import com.example.c001apk.util.LoginUtils.Companion.createRandomNumber
import com.example.c001apk.util.LoginUtils.Companion.createRequestHash
import com.example.c001apk.util.PrefManager
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.jsoup.Jsoup
import java.io.IOException
import kotlin.concurrent.thread


class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var requestHash = ""
    private var SESSID = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        getLoginParam()

        binding.login.setOnClickListener {
            if (binding.account.text.toString() == "" || binding.password.text.toString() == "")
                Toast.makeText(this, "用户名或密码为空", Toast.LENGTH_SHORT).show()
            else {
                Toast.makeText(this, "正在登录...", Toast.LENGTH_SHORT).show()
                tryLogin()
            }

        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }

    private fun tryLogin() {
        thread {
            try {
                val httpClient = OkHttpClient()
                val formBody: RequestBody = FormBody.Builder()
                    .add("submit", "1")
                    .add("randomNumber", createRandomNumber())
                    .add("requestHash", requestHash)
                    .add("login", binding.account.text.toString())
                    .add("password", binding.password.text.toString())
                    .add("captcha", "")
                    .add("code", "")
                    .build()

                val getRequest: Request = Request.Builder()
                    .addHeader("Cookie", "$SESSID; forward=https://www.coolapk.com")
                    .addHeader("X-Requested-With", "XMLHttpRequest")
                    .url("https://account.coolapk.com/auth/loginByCoolApk")
                    .post(formBody)
                    .build()

                val call: Call = httpClient.newCall(getRequest)

                call.enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("LoginActivity", "onFailure: ${e.message}")
                    }

                    override fun onResponse(call: Call, response: Response) {

                        val login: LoginResponse = Gson().fromJson(
                            response.body!!.string(),
                            LoginResponse::class.java
                        )

                        if (login.status == 1) {
                            val headers = response.headers
                            val cookies = headers.values("Set-Cookie");
                            val uid =
                                cookies[cookies.size - 3].substring(
                                    0,
                                    cookies[cookies.size - 3].indexOf(";")
                                )
                            val name =
                                cookies[cookies.size - 2].substring(
                                    0,
                                    cookies[cookies.size - 2].indexOf(";")
                                )
                            val token =
                                cookies[cookies.size - 1].substring(
                                    0,
                                    cookies[cookies.size - 1].indexOf(";")
                                )
                            PrefManager.uid = uid
                            PrefManager.name = name
                            PrefManager.token = token
                            getUserProfile()
                        } else {
                            loginFailed(login.message)
                        }


                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loginFailed(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            //binding.loginMess.text = message
        }
    }

    private fun afterLogin() {

        val handler = Handler(Looper.getMainLooper())

        class MyThread : Runnable {
            override fun run() {
                ActivityCollector.recreateActivity(MainActivity::class.java.name)
            }
        }
        handler.post(MyThread())

        runOnUiThread {
            Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show()
            finish()
        }

    }

    private fun getUserProfile() {
        thread {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://api2.coolapk.com/v6/user/profile?${PrefManager.uid}")
                    .addHeader("User-Agent", Constants.USER_AGENT)
                    .addHeader("X-Requested-With", Constants.REQUEST_WIDTH)
                    .addHeader("X-Sdk-Int", "33")
                    .addHeader("X-Sdk-Locale", "zh-CN")
                    .addHeader("X-App-Id", Constants.APP_ID)
                    .addHeader("X-App-Token", CookieUtil.token)
                    .addHeader("X-App-Version", "13.3.1")
                    .addHeader("X-App-Code", "2307121")
                    .addHeader("X-Api-Version", "13")
                    .addHeader("X-App-Device", CookieUtil.deviceCode)
                    .addHeader("X-Dark-Mode", "0")
                    .addHeader("X-App-Channel", "coolapk")
                    .addHeader("X-App-Mode", "universal")
                    .addHeader(
                        "Cookie",
                        "${PrefManager.uid}; ${PrefManager.name}; ${PrefManager.token}"
                    )
                    .build()
                val response = client.newCall(request).execute()
                val profile: ProfileResponse = Gson().fromJson(
                    response.body!!.string(),
                    ProfileResponse::class.java
                )
                PrefManager.apply {
                    userAvatar = profile.data.userAvatar
                    level = profile.data.level
                    experience = profile.data.experience.toString()
                    nextLevelExperience = profile.data.nextLevelExperience.toString()
                    isLogin = true
                }
                afterLogin()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getLoginParam() {
        thread {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://account.coolapk.com/auth/loginByCoolApk")
                    .addHeader("X-Requested-With", "com.coolapk.market")
                    .addHeader("X-App-Id", "com.coolapk.market")
                    .build()
                val response = client.newCall(request).execute()
                val headers = response.headers
                val cookies = headers.values("Set-Cookie");
                val session = cookies[0]
                val sessionID = session.substring(0, session.indexOf(";"))
                SESSID = sessionID
                val body = response.body?.string()
                body?.apply {
                    requestHash = Jsoup.parse(this).createRequestHash()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}