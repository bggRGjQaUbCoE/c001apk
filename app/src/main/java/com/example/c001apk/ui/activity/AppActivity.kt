package com.example.c001apk.ui.activity

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityAppBinding
import com.example.c001apk.ui.fragment.AppFragment
import com.example.c001apk.ui.fragment.minterface.IOnTabClickContainer
import com.example.c001apk.ui.fragment.minterface.IOnTabClickListener
import com.example.c001apk.util.DateUtils
import com.example.c001apk.util.ImageUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator

class AppActivity : BaseActivity(), IOnTabClickContainer {

    private lateinit var binding: ActivityAppBinding
    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var subscribe: MenuItem
    override var tabController: IOnTabClickListener? = null

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        if (viewModel.title != "") {
            showAppInfo()
            binding.appLayout.visibility = View.VISIBLE
        } else if (viewModel.errorMessage != null) {
            showErrorMessage()
        }
        initData()

        viewModel.appInfoData.observe(this) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val appInfo = result.getOrNull()
                if (appInfo?.message != null) {
                    viewModel.errorMessage = appInfo.message
                    binding.indicator.isIndeterminate = false
                    binding.indicator.visibility = View.GONE
                    showErrorMessage()
                    return@observe
                } else if (appInfo?.data != null) {
                    viewModel.isFollow = appInfo.data.userAction?.follow == 1
                    viewModel.commentStatusText = appInfo.data.commentStatusText
                    viewModel.title = appInfo.data.title
                    viewModel.version =
                        "版本: ${appInfo.data.version}(${appInfo.data.apkversioncode})"
                    viewModel.size = "大小: ${appInfo.data.apksize}"
                    viewModel.lastupdate = if (appInfo.data.lastupdate == null) "更新时间: null"
                    else "更新时间: ${DateUtils.fromToday(appInfo.data.lastupdate)}"
                    viewModel.logo = appInfo.data.logo
                    viewModel.appId = appInfo.data.id
                    viewModel.packageName = appInfo.data.apkname
                    viewModel.versionCode = appInfo.data.apkversioncode
                    viewModel.type = appInfo.data.entityType
                    showAppInfo()

                    if (viewModel.commentStatusText == "允许评论" || viewModel.type == "appForum") {
                        viewModel.tabList.apply {
                            add("最近回复")
                            add("热度排序")
                            add("最新发布")
                        }
                        viewModel.fragmentList.apply {
                            add(AppFragment.newInstance("reply", viewModel.id))
                            add(AppFragment.newInstance("hot", viewModel.id))
                            add(AppFragment.newInstance("pub", viewModel.id))
                        }
                        initView()
                    } else {
                        viewModel.errorMessage = appInfo.data.commentStatusText
                        showErrorMessage()
                    }
                    binding.indicator.isIndeterminate = false
                    binding.indicator.visibility = View.GONE
                    binding.appLayout.visibility = View.VISIBLE
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

        viewModel.getFollowData.observe(this) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val response = result.getOrNull()
                if (response != null) {
                    response.data?.follow?.let {
                        viewModel.isFollow = !viewModel.isFollow
                        initSub()
                        Toast.makeText(
                            this, if (response.data.follow == 1) "关注成功"
                            else "取消关注成功", Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

    }

    private fun initView() {
        binding.viewPager.offscreenPageLimit = viewModel.tabList.size
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): Fragment = viewModel.fragmentList[position]
            override fun getItemCount() = viewModel.tabList.size
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = viewModel.tabList[position]
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {
                tabController?.onReturnTop(null)
            }

        })
    }

    private fun showErrorMessage() {
        binding.tabLayout.visibility = View.GONE
        binding.errorMessage.visibility = View.VISIBLE
        binding.errorMessage.text = viewModel.errorMessage
    }

    private fun showAppInfo() {
        binding.name.text = viewModel.title
        binding.version.text = viewModel.version
        binding.size.text = viewModel.size
        binding.updateTime.text = viewModel.lastupdate
        binding.collapsingToolbar.title = viewModel.title
        binding.collapsingToolbar.setExpandedTitleColor(this.getColor(com.google.android.material.R.color.mtrl_btn_transparent_bg_color))
        ImageUtil.showIMG(binding.logo, viewModel.logo)
        if (viewModel.type == "apk")
            viewModel.getDownloadLink()
        viewModel.downloadLinkData.observe(this@AppActivity) { result ->
            val link = result.getOrNull()
            if (link != null) {
                binding.btnDownload.visibility = View.VISIBLE
                viewModel.collectionUrl = link
            } else {
                result.exceptionOrNull()?.printStackTrace()
            }
        }
        binding.btnDownload.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(viewModel.collectionUrl))
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this@AppActivity, "打开失败", Toast.LENGTH_SHORT).show()
                Log.w("error", "Activity was not found for intent, $intent")
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initData() {
        if (viewModel.isInit) {
            viewModel.isInit = false
            binding.indicator.isIndeterminate = true
            binding.indicator.visibility = View.VISIBLE
            refreshData()
        } else if (viewModel.tabList.isNotEmpty()) {
            initView()
        } else if (viewModel.commentStatusText != "允许评论" && viewModel.type != "appForum") {
            showErrorMessage()
        }
    }

    private fun refreshData() {
        viewModel.id = intent.getStringExtra("id")!!
        viewModel.isNew = true
        viewModel.getAppInfo()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.topic_product_menu, menu)
        subscribe = menu!!.findItem(R.id.subscribe)
        subscribe.isVisible = PrefManager.isLogin
        menu.findItem(R.id.order).isVisible = false
        return true
    }

    private fun initSub() {
        subscribe.title = if (viewModel.isFollow) "取消关注"
        else "关注"
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        initSub()
        return super.onPrepareOptionsMenu(menu)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()

            R.id.search -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra("pageType", "apk")
                intent.putExtra("pageParam", viewModel.appId)
                intent.putExtra("title", viewModel.title)
                startActivity(intent)
            }

            R.id.subscribe -> {
                viewModel.isNew = true
                viewModel.url = if (viewModel.isFollow) "/v6/apk/unFollow"
                else "/v6/apk/follow"
                viewModel.fid = viewModel.appId
                viewModel.getFollow()
            }
        }
        return true
    }

}