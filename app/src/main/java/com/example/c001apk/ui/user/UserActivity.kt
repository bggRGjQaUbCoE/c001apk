package com.example.c001apk.ui.user

import android.content.res.Configuration
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.R
import com.example.c001apk.adapter.AppAdapter
import com.example.c001apk.adapter.FooterAdapter
import com.example.c001apk.adapter.HeaderAdapter
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


@AndroidEntryPoint
class UserActivity : BaseActivity<ActivityUserBinding>() {

    private val viewModel by viewModels<UserViewModel>()
    private lateinit var mAdapter: AppAdapter
    private lateinit var footerAdapter: FooterAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var sLayoutManager: StaggeredGridLayoutManager
    private var itemBlock: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        initView()
        initData()
        initRefresh()
        initScroll()
        initObserve()

        binding.errorLayout.retry.setOnClickListener {
            binding.errorLayout.parent.visibility = View.GONE
            binding.indicator.parent.visibility = View.VISIBLE
            binding.indicator.parent.isIndeterminate = true
            refreshData()
        }

        binding.followBtn.setOnClickListener {
            if (PrefManager.isLogin)
                if (viewModel.followType) {
                    viewModel.url = "/v6/user/unfollow"
                    viewModel.onPostFollowUnFollow()
                } else {
                    viewModel.url = "/v6/user/follow"
                    viewModel.onPostFollowUnFollow()
                }
        }

    }

    private fun initObserve() {
        viewModel.updateBlockState.observe(this) { event ->
            event?.getContentIfNotHandledOrReturnNull()?.let {
                if (it)
                    itemBlock?.title = getMenuTitle("移除黑名单")
            }
        }

        viewModel.toastText.observe(this) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.afterFollow.observe(this) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                if (viewModel.followType) {
                    binding.followBtn.text = "已关注"
                } else {
                    binding.followBtn.text = "关注"
                }
            }
        }

        viewModel.showError.observe(this) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                if (it) {
                    binding.indicator.parent.isIndeterminate = false
                    binding.indicator.parent.visibility = View.GONE
                    showErrorMessage()
                } else {
                    binding.indicator.parent.isIndeterminate = false
                    binding.indicator.parent.visibility = View.GONE
                    binding.errorLayout.parent.visibility = View.VISIBLE
                }
            }
        }

        viewModel.showUser.observe(this) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                if (it) {
                    binding.userData = viewModel.userData
                    binding.listener = viewModel.ItemClickListener()
                    binding.infoLayout.visibility = View.VISIBLE
                }
            }
        }

        viewModel.changeState.observe(this) {
            footerAdapter.setLoadState(it.first, it.second)
            footerAdapter.notifyItemChanged(0)
            if (it.first != FooterAdapter.LoadState.LOADING) {
                binding.swipeRefresh.isRefreshing = false
                binding.indicator.parent.isIndeterminate = false
                binding.indicator.parent.visibility = View.GONE
                viewModel.isLoadMore = false
                viewModel.isRefreshing = false
            }
        }

        viewModel.feedData.observe(this) {
            viewModel.listSize = it.size
            mAdapter.submitList(it)
        }

    }

    private fun showErrorMessage() {
        binding.swipeRefresh.isEnabled = false
        binding.errorMessage.parent.visibility = View.VISIBLE
        binding.errorMessage.parent.text = viewModel.errorMessage
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
                        viewModel.page++
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
        binding.swipeRefresh.setColorSchemeColors(
            MaterialColors.getColor(
                this,
                com.google.android.material.R.attr.colorPrimary,
                0
            )
        )
        binding.swipeRefresh.setOnRefreshListener {
            binding.indicator.parent.isIndeterminate = false
            binding.indicator.parent.visibility = View.GONE
            refreshData()
        }
    }

    private fun initData() {
        if (viewModel.isInit) {
            viewModel.isInit = false
            binding.indicator.parent.visibility = View.VISIBLE
            binding.indicator.parent.isIndeterminate = true
            refreshData()
        } else if (viewModel.userData != null) {
            binding.userData = viewModel.userData
            binding.infoLayout.visibility = View.VISIBLE
        } else if (viewModel.errorMessage != null) {
            showErrorMessage()
        } else {
            binding.errorLayout.parent.visibility = View.VISIBLE
        }
    }

    private fun refreshData() {
        viewModel.lastVisibleItemPosition = 0
        viewModel.lastItem = null
        viewModel.page = 1
        viewModel.isRefreshing = true
        viewModel.isEnd = false
        if (viewModel.uid.isNullOrEmpty())
            viewModel.uid = intent.getStringExtra("id")
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

        viewModel.uid?.let {
            viewModel.checkUid(it)
        }
        itemBlock = menu?.findItem(R.id.block)
        itemBlock?.title = getMenuTitle(itemBlock?.title)

        val itemShare = menu?.findItem(R.id.share)
        itemShare?.title = getMenuTitle(itemShare?.title)

        val itemReport = menu?.findItem(R.id.report)
        itemReport?.title = getMenuTitle(itemReport?.title)
        itemReport?.isVisible = PrefManager.isLogin

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
                val isBlocked = itemBlock?.title.toString() == "移除黑名单"
                MaterialAlertDialogBuilder(this).apply {
                    setTitle("确定将 ${viewModel.uname} ${itemBlock?.title}？")
                    setNegativeButton(android.R.string.cancel, null)
                    setPositiveButton(android.R.string.ok) { _, _ ->
                        viewModel.uid?.let {
                            itemBlock?.title = if (isBlocked) {
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