package com.example.c001apk.ui.activity.user

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityUserBinding
import com.example.c001apk.ui.fragment.home.feed.HomeFeedAdapter
import com.example.c001apk.util.ImageShowUtil
import com.example.c001apk.util.LinearItemDecoration
import com.example.c001apk.util.PubDateUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserBinding
    private val viewModel by lazy { ViewModelProvider(this)[UserViewModel::class.java] }
    private lateinit var mAdapter: HomeFeedAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private var firstCompletelyVisibleItemPosition = 0
    private var lastVisibleItemPosition = 0

    @SuppressLint("ResourceAsColor", "SetTextI18n", "NotifyDataSetChanged",
        "UseCompatLoadingForDrawables"
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
                ImageShowUtil.showIMG(binding.cover, user.cover)
                //binding.cover.foreground = this.getDrawable(R.color.user_cover)
                ImageShowUtil.showAvatar(binding.avatar, user.userAvatar)
                binding.name.text = user.username
                binding.level.text = "Lv.${user.level}"
                binding.level.visibility = View.VISIBLE
                binding.bio.text = user.bio
                binding.like.text = "${user.beLikeNum} 获赞"
                binding.follow.text = "${user.follow} 关注"
                binding.fans.text = "${user.fans} 粉丝"
                binding.loginTime.text = PubDateUtil.time(user.logintime) + "活跃"

                binding.progress.isIndeterminate = false
                viewModel.uid = user.uid
                viewModel.isRefreh = true
                binding.swipeRefresh.isRefreshing = true
                lifecycleScope.launch {
                    delay(500)
                    viewModel.getUserFeed()
                }
            } else {
                result.exceptionOrNull()?.printStackTrace()
            }
        }

        viewModel.userFeedData.observe(this) { result ->
            val feed = result.getOrNull()
            if (!feed.isNullOrEmpty()) {
                if (viewModel.isRefreh)
                    viewModel.feedContentList.clear()
                if (viewModel.isRefreh || viewModel.isLoadMore) {
                    for (element in feed) {
                        if (element.entityType == "feed")
                            viewModel.feedContentList.add(element)
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

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (lastVisibleItemPosition == viewModel.feedContentList.size - 1) {
                        if (!viewModel.isEnd) {
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

    private fun initRefresh() {
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
        val id = intent.getStringExtra("id")!!
        viewModel.id = id
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
            if (itemDecorationCount == 0)
                addItemDecoration(LinearItemDecoration(space))
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }
}