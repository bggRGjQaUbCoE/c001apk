package com.example.c001apk.ui.base

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.R
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.constant.Constants.LOADING_EMPTY
import com.example.c001apk.databinding.BaseRefreshRecyclerviewBinding
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.view.StaggerItemDecoration
import com.google.android.material.color.MaterialColors

// SwipeRefreshLayout + RecyclerView
abstract class BaseViewFragment<VM : BaseViewModel> : Fragment() {

    var _binding: BaseRefreshRecyclerviewBinding? = null
    val binding get() = _binding!!
    abstract val viewModel: VM
    lateinit var mAdapter: ConcatAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var sLayoutManager: StaggeredGridLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BaseRefreshRecyclerviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!viewModel.isInit) {
            initView()
            initRefresh()
            initScroll()
            initObserve()
            initError()
        }
    }

    private fun initData() {
        viewModel.loadingState.value = LoadingState.Loading
    }

    private fun initView() {
        initAdapter()
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager =
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    mLayoutManager = LinearLayoutManager(requireContext())
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

    abstract fun initAdapter()

    open fun initRefresh() {
        binding.swipeRefresh.apply {
            isEnabled = false
            setColorSchemeColors(
                MaterialColors.getColor(
                    requireContext(),
                    com.google.android.material.R.attr.colorPrimary,
                    0
                )
            )
            setOnRefreshListener {
                binding.swipeRefresh.isRefreshing = true
                refreshData()
            }
        }
    }

    fun refreshData() {
        viewModel.lastVisibleItemPosition = 0
        viewModel.lastItem = null
        viewModel.page = 1
        viewModel.isEnd = false
        viewModel.isRefreshing = true
        viewModel.isLoadMore = false
        fetchData()
    }

    open fun fetchData() {
        viewModel.fetchData()
    }

    open fun initObserve() {
        viewModel.loadingState.observe(viewLifecycleOwner) {
            when (it) {
                LoadingState.Loading -> {
                    binding.indicator.parent.apply {
                        isIndeterminate = true
                        isVisible = true
                    }
                    refreshData()
                }

                LoadingState.LoadingDone -> {
                    binding.swipeRefresh.apply {
                        isEnabled = true
                        isRefreshing = false
                    }
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
                        retry.text = if (it.msg == LOADING_EMPTY) getString(R.string.refresh)
                        else getString(R.string.retry)
                        parent.isVisible = true
                    }
                }
            }
            if (it !is LoadingState.Loading) {
                binding.indicator.parent.isIndeterminate = false
                binding.indicator.parent.isVisible = false
            }
        }
    }

    private fun initError() {
        binding.errorLayout.retry.setOnClickListener {
            binding.errorLayout.parent.isVisible = false
            viewModel.loadingState.value = LoadingState.Loading
        }
    }

    fun loadMore() {
        viewModel.isLoadMore = true
        viewModel.fetchData()
    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                    if (viewModel.listSize != -1 && isAdded)
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

                    if (viewModel.lastVisibleItemPosition == viewModel.listSize + 1
                        && !viewModel.isEnd && !viewModel.isRefreshing && !viewModel.isLoadMore
                        && !binding.swipeRefresh.isRefreshing
                    ) {
                        loadMore()
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                onScrolled(dy)
            }
        })
    }

    open fun onScrolled(dy: Int) {}

    override fun onResume() {
        super.onResume()
        if (viewModel.isInit) {
            viewModel.isInit = false
            initView()
            initData()
            initRefresh()
            initScroll()
            initObserve()
            initError()
        }
        initLift()
    }

    override fun onStart() {
        super.onStart()
        initLift()
    }

    override fun onPause() {
        super.onPause()
        detachLift()
    }

    override fun onStop() {
        super.onStop()
        detachLift()
    }

    private fun detachLift() {
        binding.recyclerView.borderViewDelegate.borderVisibilityChangedListener = null
    }

    private fun initLift() {
        val parent = parentFragment as? BasePagerFragment
        parent?.let {
            it.binding.appBar.setLifted(
                !binding.recyclerView.borderViewDelegate.isShowingTopBorder
            )
            binding.recyclerView.borderViewDelegate
                .setBorderVisibilityChangedListener { top, _, _, _ ->
                    it.binding.appBar.setLifted(!top)
                }
        }
    }

}
