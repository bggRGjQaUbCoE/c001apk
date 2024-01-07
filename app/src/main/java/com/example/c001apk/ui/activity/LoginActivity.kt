package com.example.c001apk.ui.activity

import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.text.Spanned
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityLoginBinding
import com.example.c001apk.logic.model.LoginResponse
import com.example.c001apk.util.ActivityCollector
import com.example.c001apk.util.CookieUtil.SESSID
import com.example.c001apk.util.CookieUtil.isGetCaptcha
import com.example.c001apk.util.CookieUtil.isGetLoginParam
import com.example.c001apk.util.CookieUtil.isGetSmsLoginParam
import com.example.c001apk.util.CookieUtil.isGetSmsToken
import com.example.c001apk.util.CookieUtil.isPreGetLoginParam
import com.example.c001apk.util.CookieUtil.isTryLogin
import com.example.c001apk.util.LoginUtils.Companion.createRandomNumber
import com.example.c001apk.util.LoginUtils.Companion.createRequestHash
import com.example.c001apk.util.PrefManager
import com.example.c001apk.viewmodel.AppViewModel
import com.google.gson.Gson
import org.jsoup.Jsoup


class LoginActivity : BaseActivity<ActivityLoginBinding>() {

    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
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

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        isPreGetLoginParam = true
        viewModel.preGetLoginParam()

        viewModel.smsLoginParamData.observe(this) { result ->
            val response = result.getOrNull()
            val body = response?.body()?.string()
            body?.apply {
                viewModel.requestHash = Jsoup.parse(this).createRequestHash()
            }
            response?.apply {
                try {
                    val cookies = response.headers().values("Set-Cookie")
                    val session = cookies[0]
                    val sessionID = session.substring(0, session.indexOf(";"))
                    SESSID = sessionID
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "无法获取cookie", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        }

        viewModel.getSmsTokenData.observe(this) { result ->
            val response = result.getOrNull()
            response?.apply {
                viewModel.key = response.headers().values("Location").toString()
            }
        }

        viewModel.preGetLoginParamData.observe(this) { result ->
            val response = result.getOrNull()
            val body = response?.body()?.string()

            body?.apply {
                viewModel.requestHash = Jsoup.parse(this).createRequestHash()
            }
            response?.apply {
                try {
                    val cookies = response.headers().values("Set-Cookie")
                    val session = cookies[0]
                    val sessionID = session.substring(0, session.indexOf(";"))
                    SESSID = sessionID
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "无法获取cookie", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                    return@observe
                }
                isGetLoginParam = true
                viewModel.getLoginParam()
            }
        }

        viewModel.loginParamData.observe(this) { result ->
            val response = result.getOrNull()
            val body = response?.body()?.string()
            body?.apply {
                viewModel.requestHash = Jsoup.parse(this).createRequestHash()
            }
            response?.apply {
                try {
                    val cookies = response.headers().values("Set-Cookie")
                    val session = cookies[0]
                    val sessionID = session.substring(0, session.indexOf(";"))
                    SESSID = sessionID
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "无法获取cookie", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                    return@observe
                }
            }
        }

        viewModel.tryLoginData.observe(this) { result ->
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
                    viewModel.uid = uid
                    viewModel.getProfile()
                } else {
                    Toast.makeText(this, login.message, Toast.LENGTH_SHORT).show()
                    if (login.message == "图形验证码不能为空") {
                        binding.captcha.visibility = View.VISIBLE
                        getCaptcha()
                    } else if (login.message == "图形验证码错误") {
                        getCaptcha()
                    } else if (login.message == "密码错误" && binding.captcha.visibility == View.VISIBLE) {
                        getCaptcha()
                    }

                }
            }
        }

        viewModel.captchaData.observe(this) { result ->
            val response = result.getOrNull()
            response?.let {
                val responseBody = response.body()
                val bitmap = BitmapFactory.decodeStream(responseBody!!.byteStream())
                binding.captchaImg.setImageBitmap(bitmap)
            }
        }

        viewModel.profileDataLiveData.observe(this) { result ->
            val data = result.getOrNull()
            data?.data.let {
                PrefManager.userAvatar = data?.data?.userAvatar.toString()
                PrefManager.level = data?.data?.level.toString()
                PrefManager.experience = data?.data?.experience.toString()
                PrefManager.nextLevelExperience = data?.data?.nextLevelExperience.toString()
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
            else if (binding.account.text.toString().length != 11)
                Toast.makeText(this, "手机号不合规", Toast.LENGTH_SHORT).show()
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
        viewModel.loginData["requestHash"] = viewModel.requestHash
        viewModel.loginData["login"] = binding.account.text.toString()
        viewModel.loginData["password"] = binding.password.text.toString()
        viewModel.loginData["captcha"] = binding.captchaText.text.toString()
        viewModel.loginData["code"] = ""
        viewModel.tryLogin()
    }

    /*override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.login_menu, menu)
        return true
    }*/

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()

            R.id.loginPass -> {
                isLoginPass = true
                binding.account.inputType = InputType.TYPE_CLASS_TEXT
                binding.account.filters = arrayOf(LengthFilter(99), filter)
                binding.passLayout.visibility = View.VISIBLE
                binding.smsLayout.visibility = View.GONE
                binding.getSMS.visibility = View.GONE
            }

            R.id.loginPhone -> {
                isLoginPass = false
                binding.account.inputType = InputType.TYPE_CLASS_NUMBER
                binding.account.filters = arrayOf(LengthFilter(11), filter)
                binding.getSMS.visibility = View.VISIBLE
                binding.smsLayout.visibility = View.VISIBLE
                binding.passLayout.visibility = View.GONE
                isGetSmsLoginParam = true
                //viewModel.getSmsLoginParam()
            }
        }
        return true
    }

    private fun getSMS() {
        isGetSmsToken = true
        viewModel.getSmsData["submit"] = "1"
        viewModel.getSmsData["requestHash"] = viewModel.requestHash
        viewModel.getSmsData["country"] = "86"
        viewModel.getSmsData["mobile"] = binding.account.text.toString()
        viewModel.getSmsData["captcha"] = binding.captchaText.text.toString()
        viewModel.getSmsData["randomNumber"] = createRandomNumber()
        viewModel.getSmsToken()
    }

    private fun getCaptcha() {
        isGetCaptcha = true
        viewModel.timeStamp = System.currentTimeMillis()
        viewModel.getCaptcha()
    }

    private fun afterLogin() {
        ActivityCollector.recreateActivity(MainActivity::class.java.name)
        Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show()
        finish()
    }

}