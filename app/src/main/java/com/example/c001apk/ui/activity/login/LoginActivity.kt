package com.example.c001apk.ui.activity.login

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.text.Spanned
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityLoginBinding
import com.example.c001apk.logic.model.LoginResponse
import com.example.c001apk.ui.activity.BaseActivity
import com.example.c001apk.ui.activity.main.MainActivity
import com.example.c001apk.util.ActivityCollector
import com.example.c001apk.util.CookieUtil.SESSID
import com.example.c001apk.util.CookieUtil.isGetCaptcha
import com.example.c001apk.util.CookieUtil.isGetLoginParam
import com.example.c001apk.util.CookieUtil.isTryLogin
import com.example.c001apk.util.CookieUtil.requestHash
import com.example.c001apk.util.LoginUtils.Companion.createRandomNumber
import com.example.c001apk.util.LoginUtils.Companion.createRequestHash
import com.example.c001apk.util.PrefManager
import com.google.gson.Gson
import org.jsoup.Jsoup


class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel by lazy { ViewModelProvider(this)[LoginViewModel::class.java] }
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

        isGetLoginParam = true
        viewModel.getLoginParam()

        viewModel.loginParamData.observe(this) { result ->
            val response = result.getOrNull()
            val body = response?.body()?.string()
            body?.apply {
                requestHash = Jsoup.parse(this).createRequestHash()
            }
            response?.apply {
                val cookies = response.headers().values("Set-Cookie")
                val session = cookies[0]
                val sessionID = session.substring(0, session.indexOf(";"))
                SESSID = sessionID
            }
        }

        viewModel.tryLoginData.observe(this) { result ->
            val response = result.getOrNull()
            response?.let {
                response.body()?.let {
                    val login: LoginResponse = Gson().fromJson(
                        response.body()!!.string(),
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
                        viewModel.uid = uid
                        viewModel.getProfile()
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
            }
        }

        viewModel.captchaData.observe(this) { result ->
            val response = result.getOrNull()
            response?.let {
                val responseBody = response.body()
                val bitmap = BitmapFactory.decodeStream(responseBody!!.byteStream())
                runOnUiThread {
                    binding.captchaImg.setImageBitmap(bitmap)
                }
            }
        }

        viewModel.profileDataLiveData.observe(this) { result ->
            val data = result.getOrNull()
            data?.let {
                PrefManager.userAvatar = data.userAvatar
                PrefManager.level = data.level
                PrefManager.experience = data.experience.toString()
                PrefManager.nextLevelExperience = data.nextLevelExperience.toString()
                afterLogin()
            }
        }


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
                else
                    tryLogin()
            } else {
                if (binding.account.text.toString() == "" || binding.sms.text.toString() == "")
                    Toast.makeText(this, "手机号或验证码为空", Toast.LENGTH_SHORT).show()
                else
                    tryLogin()
            }

        }

        binding.captchaImg.setOnClickListener {
            getCaptcha()
        }

    }

    private fun tryLogin() {
        Toast.makeText(this, "正在登录...", Toast.LENGTH_SHORT).show()
        isTryLogin = true
        viewModel.loginData["submit"] = "1"
        viewModel.loginData["randomNumber"] = createRandomNumber()
        viewModel.loginData["requestHash"] = requestHash
        viewModel.loginData["login"] = binding.account.text.toString()
        viewModel.loginData["password"] = binding.password.text.toString()
        viewModel.loginData["captcha"] = binding.captchaText.text.toString()
        viewModel.loginData["code"] = ""
        viewModel.tryLogin()
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

    private fun getCaptcha() {
        isGetCaptcha = true
        viewModel.timeStamp = System.currentTimeMillis()
        viewModel.getCaptcha()
    }

    private fun loginFailed(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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

}