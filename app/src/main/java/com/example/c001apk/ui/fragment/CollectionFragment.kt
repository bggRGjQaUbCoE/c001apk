package com.example.c001apk.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.ThemeUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.adapter.AppAdapter
import com.example.c001apk.databinding.FragmentCollectionBinding
import com.example.c001apk.ui.fragment.minterface.IOnCollectionItemClickListener
import com.example.c001apk.ui.fragment.search.SearchResultFragment
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.viewmodel.AppViewModel

class CollectionFragment : Fragment(), IOnCollectionItemClickListener {

    private lateinit var binding: FragmentCollectionBinding
    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var mAdapter: AppAdapter
    private lateinit var mLayoutManager: LinearLayoutManager

    companion object {
        @JvmStatic
        fun newInstance(id: String, title:String) =
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
            viewModel.cId = it.getString("ID")!!
            viewModel.title = it.getString("TITLE")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCollectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initBar()
        initView()
        initData()
        initRefresh()
        initScroll()

        viewModel.collectionListData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val data = result.getOrNull()
                if (!data.isNullOrEmpty()) {
                    if (viewModel.isRefreshing) viewModel.dataList.clear()
                    if (viewModel.isRefreshing || viewModel.isLoadMore) {
                        viewModel.listSize = viewModel.dataList.size
                        for (element in data)
                            if (element.entityType == "collection"
                                || element.entityType == "feed"
                            )
                                viewModel.dataList.add(element)
                    }
                    mAdapter.setLoadState(mAdapter.LOADING_COMPLETE, null)
                } else {
                    mAdapter.setLoadState(mAdapter.LOADING_END, null)
                    viewModel.isEnd = true
                    result.exceptionOrNull()?.printStackTrace()
                }
                if (viewModel.isLoadMore)
                    if (viewModel.isEnd)
                        mAdapter.notifyItemChanged(viewModel.dataList.size)
                    else
                        mAdapter.notifyItemRangeChanged(
                            viewModel.listSize + 1,
                            viewModel.dataList.size - viewModel.listSize + 1
                        )
                else
                    mAdapter.notifyDataSetChanged()
                binding.indicator.isIndeterminate = false
                binding.indicator.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                viewModel.isRefreshing = false
                viewModel.isLoadMore = false
            }
        }


    }

    private fun initBar() {
        binding.toolBar.apply {
            title = if (viewModel.title == "") "我的收藏单"
            else viewModel.title
            setNavigationIcon(R.drawable.ic_back)
            setNavigationOnClickListener {
                if (viewModel.cId.isNullOrEmpty())
                    requireActivity().finish()
                else
                    requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }

    private fun initData() {
        if (viewModel.dataList.isEmpty()) {
            binding.indicator.visibility = View.VISIBLE
            binding.indicator.isIndeterminate = true
            refreshData()
        } else {
            if (viewModel.isEnd) {
                mAdapter.setLoadState(mAdapter.LOADING_END, null)
                mAdapter.notifyItemChanged(viewModel.dataList.size)
            }
        }
    }

    private fun initView() {
        val space = resources.getDimensionPixelSize(R.dimen.normal_space)
        mAdapter = AppAdapter(requireContext(), viewModel.dataList)
        mAdapter.setIOnCollectionItemClickListener(this)
        mLayoutManager = LinearLayoutManager(activity)
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
            if (itemDecorationCount == 0)
                addItemDecoration(LinearItemDecoration(space))
        }
    }

    private fun refreshData() {
        viewModel.isEnd = false
        viewModel.isRefreshing = true
        viewModel.isLoadMore = false
        viewModel.isNew = true
        viewModel.collectionUrl = if (viewModel.cId.isNullOrEmpty()) "/v6/collection/list"
        else "/v6/collection/itemList"
        viewModel.getCollectionList()
    }

    @SuppressLint("RestrictedApi")
    private fun initRefresh() {
        binding.swipeRefresh.setColorSchemeColors(
            ThemeUtils.getThemeAttrColor(
                requireContext(),
                rikka.preference.simplemenu.R.attr.colorPrimary
            )
        )
        binding.swipeRefresh.setOnRefreshListener {
            binding.indicator.isIndeterminate = false
            binding.indicator.visibility = View.GONE
            refreshData()
        }
    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            @SuppressLint("NotifyDataSetChanged")
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (viewModel.lastVisibleItemPosition == viewModel.dataList.size
                        && !viewModel.isEnd && !viewModel.isRefreshing && !viewModel.isLoadMore
                    ) {
                        mAdapter.setLoadState(mAdapter.LOADING, null)
                        mAdapter.notifyItemChanged(viewModel.dataList.size)
                        viewModel.isLoadMore = true
                        viewModel.page++
                        viewModel.isNew = true
                        viewModel.getCollectionList()
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (viewModel.dataList.isNotEmpty()) {
                    viewModel.lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition()
                    viewModel.firstCompletelyVisibleItemPosition =
                        mLayoutManager.findFirstCompletelyVisibleItemPosition()
                }
            }
        })
    }

    override fun onShowCollection(id: String, title:String) {
        requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragment,
                newInstance(id, title),
                null
            )
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .addToBackStack(null)
            .commit()
    }

}