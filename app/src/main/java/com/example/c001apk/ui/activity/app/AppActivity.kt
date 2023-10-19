package com.example.c001apk.ui.activity.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.TypedValue
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityAppBinding
import com.example.c001apk.ui.fragment.home.feed.HomeFeedAdapter
import com.example.c001apk.util.ImageShowUtil
import com.example.c001apk.util.LinearItemDecoration
import com.example.c001apk.util.PubDateUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AppActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppBinding
    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var mAdapter: HomeFeedAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private var firstCompletelyVisibleItemPosition = 0
    private var lastVisibleItemPosition = 0

    /**
     * 获取状态栏高度
     * @return
     */
    @SuppressLint("DiscouragedApi", "InternalInsetResource")
    fun getStatusBarHeight(): Int {
        var result = 0
        //获取状态栏高度的资源id
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }


    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        /*val tv = TypedValue()
        var actionBarHeight = 0
        if (this.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight =
                TypedValue.complexToDimensionPixelSize(tv.data, this.resources.displayMetrics)
        }

        val mediumSpace = resources.getDimensionPixelSize(R.dimen.medium_space)
        val minorSpace = resources.getDimensionPixelSize(R.dimen.minor_space)
        binding.appLayout.setPadding(
            mediumSpace,
            actionBarHeight,
            mediumSpace,
            minorSpace
        )//px*/

        initView()
        initData()
        initRefresh()
        initScroll()

        viewModel.appInfoData.observe(this) { result ->
            val appInfo = result.getOrNull()
            if (appInfo != null) {
                binding.name.text = appInfo.title
                binding.version.text = "版本: ${appInfo.version}(${appInfo.apkversioncode})"
                binding.size.text = "大小: ${appInfo.apksize}"
                if (appInfo.lastupdate == null)
                    binding.updateTime.text = "更新时间: null"
                else
                    binding.updateTime.text = "更新时间: ${PubDateUtil.time(appInfo.lastupdate)}"
                binding.collapsingToolbar.title = appInfo.title
                binding.collapsingToolbar.setExpandedTitleColor(this.getColor(com.google.android.material.R.color.mtrl_btn_transparent_bg_color))
                ImageShowUtil.showIMG(binding.logo, appInfo.logo)

                binding.progress.isIndeterminate = false
                viewModel.appId = appInfo.id
                viewModel.isRefreh = true
                binding.swipeRefresh.isRefreshing = true
                lifecycleScope.launch {
                    delay(500)
                    viewModel.getAppComment()
                }
            } else {
                result.exceptionOrNull()?.printStackTrace()
            }
        }

        viewModel.appCommentData.observe(this) { result ->
            val comment = result.getOrNull()
            if (!comment.isNullOrEmpty()) {
                if (viewModel.isRefreh)
                    viewModel.appCommentList.clear()
                if (viewModel.isRefreh || viewModel.isLoadMore) {
                    for (element in comment) {
                        if (element.entityType == "feed")
                            viewModel.appCommentList.add(element)
                    }
                }
                mAdapter.notifyDataSetChanged()
                binding.swipeRefresh.isRefreshing = false
                viewModel.isRefreh = false
            } else {
                viewModel.isRefreh = false
                viewModel.isEnd = true
                binding.swipeRefresh.isRefreshing = false
                result.exceptionOrNull()?.printStackTrace()
            }
        }


    }

    private fun initView() {
        val space = resources.getDimensionPixelSize(R.dimen.normal_space)
        mAdapter = HomeFeedAdapter(this, viewModel.appCommentList)
        mLayoutManager = LinearLayoutManager(this)
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
            if (itemDecorationCount == 0)
                addItemDecoration(LinearItemDecoration(space))
        }
    }

    private fun initData() {
        if (viewModel.isInit) {
            refreshData()
        }
    }

    private fun refreshData() {
        viewModel.page = 1
        viewModel.isRefreh = true
        viewModel.isEnd = false
        val id = intent.getStringExtra("id")!!
        viewModel.id = id
        viewModel.getAppInfo()
        viewModel.isInit = false
    }

    private fun initRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.page = 1
            viewModel.isRefreh = true
            viewModel.isEnd = false
            viewModel.getAppComment()
        }
    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (lastVisibleItemPosition == viewModel.appCommentList.size - 1) {
                        if (!viewModel.isEnd) {
                            viewModel.isLoadMore = true
                            viewModel.page++
                            viewModel.getAppComment()
                        }
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition()
                firstCompletelyVisibleItemPosition =
                    mLayoutManager.findFirstCompletelyVisibleItemPosition()
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }

}