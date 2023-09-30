package com.example.coolapk.ui.fragment.search.result

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coolapk.R
import com.example.coolapk.databinding.FragmentSearchFeedBinding
import com.example.coolapk.util.LinearItemDecoration
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchContentFragment : Fragment() {

    private lateinit var binding: FragmentSearchFeedBinding
    private val viewModel by lazy { ViewModelProvider(this)[SearchContentViewModel::class.java] }
    private var keyWord: String = ""
    private var type: String = ""
    private lateinit var mAdapter: SearchFeedAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private var firstCompletelyVisibleItemPosition = 0
    private var lastVisibleItemPosition = 0

    companion object {
        @JvmStatic
        fun newInstance(keyWord: String, type: String) =
            SearchContentFragment().apply {
                arguments = Bundle().apply {
                    putString("KEYWORD", keyWord)
                    putString("TYPE", type)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            keyWord = it.getString("KEYWORD")!!
            type = it.getString("TYPE")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initData()
        initRefresh()
        initScroll()

        viewModel.searchData.observe(viewLifecycleOwner) { result ->
            val search = result.getOrNull()
            if (!search.isNullOrEmpty()) {
                if (viewModel.isRefreshing) {
                    viewModel.searchList.clear()
                }
                if (viewModel.isRefreshing || viewModel.isLoadMore) {
                    viewModel.searchList.addAll(search)
                }
                mAdapter.notifyDataSetChanged()
                viewModel.isLoadMore = false
                viewModel.isRefreshing = false
                binding.swipeRefresh.isRefreshing = false
            } else {
                viewModel.isEnd = true
                viewModel.isLoadMore = false
                viewModel.isRefreshing = false
                binding.swipeRefresh.isRefreshing = false
                //Toast.makeText(activity, "没有更多了", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
        }

    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (lastVisibleItemPosition == viewModel.searchList.size - 1) {
                        if (!viewModel.isEnd) {
                            viewModel.isLoadMore = true
                            viewModel.page++
                            viewModel.getSearch()
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
        //binding.swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.black)
        /*binding.swipeRefresh.setColorSchemeColors(
            ThemeUtils.getThemeAttrColor(
                requireActivity(),
                rikka.preference.simplemenu.R.attr.colorPrimary
            )
        )*/
        binding.swipeRefresh.setOnRefreshListener {
            refreshData()
        }
    }

    private fun initData() {
        if (viewModel.searchList.isEmpty())
            refreshData()
    }

    private fun refreshData() {
        binding.swipeRefresh.isRefreshing = true
        viewModel.page = 1
        viewModel.keyWord = keyWord
        viewModel.type = type
        viewModel.isEnd = false
        viewModel.isRefreshing = true
        viewModel.isLoadMore = false
        lifecycleScope.launch {
            delay(500)
            viewModel.getSearch()
        }
    }

    private fun initView() {
        val space = resources.getDimensionPixelSize(R.dimen.normal_space)
        mAdapter = SearchFeedAdapter(viewModel.searchList)
        mLayoutManager = LinearLayoutManager(activity)
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
            if (itemDecorationCount == 0)
                addItemDecoration(LinearItemDecoration(space))
        }
    }

}