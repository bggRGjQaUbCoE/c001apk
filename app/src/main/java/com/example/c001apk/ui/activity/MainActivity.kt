package com.example.c001apk.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.widget.ThemeUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityMainBinding
import com.example.c001apk.ui.fragment.MessageFragment
import com.example.c001apk.ui.fragment.home.HomeFragment
import com.example.c001apk.ui.fragment.minterface.IOnBottomClickContainer
import com.example.c001apk.ui.fragment.minterface.IOnBottomClickListener
import com.example.c001apk.ui.fragment.settings.SettingsFragment
import com.example.c001apk.util.CookieUtil.SESSID
import com.example.c001apk.util.PrefManager
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.badge.BadgeDrawable
import java.net.URLEncoder


class MainActivity : BaseActivity(), IOnBottomClickContainer {

    private lateinit var binding: ActivityMainBinding
    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }

    override var controller: IOnBottomClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (viewModel.isInit) {
            viewModel.isInit = false
            viewModel.isNew = true
            genData()
        }

        if (viewModel.badge != 0)
            setBadge()

        binding.viewPager.apply {
            adapter = object : FragmentStateAdapter(this@MainActivity) {
                override fun getItemCount() = 3
                override fun createFragment(position: Int): Fragment {
                    return when (position) {
                        0 -> HomeFragment()
                        1 -> MessageFragment()
                        2 -> SettingsFragment()
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

        viewModel.checkLoginInfoData.observe(this) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val response = result.getOrNull()
                response?.let {
                    response.body()?.let {
                        if (response.body()?.data?.token != null) {
                            val login = response.body()?.data!!
                            viewModel.badge = login.notifyCount.badge
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

                        val headers = response.headers()
                        val cookies = headers.values("Set-Cookie")
                        val session = cookies[0]
                        val sessionID = session.substring(0, session.indexOf(";"))
                        SESSID = sessionID

                        if (viewModel.badge != 0)
                            setBadge()

                    }
                }
            }
        }

    }

    private fun genData() {
        viewModel.getCheckLoginInfo()
    }

    @SuppressLint("RestrictedApi")
    private fun setBadge() {
        val badge = binding.bottomNav.getOrCreateBadge(R.id.navigation_message)
        badge.number = viewModel.badge
        badge.backgroundColor =
            ThemeUtils.getThemeAttrColor(this, rikka.preference.simplemenu.R.attr.colorPrimary)
        //badge.badgeTextColor = ContextCompat.getColor(this,R.color.design_default_color_error)
        badge.badgeGravity = BadgeDrawable.TOP_END
        badge.verticalOffset = 5
        badge.horizontalOffset = 5
    }


}