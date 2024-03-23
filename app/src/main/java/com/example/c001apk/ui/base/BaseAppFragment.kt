package com.example.c001apk.ui.base

import android.widget.Toast
import androidx.recyclerview.widget.ConcatAdapter
import com.example.c001apk.adapter.AppAdapter
import com.example.c001apk.adapter.FooterAdapter
import com.example.c001apk.adapter.FooterState
import com.example.c001apk.adapter.HeaderAdapter

// SwipeRefreshLayout + RecyclerView
abstract class BaseAppFragment<VM : BaseAppViewModel> : BaseViewFragment<VM>() {

    private lateinit var appAdapter: AppAdapter
    lateinit var footerAdapter: FooterAdapter

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

        viewModel.toastText.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let { text ->
                context?.let {
                    Toast.makeText(it, text, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.followState.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                mAdapter.notifyItemChanged(it)
            }
        }
    }

    override fun initAdapter() {
        appAdapter = AppAdapter(viewModel.blackListRepo, viewModel.ItemClickListener())
        footerAdapter = FooterAdapter(ReloadListener())
        mAdapter = ConcatAdapter(HeaderAdapter(), appAdapter, footerAdapter)
    }

    inner class ReloadListener : FooterAdapter.FooterListener {
        override fun onReLoad() {
            loadMore()
        }
    }

}