package com.example.c001apk.ui.messagedetail

import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.adapter.FooterAdapter
import com.example.c001apk.adapter.FooterState
import com.example.c001apk.adapter.HeaderAdapter
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.databinding.ActivityMessageBinding
import com.example.c001apk.ui.base.BaseActivity
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.view.StaggerItemDecoration
import com.google.android.material.color.MaterialColors
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback

@AndroidEntryPoint
class MessageActivity : BaseActivity<ActivityMessageBinding>() {

    private val viewModel by viewModels<MessageViewModel>(
        extrasProducer = {
            defaultViewModelCreationExtras.withCreationCallback<MessageViewModel.Factory> { factory ->
                factory.create(type = intent.getStringExtra("type").orEmpty())
            }
        }
    )
    private lateinit var mAdapter: MessageContentAdapter
    private lateinit var footerAdapter: FooterAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var sLayoutManager: StaggeredGridLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.appBar.setLiftable(true)

        initBar()
        initView()
        viewModel.loadingState.value = LoadingState.Loading
        initRefresh()
        initScroll()
        initObserve()
        initError()

    }

    private fun initError() {
        binding.errorLayout.retry.setOnClickListener {
            binding.errorLayout.parent.isVisible = false
            viewModel.loadingState.value = LoadingState.Loading
        }
    }

    private fun initObserve() {
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

        viewModel.messageListData.observe(this) {
            viewModel.listSize = it.size
            mAdapter.submitList(it)
        }
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
        when (viewModel.type) {
            "atMe" -> {
                binding.toolBar.title = "@我的动态"
            }

            "atCommentMe" -> {
                binding.toolBar.title = "@我的评论"
            }

            "feedLike" -> {
                binding.toolBar.title = "我收到的赞"
            }

            "contactsFollow" -> {
                binding.toolBar.title = "好友关注"
            }

            "list" -> {
                binding.toolBar.title = "私信"
            }
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
        viewModel.fetchMessage()
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
        mAdapter = MessageContentAdapter(viewModel.type, viewModel.ItemClickListener())
        footerAdapter = FooterAdapter(ReloadListener())
        binding.recyclerView.apply {
            adapter = ConcatAdapter(HeaderAdapter(), mAdapter, footerAdapter)
            layoutManager =
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    mLayoutManager = LinearLayoutManager(this@MessageActivity)
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
        when (viewModel.type) {
            "atMe" -> viewModel.url = "/v6/notification/atMeList"
            "atCommentMe" -> viewModel.url = "/v6/notification/atCommentMeList"
            "feedLike" -> viewModel.url = "/v6/notification/feedLikeList"
            "contactsFollow" -> viewModel.url = "/v6/notification/contactsFollowList"
            "list" -> viewModel.url = "/v6/message/list"

        }
        viewModel.fetchMessage()
    }

    inner class ReloadListener : FooterAdapter.FooterListener {
        override fun onReLoad() {
            loadMore()
        }
    }

    override fun onResume() {
        super.onResume()
        initLift()
    }

    override fun onStart() {
        super.onStart()
        initLift()
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

    private fun initLift() {
        binding.appBar.setLifted(
            !binding.recyclerView.borderViewDelegate.isShowingTopBorder
        )
        binding.recyclerView.borderViewDelegate
            .setBorderVisibilityChangedListener { top, _, _, _ ->
                binding.appBar.setLifted(!top)
            }
    }

}