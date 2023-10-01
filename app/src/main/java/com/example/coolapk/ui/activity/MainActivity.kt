package com.example.coolapk.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.coolapk.R
import com.example.coolapk.databinding.ActivityMainBinding
import com.example.coolapk.ui.fragment.BlankFragment
import com.example.coolapk.ui.fragment.home.HomeFragment
import com.example.coolapk.ui.fragment.minterface.IOnBottomClickContainer
import com.example.coolapk.ui.fragment.minterface.IOnBottomClickListener
import com.example.coolapk.util.CookieUtil.SESSID
import com.example.coolapk.util.CookieUtil.deviceCode
import com.example.coolapk.util.CookieUtil.token
import com.example.coolapk.util.TokenDeviceUtils
import com.example.coolapk.util.TokenDeviceUtils.Companion.getTokenV2
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity(), IOnBottomClickContainer {

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
                        1 -> BlankFragment()
                        2 -> BlankFragment()
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
                binding.bottomNav?.menu?.getItem(position)?.isChecked = true
                binding.navRail?.menu?.getItem(position)?.isChecked = true
            }
        })

        binding.bottomNav?.setOnItemSelectedListener {
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

        binding.navRail?.setOnItemSelectedListener {
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
        deviceCode = TokenDeviceUtils.getLastingDeviceCode(this)

        token = deviceCode.getTokenV2()
    }

    private fun requestData() {
        thread {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://api.coolapk.com/v6/account/checkLoginInfo")
                    .addHeader("X-Requested-With", "XMLHttpRequest")
                    .addHeader("X-App-Id", "com.coolapk.market")
                    .addHeader(
                        "X-App-Device",
                        "wMxASdvl1ciJGbv92QgsDM2gTOH1STTByOn5Wdz1WYzByOn5Wdz1WYzByO3AjO4UjOxkjOCNkOBZkO2kDI7AyOgsjYkRmZ4MmNxADN0YWYllDZ"
                    )
                    .addHeader(
                        "X-App-Token",
                        "v2JDJhJDEwJE1TNDJPVFl3TXpRNE1rVTUvN2M4MXVDTHMua2NyTWFEV09RbXJVUFZWSm5FTzlCU0ZVOS5T"
                    )
                    .addHeader("Cookie", SESSID)
                    .addHeader("Cookie", "token=deleted")
                    .build()
                val response = client.newCall(request).execute()
                //val responseData = response.body
                val headers = response.headers
                val cookies = headers.values("Set-Cookie");
                val session = cookies[0]
                val sessionID = session.substring(0, session.indexOf(";"))
                SESSID = sessionID
                // Toast.makeText(this, SESSID, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

}