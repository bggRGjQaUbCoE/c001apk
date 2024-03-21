package com.example.c001apk.ui.follow

import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.adapter.AppAdapter
import com.example.c001apk.adapter.FooterAdapter
import com.example.c001apk.adapter.FooterState
import com.example.c001apk.adapter.HeaderAdapter
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.databinding.ActivityFfflistBinding
import com.example.c001apk.ui.base.BaseActivity
import com.example.c001apk.ui.home.IOnTabClickContainer
import com.example.c001apk.ui.home.IOnTabClickListener
import com.example.c001apk.util.PrefManager
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.view.StaggerItemDecoration
import com.google.android.material.color.MaterialColors
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FFFListActivity : BaseActivity<ActivityFfflistBinding>(), IOnTabClickContainer {

    private val viewModel by viewModels<FollowViewModel>()
    private lateinit var mAdapter: AppAdapter
    private lateinit var footerAdapter: FooterAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var sLayoutManager: StaggeredGridLayoutManager
    override var tabController: IOnTabClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.appBar.setLiftable(true)

        viewModel.uid = intent.getStringExtra("uid")
        viewModel.type = intent.getStringExtra("type")
        viewModel.isEnable = intent.getBooleanExtra("isEnable", false)

        initBar()
        if (viewModel.isEnable) {
            binding.tabLayout.isVisible = true
            binding.viewPager.isVisible = true
            binding.swipeRefresh.isVisible = false
            binding.recyclerView.isVisible = false
            if (viewModel.tabList.isEmpty()) {
                if (viewModel.type == "follow") {
                    viewModel.tabList.apply {
                        add("用户")
                        add("话题")
                        add("数码")
                        add("应用")
                    }
                } else if (viewModel.type == "reply") {
                    viewModel.tabList.apply {
                        add("我的回复")
                        add("我收到的回复")
                    }
                }
            }
            initViewPager()
        } else {
            binding.tabLayout.isVisible = false
            binding.viewPager.isVisible = false
            binding.swipeRefresh.isVisible = true
            binding.recyclerView.isVisible = true
            initView()
            viewModel.loadingState.value = LoadingState.Loading
            initRefresh()
            initScroll()
            initObserve()
            initError()
        }

    }

    private fun initError() {
        binding.errorLayout.retry.setOnClickListener {
            binding.errorLayout.parent.isVisible = false
            viewModel.loadingState.value = LoadingState.Loading
        }
    }

    override fun onResume() {
        super.onResume()
        if (!viewModel.isEnable)
            initLift()
    }

    override fun onStart() {
        super.onStart()
        if (!viewModel.isEnable)
            initLift()
    }

    private fun initLift() {
        binding.appBar.setLifted(
            !binding.recyclerView.borderViewDelegate.isShowingTopBorder
        )
        binding.recyclerView.borderViewDelegate
            .setBorderVisibilityChangedListener { top, _, _, _ ->
                binding.appBar.setLifted(!top)
            }
    }

    override fun onStop() {
        super.onStop()
        detachLift()
    }

    override fun onPause() {
        super.onPause()
        detachLift()
    }

    private fun detachLift() {
        binding.recyclerView.borderViewDelegate.borderVisibilityChangedListener = null
    }

    private fun initObserve() {
        viewModel.toastText.observe(this) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.loadingState.observe(this) {
            when (it) {
                LoadingState.Loading -> {
                    binding.indicator.parent.isIndeterminate = true
                    binding.indicator.parent.isVisible = true
                    refreshData()
                }

                LoadingState.LoadingDone -> {
                    binding.swipeRefresh.isEnabled = true
                }

                is LoadingState.LoadingError -> {
                    binding.errorMessage.errMsg.apply {
                        text = it.errMsg
                        isVisible = true
                    }
                }

                is LoadingState.LoadingFailed -> {
                    binding.errorLayout.apply {
                        msg.text = it.msg
                        parent.isVisible = true
                    }
                }
            }
            if (it !is LoadingState.Loading) {
                binding.indicator.parent.isIndeterminate = false
                binding.indicator.parent.isVisible = false
            }
        }
        viewModel.footerState.observe(this) {
            footerAdapter.setLoadState(it)
            if (it !is FooterState.Loading) {
                binding.swipeRefresh.isRefreshing = false
            }
        }

        viewModel.dataListData.observe(this) {
            viewModel.listSize = it.size
            mAdapter.submitList(it)
        }
    }

    private fun initViewPager() {
        binding.viewPager.offscreenPageLimit = viewModel.tabList.size
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int) =
                if (viewModel.type == "follow") {
                    when (position) {
                        0 -> FollowFragment.newInstance("follow")
                        1 -> FollowFragment.newInstance("topic")
                        2 -> FollowFragment.newInstance("product")
                        3 -> FollowFragment.newInstance("apk")
                        else -> throw IllegalArgumentException()
                    }
                } else {
                    when (position) {
                        0 -> FollowFragment.newInstance("reply")
                        1 -> FollowFragment.newInstance("replyToMe")
                        else -> throw IllegalArgumentException()
                    }
                }

            override fun getItemCount() = viewModel.tabList.size

        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = viewModel.tabList[position]
        }.attach()
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {
                tabController?.onReturnTop(null)
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }

    private fun initBar() {
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolBar.title = when (viewModel.type) {
            "feed" -> "我的动态"

            "follow" -> {
                if (viewModel.uid == PrefManager.uid)
                    "我的关注"
                else
                    "TA关注的人"
            }

            "fans" -> {
                if (viewModel.uid == PrefManager.uid)
                    "关注我的人"
                else
                    "TA的粉丝"
            }

            "like" -> "我的赞"

            "reply" -> "我的回复"

            "recentHistory" -> "我的常去"

            else -> viewModel.type

        }
    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                    if (viewModel.listSize != -1 && !viewModel.isEnd) {
                        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                            viewModel.lastVisibleItemPosition =
                                mLayoutManager.findLastVisibleItemPosition()
                        } else {
                            val positions = sLayoutManager.findLastVisibleItemPositions(null)
                            viewModel.lastVisibleItemPosition = positions[0]
                            positions.forEach { pos ->
                                if (pos > viewModel.lastVisibleItemPosition) {
                                    viewModel.lastVisibleItemPosition = pos
                                }
                            }
                        }
                    }

                    if (viewModel.lastVisibleItemPosition == viewModel.listSize + 1
                        && !viewModel.isEnd && !viewModel.isRefreshing && !viewModel.isLoadMore
                    ) {
                        loadMore()
                    }
                }
            }
        })
    }

    private fun loadMore() {
        viewModel.isLoadMore = true
        viewModel.fetchFeedList()
    }

    private fun initRefresh() {
        binding.swipeRefresh.apply {
            isEnabled = false
            setColorSchemeColors(
                MaterialColors.getColor(
                    this,
                    com.google.android.material.R.attr.colorPrimary,
                    0
                )
            )
            setOnRefreshListener {
                refreshData()
            }
        }
    }

    private fun initView() {
        mAdapter = AppAdapter(viewModel.repository, viewModel.ItemClickListener())
        footerAdapter = FooterAdapter(ReloadListener())
        binding.recyclerView.apply {
            adapter = ConcatAdapter(HeaderAdapter(), mAdapter, footerAdapter)
            layoutManager =
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    mLayoutManager = LinearLayoutManager(this@FFFListActivity)
                    mLayoutManager
                } else {
                    sLayoutManager =
                        StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                    sLayoutManager
                }
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                addItemDecoration(LinearItemDecoration(10.dp))
            else
                addItemDecoration(StaggerItemDecoration(10.dp))
        }
    }

    private fun refreshData() {
        viewModel.lastVisibleItemPosition = 0
        viewModel.lastItem = null
        viewModel.page = 1
        viewModel.isRefreshing = true
        viewModel.isEnd = false
        viewModel.fetchFeedList()
    }

    inner class ReloadListener : FooterAdapter.FooterListener {
        override fun onReLoad() {
            loadMore()
        }
    }

}
