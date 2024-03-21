package com.example.c001apk.ui.user

import android.content.res.Configuration
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.R
import com.example.c001apk.adapter.AppAdapter
import com.example.c001apk.adapter.FooterAdapter
import com.example.c001apk.adapter.FooterState
import com.example.c001apk.adapter.HeaderAdapter
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.databinding.ActivityUserBinding
import com.example.c001apk.ui.base.BaseActivity
import com.example.c001apk.ui.others.WebViewActivity
import com.example.c001apk.ui.search.SearchActivity
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.view.StaggerItemDecoration
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback


@AndroidEntryPoint
class UserActivity : BaseActivity<ActivityUserBinding>() {

    private val viewModel by viewModels<UserViewModel>(
        extrasProducer = {
            defaultViewModelCreationExtras.withCreationCallback<UserViewModel.Factory> { factory ->
                factory.create(uid = intent.getStringExtra("id").orEmpty())
            }
        }
    )
    private lateinit var mAdapter: AppAdapter
    private lateinit var footerAdapter: FooterAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var sLayoutManager: StaggeredGridLayoutManager
    private var menuBlock: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        initView()
        initFollowBtn()
        initData()
        initRefresh()
        initScroll()
        initObserve()

        binding.errorLayout.retry.setOnClickListener {
            binding.errorLayout.parent.isVisible = false
            viewModel.loadingState.value = LoadingState.Loading
        }

    }

    private fun initFollowBtn() {
        binding.followBtn.apply {
            isVisible = PrefManager.isLogin
            setOnClickListener {
                if (PrefManager.isLogin)
                    viewModel.onPostFollowUnFollow(
                        if (viewModel.userData?.isFollow == 1)
                            "/v6/user/unfollow"
                        else
                            "/v6/user/follow"
                    )
            }
        }
    }

    private fun initObserve() {
        viewModel.blockState.observe(this) { event ->
            event?.getContentIfNotHandledOrReturnNull()?.let {
                if (it)
                    menuBlock?.title = getMenuTitle("移除黑名单")
            }
        }

        viewModel.toastText.observe(this) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.followState.observe(this) {
            binding.followBtn.text = if (it == 1) "取消关注"
            else "关注"
        }

        viewModel.loadingState.observe(this) {
            when (it) {
                LoadingState.Loading -> {
                    binding.indicator.parent.isIndeterminate = true
                    binding.indicator.parent.isVisible = true
                    refreshData()
                }

                LoadingState.LoadingDone -> {
                    binding.userData = viewModel.userData
                    binding.listener = viewModel.ItemClickListener()
                    binding.infoLayout.isVisible = true
                    binding.swipeRefresh.isEnabled = true
                }

                is LoadingState.LoadingError -> {
                    binding.errorMessage.errMsg.text = it.errMsg
                    binding.errorMessage.errMsg.isVisible = true
                }

                is LoadingState.LoadingFailed -> {
                    binding.errorLayout.msg.text = it.msg
                    binding.errorLayout.parent.isVisible = true
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

        viewModel.feedData.observe(this) {
            viewModel.listSize = it.size
            mAdapter.submitList(it)
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
        viewModel.fetchUserFeed()
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

    private fun initData() {
        if (viewModel.isInit) {
            viewModel.isInit = false
            viewModel.loadingState.value = LoadingState.Loading
        }
    }

    private fun refreshData() {
        viewModel.lastVisibleItemPosition = 0
        viewModel.lastItem = null
        viewModel.page = 1
        viewModel.isRefreshing = true
        viewModel.isEnd = false
        viewModel.fetchUser()
    }

    private fun initView() {
        mAdapter = AppAdapter(viewModel.repository, viewModel.ItemClickListener())
        footerAdapter = FooterAdapter(ReloadListener())
        binding.recyclerView.apply {
            adapter = ConcatAdapter(HeaderAdapter(), mAdapter, footerAdapter)
            layoutManager =
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    mLayoutManager = LinearLayoutManager(this@UserActivity)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.user_menu, menu)

        menuBlock = menu?.findItem(R.id.block)
        menuBlock?.title = getMenuTitle(menuBlock?.title)
        viewModel.checkUid(viewModel.uid)

        val menuShare = menu?.findItem(R.id.share)
        menuShare?.title = getMenuTitle(menuShare?.title)

        val menuReport = menu?.findItem(R.id.report)
        menuReport?.title = getMenuTitle(menuReport?.title)
        menuReport?.isVisible = PrefManager.isLogin

        return true
    }

    private fun getMenuTitle(title: CharSequence?): SpannableString {
        return SpannableString(title).also {
            it.setSpan(
                ForegroundColorSpan(
                    MaterialColors.getColor(
                        this,
                        com.google.android.material.R.attr.colorControlNormal,
                        0
                    )
                ),
                0, title?.length ?: 0, 0
            )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()

            R.id.search -> {
                if (viewModel.userData == null)
                    Toast.makeText(this, "加载中...", Toast.LENGTH_SHORT).show()
                else
                    IntentUtil.startActivity<SearchActivity>(this) {
                        putExtra("pageType", "user")
                        putExtra("pageParam", viewModel.uid)
                        putExtra("title", binding.name.text)
                    }
            }

            R.id.block -> {
                val isBlocked = menuBlock?.title.toString() == "移除黑名单"
                MaterialAlertDialogBuilder(this).apply {
                    setTitle("确定将 ${viewModel.userData?.username} ${menuBlock?.title}？")
                    setNegativeButton(android.R.string.cancel, null)
                    setPositiveButton(android.R.string.ok) { _, _ ->
                        viewModel.uid.let {
                            menuBlock?.title = if (isBlocked) {
                                viewModel.deleteUid(it)
                                getMenuTitle("加入黑名单")
                            } else {
                                viewModel.saveUid(it)
                                getMenuTitle("移除黑名单")
                            }
                        }
                    }
                    show()
                }
            }

            R.id.share -> {
                IntentUtil.shareText(this, "https://www.coolapk.com/u/${viewModel.uid}")
            }

            R.id.report -> {
                IntentUtil.startActivity<WebViewActivity>(this) {
                    putExtra(
                        "url",
                        "https://m.coolapk.com/mp/do?c=user&m=report&id=${viewModel.uid}"
                    )
                }
            }

        }
        return true
    }


    inner class ReloadListener : FooterAdapter.FooterListener {
        override fun onReLoad() {
            loadMore()
        }
    }

}