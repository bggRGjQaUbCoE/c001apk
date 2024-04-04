package com.example.c001apk.ui.feed.vote

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.R
import com.example.c001apk.adapter.FooterAdapter
import com.example.c001apk.adapter.FooterState
import com.example.c001apk.adapter.HeaderAdapter
import com.example.c001apk.adapter.ItemListener
import com.example.c001apk.databinding.FragmentFeedVoteBinding
import com.example.c001apk.ui.base.BaseFragment
import com.example.c001apk.ui.feed.FeedActivity
import com.example.c001apk.ui.feed.FeedDataAdapter
import com.example.c001apk.ui.feed.FeedReplyAdapter
import com.example.c001apk.ui.feed.FeedViewModel
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.view.VoteStaggerItemDecoration
import com.google.android.material.color.MaterialColors

class FeedVoteFragment : BaseFragment<FragmentFeedVoteBinding>() {

    private val viewModel by viewModels<FeedViewModel>(ownerProducer = { requireActivity() })
    private lateinit var feedDataAdapter: FeedDataAdapter
    private lateinit var feedReplyAdapter: FeedReplyAdapter
    private lateinit var footerAdapter: FooterAdapter
    private lateinit var sLayoutManager: StaggeredGridLayoutManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initBar()
        initData()
        initRefresh()
        initScroll()
        initObserve()

    }

    private fun initObserve() {
        viewModel.toastText.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.footerState.observe(viewLifecycleOwner) {
            footerAdapter.setLoadState(it)
            if (it !is FooterState.Loading) {
                binding.swipeRefresh.isRefreshing = false
                binding.indicator.parent.isIndeterminate = false
                binding.indicator.parent.isVisible = false
            }
        }

        viewModel.feedReplyData.observe(viewLifecycleOwner) {
            viewModel.listSize = it.size
            feedReplyAdapter.submitList(it)
        }
    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    viewModel.lastVisibleItemPosition =
                        sLayoutManager.findLastVisibleItemPositions(null).max()

                    if (viewModel.lastVisibleItemPosition == viewModel.listSize + viewModel.itemCount
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
        viewModel.preFetchVoteComment()
    }

    private fun initRefresh() {
        binding.swipeRefresh.apply {
            setColorSchemeColors(
                MaterialColors.getColor(
                    requireContext(),
                    com.google.android.material.R.attr.colorPrimary,
                    0
                )
            )
            setOnRefreshListener {
                refreshData()
            }
        }
    }

    private fun initBar() {
        binding.toolBar.apply {
            title = viewModel.feedTypeName

            setNavigationIcon(R.drawable.ic_back)
            setNavigationOnClickListener {
                activity?.finish()
            }
        }
    }

    private fun initData() {
        if (viewModel.isInit) {
            viewModel.isInit = false
            refreshData()
        }
    }

    private fun refreshData() {
        viewModel.lastVisibleItemPosition = 0
        viewModel.firstItem = null
        viewModel.lastItem = null
        viewModel.page = 1
        viewModel.isEnd = false
        viewModel.isRefreshing = true
        viewModel.isLoadMore = false
        viewModel.preFetchVoteComment()
    }

    private fun initView() {
        feedDataAdapter =
            FeedDataAdapter(ItemClickListener(), viewModel.feedDataList, viewModel.articleList)
        feedReplyAdapter = FeedReplyAdapter(viewModel.blackListRepo, ItemClickListener())
        footerAdapter = FooterAdapter(ReloadListener())
        sLayoutManager = StaggeredGridLayoutManager(2, 1)
        binding.recyclerView.apply {
            adapter =
                ConcatAdapter(HeaderAdapter(), feedDataAdapter, feedReplyAdapter, footerAdapter)
            layoutManager = sLayoutManager
            addItemDecoration(VoteStaggerItemDecoration(10.dp))
        }
    }

    inner class ReloadListener : FooterAdapter.FooterListener {
        override fun onReLoad() {
            loadMore()
        }
    }

    inner class ItemClickListener : ItemListener {
        override fun onReply(
            id: String,
            uid: String,
            username: String?,
            position: Int,
            rPosition: Int?
        ) {
            IntentUtil.startActivity<FeedActivity>(requireContext()) {
                putExtra("id", id)
            }
        }

    }

}