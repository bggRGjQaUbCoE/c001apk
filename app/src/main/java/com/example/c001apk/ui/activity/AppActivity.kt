package com.example.c001apk.ui.activity

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityAppBinding
import com.example.c001apk.ui.fragment.AppFragment
import com.example.c001apk.ui.fragment.minterface.IOnTabClickContainer
import com.example.c001apk.ui.fragment.minterface.IOnTabClickListener
import com.example.c001apk.util.DateUtils
import com.example.c001apk.util.ImageUtil
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.TopicBlackListUtil
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator

class AppActivity : BaseActivity<ActivityAppBinding>(), IOnTabClickContainer {

    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private var subscribe: MenuItem? = null
    override var tabController: IOnTabClickListener? = null

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        if (!viewModel.title.isNullOrEmpty()) {
            showAppInfo()
            binding.appLayout.visibility = View.VISIBLE
        } else if (viewModel.errorMessage != null) {
            showErrorMessage()
        }
        initData()

        binding.errorLayout.retry.setOnClickListener {
            binding.errorLayout.parent.visibility = View.GONE
            binding.indicator.parent.visibility = View.VISIBLE
            binding.indicator.parent.isIndeterminate = true
            refreshData()
        }

        viewModel.appInfoData.observe(this) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val appInfo = result.getOrNull()
                if (appInfo?.message != null) {
                    viewModel.errorMessage = appInfo.message
                    binding.indicator.parent.isIndeterminate = false
                    binding.indicator.parent.visibility = View.GONE
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
                            add("最新发布")
                            add("热度排序")
                        }
                        initView()
                    } else {
                        viewModel.errorMessage = appInfo.data.commentStatusText
                        showErrorMessage()
                    }

                    binding.appLayout.visibility = View.VISIBLE
                } else {
                    binding.errorLayout.parent.visibility = View.VISIBLE
                    result.exceptionOrNull()?.printStackTrace()
                }
                binding.indicator.parent.isIndeterminate = false
                binding.indicator.parent.visibility = View.GONE
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
            override fun createFragment(position: Int) =
                when (position) {
                    0 -> AppFragment.newInstance("reply", viewModel.id.toString())
                    1 -> AppFragment.newInstance("pub", viewModel.id.toString())
                    2 -> AppFragment.newInstance("hot", viewModel.id.toString())
                    else -> throw IllegalArgumentException()
                }

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
        binding.errorMessage.parent.visibility = View.VISIBLE
        binding.errorMessage.parent.text = viewModel.errorMessage
    }

    private fun showAppInfo() {
        binding.name.text = viewModel.title
        binding.version.text = viewModel.version
        binding.size.text = viewModel.size
        binding.updateTime.text = viewModel.lastupdate
        binding.collapsingToolbar.title = viewModel.title
        binding.collapsingToolbar.setExpandedTitleColor(Color.TRANSPARENT)
        ImageUtil.showIMG(binding.logo, viewModel.logo)
        if (viewModel.type == "apk")
            binding.btnDownload.visibility = View.VISIBLE
        binding.btnDownload.setOnClickListener {
            if (viewModel.collectionUrl.isNullOrEmpty()) {
                viewModel.isNew = true
                viewModel.getDownloadLink()
            } else
                downloadApp()
        }
        viewModel.downloadLinkData.observe(this@AppActivity) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false
                val link = result.getOrNull()
                if (!link.isNullOrEmpty()) {
                    viewModel.collectionUrl = link
                    downloadApp()
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }
    }

    private fun downloadApp() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(viewModel.collectionUrl)))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this@AppActivity, "下载失败", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun initData() {
        if (viewModel.isInit) {
            viewModel.isInit = false
            binding.indicator.parent.isIndeterminate = true
            binding.indicator.parent.visibility = View.VISIBLE
            refreshData()
        } else if (viewModel.tabList.isEmpty()) {
            binding.errorLayout.parent.visibility = View.VISIBLE
        } else if (viewModel.tabList.isNotEmpty()) {
            initView()
        } else if (viewModel.commentStatusText != "允许评论" && viewModel.type != "appForum") {
            showErrorMessage()
        }
    }

    private fun refreshData() {
        viewModel.id = intent.getStringExtra("id")
        viewModel.isNew = true
        viewModel.getAppInfo()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.topic_product_menu, menu)
        subscribe = menu?.findItem(R.id.subscribe)
        subscribe?.isVisible = PrefManager.isLogin
        menu?.findItem(R.id.order)?.isVisible = false
        return true
    }

    private fun initSub() {
        subscribe?.title = if (viewModel.isFollow) "取消关注"
        else "关注"
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        initSub()
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()

            R.id.search -> {
                if (viewModel.appId.isNullOrEmpty() || viewModel.title.isNullOrEmpty()) {
                    Toast.makeText(this, "加载中...", Toast.LENGTH_SHORT).show()
                } else {
                    IntentUtil.startActivity<SearchActivity>(this) {
                        putExtra("pageType", "apk")
                        putExtra("pageParam", viewModel.appId)
                        putExtra("title", viewModel.title)
                    }
                }
            }

            R.id.subscribe -> {
                viewModel.isNew = true
                viewModel.followUrl = if (viewModel.isFollow) "/v6/apk/unFollow"
                else "/v6/apk/follow"
                viewModel.fid = viewModel.appId
                viewModel.getFollow()
            }

            R.id.block -> {
                MaterialAlertDialogBuilder(this).apply {
                    val title =
                        if (viewModel.type == "topic") viewModel.url.toString()
                            .replace("/t/", "")
                        else viewModel.title
                    setTitle("确定将 $title 加入黑名单？")
                    setNegativeButton(android.R.string.cancel, null)
                    setPositiveButton(android.R.string.ok) { _, _ ->
                        TopicBlackListUtil.saveTopic(viewModel.title.toString())
                    }
                    show()
                }
            }
        }
        return true
    }

}