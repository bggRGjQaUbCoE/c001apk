package com.example.c001apk.ui.applist

import android.content.res.Configuration
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.R
import com.example.c001apk.adapter.HeaderAdapter
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.databinding.FragmentHomeFeedBinding
import com.example.c001apk.ui.appupdate.AppUpdateActivity
import com.example.c001apk.ui.base.BaseFragment
import com.example.c001apk.ui.home.IOnTabClickContainer
import com.example.c001apk.ui.home.IOnTabClickListener
import com.example.c001apk.ui.main.INavViewContainer
import com.example.c001apk.ui.main.IOnBottomClickContainer
import com.example.c001apk.ui.main.IOnBottomClickListener
import com.example.c001apk.util.DensityTool
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.view.StaggerItemDecoration
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.color.MaterialColors
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AppListFragment : BaseFragment<FragmentHomeFeedBinding>(), IOnTabClickListener,
    IOnBottomClickListener {

    private val viewModel by viewModels<AppListViewModel>()
    private lateinit var mAdapter: AppListAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private val fabViewBehavior by lazy { HideBottomViewOnScrollBehavior<FloatingActionButton>() }
    private lateinit var sLayoutManager: StaggeredGridLayoutManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!viewModel.isInit) {
            initFab()
            initView()
            initRefresh()
            initScroll()
            initObserve()
        }

    }

    private fun initObserve() {
        viewModel.loadingState.observe(viewLifecycleOwner) {
            when (it) {
                LoadingState.Loading -> {
                    binding.indicator.parent.isIndeterminate = true
                    binding.indicator.parent.isVisible = true
                    viewModel.getItems(requireContext())
                }

                LoadingState.LoadingDone -> {
                    binding.swipeRefresh.isEnabled = true
                }

                is LoadingState.LoadingError -> {}
                is LoadingState.LoadingFailed -> {}
            }
            if (it !is LoadingState.Loading) {
                binding.indicator.parent.isIndeterminate = false
                binding.indicator.parent.isVisible = false
            }
        }

        viewModel.items.observe(viewLifecycleOwner) {
            mAdapter.submitList(it)
            binding.swipeRefresh.isRefreshing = false
        }

        viewModel.setFab.observe(viewLifecycleOwner) {
            if (it)
                binding.fab.isVisible = true
        }
    }

    private fun initFab() {
        binding.fab.apply {
            setImageResource(R.drawable.ic_update)
            val lp = CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                CoordinatorLayout.LayoutParams.WRAP_CONTENT
            )
            lp.setMargins(
                0, 0, 25.dp,
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                    DensityTool.getNavigationBarHeight(requireContext()) + 105.dp
                else
                    25.dp
            )
            lp.gravity = Gravity.BOTTOM or Gravity.END
            layoutParams = lp
            (layoutParams as CoordinatorLayout.LayoutParams).behavior = fabViewBehavior
            setOnClickListener {
                IntentUtil.startActivity<AppUpdateActivity>(requireContext()) {
                    putParcelableArrayListExtra("list", viewModel.appsUpdate)
                }
            }
        }
    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {}
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (viewModel.listSize != -1 && isAdded) {
                    if (dy > 0) {
                        (activity as? INavViewContainer)?.hideNavigationView()
                    } else if (dy < 0) {
                        (activity as? INavViewContainer)?.showNavigationView()
                    }
                }
            }
        })
    }

    private fun initRefresh() {
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
                refreshData()
            }
        }
    }

    private fun initView() {
        mAdapter = AppListAdapter()
        binding.recyclerView.apply {
            itemAnimator = null
            adapter = ConcatAdapter(HeaderAdapter(), mAdapter)
            layoutManager =
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    mLayoutManager = LinearLayoutManager(requireContext())
                    mLayoutManager
                } else {
                    sLayoutManager =
                        StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                    sLayoutManager
                }
            if (itemDecorationCount == 0)
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                    addItemDecoration(LinearItemDecoration(10.dp))
                else
                    addItemDecoration(StaggerItemDecoration(10.dp))
        }

    }

    override fun onResume() {
        super.onResume()
        if (viewModel.isInit) {
            viewModel.isInit = false
            initFab()
            initView()
            viewModel.loadingState.value = LoadingState.Loading
            initRefresh()
            initScroll()
            initObserve()
        }

        (requireParentFragment() as? IOnTabClickContainer)?.tabController = this
        (activity as? IOnBottomClickContainer)?.controller = this
    }

    override fun onPause() {
        super.onPause()
        (requireParentFragment() as? IOnTabClickContainer)?.tabController = null
        (activity as? IOnBottomClickContainer)?.controller = null
    }

    private fun refreshData() {
        binding.swipeRefresh.isRefreshing = true
        viewModel.getItems(requireContext())
    }

    override fun onReturnTop(isRefresh: Boolean?) {
        binding.swipeRefresh.isRefreshing = true
        binding.recyclerView.scrollToPosition(0)
        refreshData()
    }

    override fun onReturnTop() {
        onReturnTop(null)
    }

}