package com.example.c001apk.ui.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.R
import com.example.c001apk.adapter.AppAdapter
import com.example.c001apk.adapter.FooterAdapter
import com.example.c001apk.databinding.FragmentCollectionBinding
import com.example.c001apk.util.Utils.getColorFromAttr
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.view.StaggerItemDecoration

class CollectionFragment : BaseFragment<FragmentCollectionBinding>() {

    private val viewModel by lazy { ViewModelProvider(this)[CollectionViewModel::class.java] }
    private lateinit var mAdapter: AppAdapter
    private lateinit var footerAdapter: FooterAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var sLayoutManager: StaggeredGridLayoutManager

    companion object {
        @JvmStatic
        fun newInstance(id: String, title: String) =
            CollectionFragment().apply {
                arguments = Bundle().apply {
                    putString("ID", id)
                    putString("TITLE", title)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.id = it.getString("ID")
            viewModel.title = it.getString("TITLE")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initBar()
        initView()
        initData()
        initRefresh()
        initScroll()
        initObserve()

    }

    private fun initBar() {
        if (viewModel.id == "recommend"
            || viewModel.id == "hot"
            || viewModel.id == "newest"
        )
            binding.appBar.visibility = View.GONE
        else
            binding.toolBar.apply {
                title = if (viewModel.title.isNullOrEmpty()) "我的收藏单"
                else viewModel.title
                setNavigationIcon(R.drawable.ic_back)
                setNavigationOnClickListener {
                    if (viewModel.id.isNullOrEmpty())
                        requireActivity().finish()
                    else
                        requireActivity().supportFragmentManager.popBackStack()
                }
            }
    }

    private fun initObserve(){
        viewModel.toastText.observe(viewLifecycleOwner){event->
            event.getContentIfNotHandledOrReturnNull()?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.changeState.observe(viewLifecycleOwner) {
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

        viewModel.dataListData.observe(viewLifecycleOwner) {
            viewModel.listSize = it.size
            mAdapter.submitList(it)

            val adapter = binding.recyclerView.adapter as ConcatAdapter
            if (!adapter.adapters.contains(mAdapter)) {
                adapter.apply {
                    addAdapter(mAdapter)
                    addAdapter(footerAdapter)
                }
            }
        }
    }

    private fun initData() {
        if (viewModel.listSize==-1) {
            binding.indicator.parent.visibility = View.VISIBLE
            binding.indicator.parent.isIndeterminate = true
            refreshData()
        }
    }

    private fun initView() {
        mAdapter = AppAdapter(viewModel.ItemClickListener())
        footerAdapter = FooterAdapter(ReloadListener())
        mLayoutManager = LinearLayoutManager(requireContext())
        sLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        binding.recyclerView.apply {
            adapter = ConcatAdapter()
            layoutManager =
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                    mLayoutManager
                else sLayoutManager
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                addItemDecoration(LinearItemDecoration(10.dp))
            else
                addItemDecoration(StaggerItemDecoration(10.dp))
        }
    }

    private fun refreshData() {
        viewModel.firstVisibleItemPosition = -1
        viewModel.lastVisibleItemPosition = -1
        viewModel.page = 1
        viewModel.isEnd = false
        viewModel.isRefreshing = true
        viewModel.isLoadMore = false
        viewModel.url =
            if (viewModel.id.isNullOrEmpty()) "/v6/collection/list"
            else if (viewModel.id == "recommend") "/v6/picture/list?tag=${viewModel.title}&type=recommend"
            else if (viewModel.id == "hot") "/v6/picture/list?tag=${viewModel.title}&type=hot"
            else if (viewModel.id == "newest") "/v6/picture/list?tag=${viewModel.title}&type=newest"
            else "/v6/collection/itemList"
        viewModel.fetchCollectionList()
    }

    private fun initRefresh() {
        binding.swipeRefresh.setColorSchemeColors(
            requireContext().getColorFromAttr(
                rikka.preference.simplemenu.R.attr.colorPrimary
            )
        )
        binding.swipeRefresh.setOnRefreshListener {
            binding.indicator.parent.isIndeterminate = false
            binding.indicator.parent.visibility = View.GONE
            refreshData()
        }
    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                    if (viewModel.listSize!=-1 && !viewModel.isEnd && isAdded) {
                        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                            viewModel.lastVisibleItemPosition =
                                mLayoutManager.findLastVisibleItemPosition()
                        } else {
                            val positions = sLayoutManager.findLastVisibleItemPositions(null)
                            viewModel.lastVisibleItemPosition = positions[0]
                            for (pos in positions) {
                                if (pos > viewModel.lastVisibleItemPosition) {
                                    viewModel.lastVisibleItemPosition = pos
                                }
                            }
                        }
                    }

                    if (viewModel.lastVisibleItemPosition == viewModel.listSize
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
        viewModel.fetchCollectionList()
    }

    inner class ReloadListener : FooterAdapter.FooterListener {
        override fun onReLoad() {
            loadMore()
        }
    }
}