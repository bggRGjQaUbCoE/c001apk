package com.example.c001apk.ui.feed.reply.attopic

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import com.example.c001apk.adapter.FooterAdapter
import com.example.c001apk.adapter.FooterState
import com.example.c001apk.adapter.HeaderAdapter
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.ui.base.BaseViewFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : BaseViewFragment<SearchViewModel>(), OnSearchListener {

    override val viewModel: SearchViewModel by viewModels<SearchViewModel>()
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var footerAdapter: FooterAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.type = arguments?.getString("type") ?: "user"
        viewModel.keyword = arguments?.getString("keyword") ?: "null"
    }

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
            searchAdapter.submitList(it)
        }
    }

    override fun initAdapter() {
        searchAdapter = SearchAdapter { first, second ->
            if (viewModel.type == "user")
                (activity as? AtTopicActivity)?.onClickUser(first, second) // avatar, username
            else
                (activity as? AtTopicActivity)?.onClickTopic(first, second) // title, id
        }
        footerAdapter = FooterAdapter(object : FooterAdapter.FooterListener {
            override fun onReLoad() {
                viewModel.isEnd = false
                loadMore()
            }
        })
        mAdapter = ConcatAdapter(HeaderAdapter(), searchAdapter, footerAdapter)
    }

    override fun onSearch(type: String, keyword: String) {
        viewModel.keyword = keyword
        viewModel.dataList.value = emptyList()
        viewModel.footerState.value = FooterState.LoadingDone
        viewModel.loadingState.value = LoadingState.Loading
    }

    companion object {
        @JvmStatic
        fun newInstance(type: String, keyword: String) =
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putString("type", type)
                    putString("keyword", keyword)
                }
            }
    }

    override fun onResume() {
        super.onResume()
        (activity as? OnSearchContainer)?.container = this
    }

    override fun onPause() {
        super.onPause()
        (activity as? OnSearchContainer)?.container = null
    }

}