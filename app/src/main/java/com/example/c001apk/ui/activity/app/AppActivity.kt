package com.example.c001apk.ui.activity.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.ThemeUtils
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityAppBinding
import com.example.c001apk.ui.activity.BaseActivity
import com.example.c001apk.ui.fragment.home.feed.HomeFeedAdapter
import com.example.c001apk.ui.fragment.home.feed.IOnLikeClickListener
import com.example.c001apk.util.ImageShowUtil
import com.example.c001apk.util.LinearItemDecoration
import com.example.c001apk.util.PubDateUtil

class AppActivity : BaseActivity(), IOnLikeClickListener {

    private lateinit var binding: ActivityAppBinding
    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var mAdapter: HomeFeedAdapter
    private lateinit var mLayoutManager: LinearLayoutManager

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

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
                viewModel.appId = appInfo.id
                viewModel.isRefreh = true
                viewModel.getAppComment()
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
                    for (element in comment)
                        if (element.entityType == "feed")
                            viewModel.appCommentList.add(element)

                }
                mAdapter.notifyDataSetChanged()
                mAdapter.setLoadState(mAdapter.LOADING_COMPLETE)
            } else {
                mAdapter.setLoadState(mAdapter.LOADING_END)
                viewModel.isEnd = true
                result.exceptionOrNull()?.printStackTrace()
            }
            binding.indicator.isIndeterminate = false
            binding.appLayout.visibility = View.VISIBLE
            binding.swipeRefresh.isRefreshing = false
            viewModel.isRefreh = false
        }

        viewModel.likeFeedData.observe(this) { result ->
            val response = result.getOrNull()
            if (response != null) {
                if (response.data != null) {
                    viewModel.appCommentList[viewModel.likePosition].likenum =
                        response.data.count
                    viewModel.appCommentList[viewModel.likePosition].userAction?.like = 1

                    mAdapter.notifyDataSetChanged()
                } else
                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
            } else {
                result.exceptionOrNull()?.printStackTrace()
            }
        }

        viewModel.unLikeFeedData.observe(this) { result ->
            val response = result.getOrNull()
            if (response != null) {
                if (response.data != null) {
                    viewModel.appCommentList[viewModel.likePosition].likenum =
                        response.data.count
                    viewModel.appCommentList[viewModel.likePosition].userAction?.like = 0

                    mAdapter.notifyDataSetChanged()
                } else
                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
            } else {
                result.exceptionOrNull()?.printStackTrace()
            }
        }


    }

    private fun initView() {
        val space = resources.getDimensionPixelSize(R.dimen.normal_space)
        mAdapter = HomeFeedAdapter(this, viewModel.appCommentList)
        mAdapter.setIOnLikeReplyListener(this)
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

    @SuppressLint("RestrictedApi")
    private fun initRefresh() {
        binding.swipeRefresh.setColorSchemeColors(
            ThemeUtils.getThemeAttrColor(
                this,
                rikka.preference.simplemenu.R.attr.colorPrimary
            )
        )
        binding.swipeRefresh.setOnRefreshListener {
            binding.indicator.isIndeterminate = false
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
                    if (viewModel.lastVisibleItemPosition == viewModel.appCommentList.size
                        && !viewModel.isEnd
                    ) {
                        mAdapter.setLoadState(mAdapter.LOADING)
                        viewModel.isLoadMore = true
                        viewModel.page++
                        viewModel.getAppComment()

                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (viewModel.appCommentList.isNotEmpty()) {
                    viewModel.lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition()
                    viewModel.firstCompletelyVisibleItemPosition =
                        mLayoutManager.findFirstCompletelyVisibleItemPosition()
                }
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }

    override fun onPostLike(isLike: Boolean, id: String, position: Int) {
        viewModel.likeFeedId = id
        viewModel.likePosition = position
        if (isLike)
            viewModel.postUnLikeFeed()
        else
            viewModel.postLikeFeed()
    }

}