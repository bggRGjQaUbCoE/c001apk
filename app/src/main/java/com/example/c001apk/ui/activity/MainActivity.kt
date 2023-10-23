package com.example.c001apk.ui.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.c001apk.R
import com.example.c001apk.constant.Constants
import com.example.c001apk.databinding.ActivityMainBinding
import com.example.c001apk.logic.model.CheckResponse
import com.example.c001apk.ui.fragment.home.HomeFragment
import com.example.c001apk.ui.fragment.meaasge.MessageFragment
import com.example.c001apk.ui.fragment.minterface.IOnBottomClickContainer
import com.example.c001apk.ui.fragment.minterface.IOnBottomClickListener
import com.example.c001apk.ui.fragment.settings.SettingsFragment
import com.example.c001apk.ui.fragment.settings.SettingsPreferenceFragment
import com.example.c001apk.util.CookieUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.TokenDeviceUtils
import com.example.c001apk.util.TokenDeviceUtils.Companion.getTokenV2
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.concurrent.thread

class MainActivity : BaseActivity(), IOnBottomClickContainer {

    private lateinit var binding: ActivityMainBinding

    override var controller: IOnBottomClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        genData()
        requestData()

        binding.viewPager.apply {
            adapter = object : FragmentStateAdapter(this@MainActivity) {
                override fun getItemCount() = 3
                override fun createFragment(position: Int): Fragment {
                    return when (position) {
                        0 -> HomeFragment()
                        1 -> MessageFragment()
                        2 -> SettingsPreferenceFragment()
                        else -> HomeFragment()
                    }
                }
            }
            isUserInputEnabled = false
        }

        binding.viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.bottomNav.menu.getItem(position)?.isChecked = true
            }
        })

        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    if (binding.viewPager.currentItem == 0)
                        controller?.onReturnTop()
                    else
                        binding.viewPager.setCurrentItem(0, true)
                }

                R.id.navigation_message -> binding.viewPager.setCurrentItem(1, true)
                R.id.navigation_setting -> binding.viewPager.setCurrentItem(2, true)
            }
            true
        }

    }

    private fun genData() {
        CookieUtil.deviceCode = TokenDeviceUtils.getLastingDeviceCode(this)
        CookieUtil.token = CookieUtil.deviceCode.getTokenV2()
    }

    private fun requestData() {
        thread {
            try {
                val client = OkHttpClient()
                val builder = Request.Builder().apply {
                    url("https://api.coolapk.com/v6/account/checkLoginInfo")
                    if (PrefManager.isLogin)
                        addHeader(
                            "Cookie",
                            "${PrefManager.uid}; ${PrefManager.name}; ${PrefManager.token}"
                        )
                    addHeader("User-Agent", Constants.USER_AGENT)
                    addHeader("X-Requested-With", Constants.REQUEST_WIDTH)
                    addHeader("X-Sdk-Int", "33")
                    addHeader("X-Sdk-Locale", "zh-CN")
                    addHeader("X-App-Id", Constants.APP_ID)
                    addHeader("X-App-Token", CookieUtil.token)
                    addHeader("X-App-Version", "13.3.1")
                    addHeader("X-App-Code", "2307121")
                    addHeader("X-Api-Version", "13")
                    addHeader("X-App-Device", CookieUtil.deviceCode)
                    addHeader("X-Dark-Mode", "0")
                    addHeader("X-App-Channel", "coolapk")
                    addHeader("X-App-Mode", "universal")
                }
                val request = builder.build()

                val response = client.newCall(request).execute()
                val login: CheckResponse = Gson().fromJson(
                    response.body!!.string(),
                    CheckResponse::class.java
                )

                if (login.status == null) {
                    PrefManager.isLogin = true
                    /*runOnUiThread {
                        Toast.makeText(this@MainActivity, "login", Toast.LENGTH_SHORT).show()
                    }*/
                    PrefManager.uid = "uid=${login.data!!.uid}"
                    PrefManager.name = "username=${login.data.username}"
                    PrefManager.token = "token=${login.data.token}"
                    PrefManager.userAvatar = login.data.userAvatar
                } else {
                    PrefManager.isLogin = false
                    /*runOnUiThread {
                        Toast.makeText(this@MainActivity, "no login", Toast.LENGTH_SHORT).show()
                    }*/
                    //PrefManager.uid = ""
                    //PrefManager.name = ""
                    //PrefManager.token = ""
                }

                val headers = response.headers
                val cookies = headers.values("Set-Cookie");
                val session = cookies[0]
                val sessionID = session.substring(0, session.indexOf(";"))
                CookieUtil.SESSID = sessionID

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}