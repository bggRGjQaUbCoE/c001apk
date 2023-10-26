package com.example.c001apk.ui.activity.login

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.example.c001apk.R
import com.example.c001apk.constant.Constants
import com.example.c001apk.databinding.ActivityLoginBinding
import com.example.c001apk.logic.model.FeedContentResponse
import com.example.c001apk.logic.model.LoginResponse
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
    private var isLoginPass = true

    private val filter =
        InputFilter { source: CharSequence, _: Int, _: Int, _: Spanned?, _: Int, _: Int ->
            if (source == " ")
                return@InputFilter ""
            else
                return@InputFilter null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        getLoginParam()

        binding.apply {
            account.filters = arrayOf(filter)
            password.filters = arrayOf(filter)
            sms.filters = arrayOf(filter)
            captchaText.filters = arrayOf(filter)
        }


        binding.getSMS.setOnClickListener {
            if (binding.account.text.toString() == "")
                Toast.makeText(this, "手机号不能为空", Toast.LENGTH_SHORT).show()
            else
                getSMS()
        }

        binding.login.setOnClickListener {
            if (isLoginPass) {
                if (binding.account.text.toString() == "" || binding.password.text.toString() == "")
                    Toast.makeText(this, "用户名或密码为空", Toast.LENGTH_SHORT).show()
                else {
                    Toast.makeText(this, "正在登录...", Toast.LENGTH_SHORT).show()
                    tryLogin()
                }
            } else {
                if (binding.account.text.toString() == "" || binding.sms.text.toString() == "")
                    Toast.makeText(this, "手机号或验证码为空", Toast.LENGTH_SHORT).show()
                else {
                    Toast.makeText(this, "正在登录...", Toast.LENGTH_SHORT).show()
                    tryLogin()
                }
            }

        }

        binding.captchaImg.setOnClickListener {
            getCaptcha()
        }

    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.login_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()

            R.id.loginPass -> {
                isLoginPass = true
                binding.passLayout.visibility = View.VISIBLE
                binding.smsLayout.visibility = View.GONE
                binding.getSMS.visibility = View.GONE
            }

            R.id.loginPhone -> {
                isLoginPass = false
                binding.getSMS.visibility = View.VISIBLE
                binding.smsLayout.visibility = View.VISIBLE
                binding.passLayout.visibility = View.GONE
            }
        }
        return true
    }

    private fun getSMS() {

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
                    .add("captcha", binding.captchaText.text.toString())
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
                            getUserProfile()
                        } else {
                            loginFailed(login.message)
                            if (login.message == "图形验证码不能为空") {
                                runOnUiThread {
                                    binding.captcha.visibility = View.VISIBLE
                                }
                                getCaptcha()
                            } else if (login.message == "图形验证码错误") {
                                getCaptcha()
                            } else if (login.message == "密码错误" && binding.captcha.visibility == View.VISIBLE) {
                                getCaptcha()
                            }

                        }


                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getCaptcha() {
        thread {
            try {
                val timeStamp = System.currentTimeMillis()
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://account.coolapk.com/auth/showCaptchaImage?$timeStamp")
                    .addHeader(
                        "sec-ch-ua",
                        """Android WebView";v="117", "Not;A=Brand";v="8", "Chromium";v="117"""
                    )
                    .addHeader("sec-ch-ua-mobile", "?1")
                    .addHeader("User-Agent", Constants.USER_AGENT)
                    .addHeader("sec-ch-ua-platform", "Android")
                    .addHeader(
                        "Accept",
                        """image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8"""
                    )
                    .addHeader("X-Requested-With", "com.coolapk.market")
                    .addHeader("Sec-Fetch-Site", "same-origin")
                    .addHeader("Sec-Fetch-Mode", "no-cors")
                    .addHeader("Sec-Fetch-Dest", "image")
                    .addHeader("Referer", "https://account.coolapk.com/auth/login?type=mobile")
                    .addHeader("Cookie", "$SESSID; forward=https://www.coolapk.com")
                    .build()
                val response = client.newCall(request).execute()
                val inputStream = response.body!!.byteStream()
                val bitmap = BitmapFactory.decodeStream(inputStream)
                runOnUiThread {
                    binding.captchaImg.setImageBitmap(bitmap)
                }
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
                    .url("https://api2.coolapk.com/v6/user/profile?uid=${PrefManager.uid}")
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
                        "uid=${PrefManager.uid}; username=${PrefManager.username}; token=${PrefManager.token}"
                    )
                    .build()
                val response = client.newCall(request).execute()
                val profile: FeedContentResponse = Gson().fromJson(
                    response.body!!.string(),
                    FeedContentResponse::class.java
                )
                PrefManager.userAvatar = profile.data.userAvatar
                PrefManager.level = profile.data.level
                PrefManager.experience = profile.data.experience.toString()
                PrefManager.nextLevelExperience = profile.data.nextLevelExperience.toString()
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