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
    val isPortrait by lazy { resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT }

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

    open fun initView() {
        initAdapter()
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager =
                if (isPortrait) {
                    mLayoutManager = LinearLayoutManager(requireContext())
                    mLayoutManager
                } else {
                    sLayoutManager =
                        StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                    sLayoutManager
                }
            if (isPortrait)
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
                    if (!viewModel.isLoadMore)
                        refreshData()
                }

                LoadingState.LoadingDone -> {}

                is LoadingState.LoadingError -> {
                    binding.errorMessage.errMsg.text = it.errMsg
                }

                is LoadingState.LoadingFailed -> {
                    binding.errorLayout.apply {
                        msg.text = it.msg
                        retry.text = if (it.msg == LOADING_EMPTY) getString(R.string.refresh)
                        else getString(R.string.retry)
                    }
                }
            }
            binding.indicator.parent.isIndeterminate = it is LoadingState.Loading
            binding.indicator.parent.isVisible = it is LoadingState.Loading
            binding.swipeRefresh.isEnabled = it is LoadingState.LoadingDone
            binding.errorMessage.errMsg.isVisible = it is LoadingState.LoadingError
            binding.errorLayout.parent.isVisible = it is LoadingState.LoadingFailed
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun initError() {
        binding.errorLayout.retry.setOnClickListener {
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
                    viewModel.lastVisibleItemPosition = if (isPortrait)
                        mLayoutManager.findLastVisibleItemPosition()
                    else
                        sLayoutManager.findLastVisibleItemPositions(null).max()

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
