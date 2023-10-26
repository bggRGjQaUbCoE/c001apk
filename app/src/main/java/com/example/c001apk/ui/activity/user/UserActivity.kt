package com.example.c001apk.ui.activity.user

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.ThemeUtils
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityUserBinding
import com.example.c001apk.ui.activity.BaseActivity
import com.example.c001apk.ui.fragment.home.feed.HomeFeedAdapter
import com.example.c001apk.util.CountUtil
import com.example.c001apk.util.ImageShowUtil
import com.example.c001apk.util.LinearItemDecoration
import com.example.c001apk.util.PubDateUtil


class UserActivity : BaseActivity() {

    private lateinit var binding: ActivityUserBinding
    private val viewModel by lazy { ViewModelProvider(this)[UserViewModel::class.java] }
    private lateinit var mAdapter: HomeFeedAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private var firstCompletelyVisibleItemPosition = 0
    private var lastVisibleItemPosition = 0

    @SuppressLint(
        "ResourceAsColor", "SetTextI18n", "NotifyDataSetChanged", "UseCompatLoadingForDrawables",
        "RestrictedApi", "ResourceType"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        initView()
        initData()
        initRefresh()
        initScroll()

        viewModel.userData.observe(this) { result ->
            val user = result.getOrNull()
            if (user != null) {
                binding.collapsingToolbar.title = user.username
                binding.collapsingToolbar.setCollapsedTitleTextColor(this.getColor(R.color.white))
                binding.collapsingToolbar.setExpandedTitleColor(this.getColor(com.google.android.material.R.color.mtrl_btn_transparent_bg_color))
                ImageShowUtil.showUserCover(binding.cover, user.cover)
                ImageShowUtil.showAvatar(binding.avatar, user.userAvatar)
                binding.name.text = user.username
                binding.level.text = "Lv.${user.level}"
                binding.level.visibility = View.VISIBLE
                if (user.bio == "") binding.bio.visibility = View.GONE
                else binding.bio.text = user.bio
                binding.like.text = "${CountUtil.view(user.beLikeNum)} 获赞"
                binding.follow.text = "${CountUtil.view(user.follow)} 关注"
                binding.fans.text = "${CountUtil.view(user.fans)} 粉丝"
                binding.loginTime.text = PubDateUtil.time(user.logintime) + "活跃"

                viewModel.uid = user.uid
                viewModel.isRefreh = true
                viewModel.getUserFeed()
            } else {
                result.exceptionOrNull()?.printStackTrace()
            }
        }

        viewModel.userFeedData.observe(this) { result ->
            val feed = result.getOrNull()
            if (!feed.isNullOrEmpty()) {
                if (viewModel.isRefreh) viewModel.feedContentList.clear()
                if (viewModel.isRefreh || viewModel.isLoadMore) {
                    for (element in feed) {
                        if (element.entityTemplate == "feed")
                            viewModel.feedContentList.add(element)
                    }
                }
                mAdapter.notifyDataSetChanged()
                mAdapter.setLoadState(mAdapter.LOADING_COMPLETE)
            } else {
                mAdapter.setLoadState(mAdapter.LOADING_END)
                viewModel.isEnd = true
                result.exceptionOrNull()?.printStackTrace()
            }
            binding.infoLayout.visibility = View.VISIBLE
            binding.indicator.isIndeterminate = false
            binding.swipeRefresh.isRefreshing = false
            viewModel.isRefreh = false
        }

    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (lastVisibleItemPosition == viewModel.feedContentList.size) {
                        if (!viewModel.isEnd) {
                            mAdapter.setLoadState(mAdapter.LOADING)
                            viewModel.isLoadMore = true
                            viewModel.page++
                            viewModel.getUserFeed()
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

    @SuppressLint("RestrictedApi")
    private fun initRefresh() {
        binding.swipeRefresh.setColorSchemeColors(
            ThemeUtils.getThemeAttrColor(
                this, rikka.preference.simplemenu.R.attr.colorPrimary
            )
        )
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.page = 1
            viewModel.isRefreh = true
            viewModel.isEnd = false
            viewModel.getUserFeed()
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
        viewModel.id = intent.getStringExtra("id")!!
        viewModel.getUser()
        viewModel.isInit = false
    }

    private fun initView() {
        val space = resources.getDimensionPixelSize(R.dimen.normal_space)
        mAdapter = HomeFeedAdapter(this, viewModel.feedContentList)
        mLayoutManager = LinearLayoutManager(this)
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
            if (itemDecorationCount == 0) addItemDecoration(LinearItemDecoration(space))
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }
}