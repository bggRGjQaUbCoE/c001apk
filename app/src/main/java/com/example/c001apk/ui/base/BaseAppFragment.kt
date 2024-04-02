package com.example.c001apk.ui.base

import androidx.recyclerview.widget.ConcatAdapter
import com.example.c001apk.adapter.AppAdapter
import com.example.c001apk.adapter.FooterAdapter
import com.example.c001apk.adapter.FooterState
import com.example.c001apk.adapter.HeaderAdapter
import com.example.c001apk.ui.home.IOnTabClickContainer
import com.example.c001apk.ui.home.IOnTabClickListener

// SwipeRefreshLayout + RecyclerView
abstract class BaseAppFragment<VM : BaseAppViewModel> : BaseViewFragment<VM>(),
    IOnTabClickListener {

    private lateinit var appAdapter: AppAdapter
    private lateinit var footerAdapter: FooterAdapter

    override fun initObserve() {
        super.initObserve()

        viewModel.footerState.observe(viewLifecycleOwner) {
            footerAdapter.setLoadState(it)
            if (it !is FooterState.Loading) {
                binding.swipeRefresh.isRefreshing = false
            }
        }

        viewModel.dataList.observe(viewLifecycleOwner) {
            viewModel.listSize = it.size
            appAdapter.submitList(it)
        }

    }

    override fun initAdapter() {
        appAdapter = AppAdapter(viewModel.ItemClickListener())
        footerAdapter = FooterAdapter(ReloadListener())
        mAdapter = ConcatAdapter(HeaderAdapter(), appAdapter, footerAdapter)
    }

    inner class ReloadListener : FooterAdapter.FooterListener {
        override fun onReLoad() {
            loadMore()
        }
    }

    override fun onReturnTop(isRefresh: Boolean?) {
        if (binding.swipeRefresh.isEnabled) {
            binding.swipeRefresh.isRefreshing = true
            binding.recyclerView.scrollToPosition(0)
            refreshData()
        }
    }

    override fun onResume() {
        super.onResume()
        (parentFragment as? IOnTabClickContainer)?.tabController = this
    }

    override fun onPause() {
        super.onPause()
        (parentFragment as? IOnTabClickContainer)?.tabController = null
    }

}