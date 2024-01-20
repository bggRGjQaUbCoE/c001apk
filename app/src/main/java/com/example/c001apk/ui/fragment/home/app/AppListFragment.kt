package com.example.c001apk.ui.fragment.home.app

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.appcompat.widget.ThemeUtils
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.R
import com.example.c001apk.adapter.AppListAdapter
import com.example.c001apk.databinding.FragmentHomeFeedBinding
import com.example.c001apk.ui.activity.AppUpdateActivity
import com.example.c001apk.ui.fragment.BaseFragment
import com.example.c001apk.ui.fragment.minterface.INavViewContainer
import com.example.c001apk.ui.fragment.minterface.IOnTabClickContainer
import com.example.c001apk.ui.fragment.minterface.IOnTabClickListener
import com.example.c001apk.util.DensityTool
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.UpdateListUtil
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.view.StaggerItemDecoration
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AppListFragment : BaseFragment<FragmentHomeFeedBinding>(), IOnTabClickListener {

    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
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
                0,
                0,
                25.dp,
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                    DensityTool.getNavigationBarHeight(requireContext())
                            + 105.dp
                else
                    25.dp
            )
            lp.gravity = Gravity.BOTTOM or Gravity.END
            layoutParams = lp
            (layoutParams as CoordinatorLayout.LayoutParams).behavior = fabViewBehavior
            setOnClickListener {
                IntentUtil.startActivity<AppUpdateActivity>(requireContext()) {
                }
            }
        }
    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {}
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (viewModel.appList.isNotEmpty() && isAdded) {
                    if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        viewModel.firstCompletelyVisibleItemPosition =
                            mLayoutManager.findFirstCompletelyVisibleItemPosition()
                    } else {
                        val positions = sLayoutManager.findFirstCompletelyVisibleItemPositions(null)
                        for (pos in positions) {
                            if (pos < viewModel.firstCompletelyVisibleItemPosition) {
                                viewModel.firstCompletelyVisibleItemPosition = pos
                            }
                        }
                    }

                    if (dy > 0) {
                        (activity as? INavViewContainer)?.hideNavigationView()
                    } else if (dy < 0) {
                        (activity as? INavViewContainer)?.showNavigationView()
                    }
                }
            }
        })
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
            binding.indicator.parent.isIndeterminate = false
            binding.indicator.parent.visibility = View.GONE
            refreshData()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initView() {
        mAdapter = AppListAdapter(viewModel.appList)
        mLayoutManager = LinearLayoutManager(requireContext())
        sLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        binding.recyclerView.apply {
            itemAnimator = null
            adapter = mAdapter
            layoutManager =
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                    mLayoutManager
                else sLayoutManager
            if (itemDecorationCount == 0)
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                    addItemDecoration(LinearItemDecoration(10.dp))
                else
                    addItemDecoration(StaggerItemDecoration(10.dp))
        }
        viewModel.items.observe(viewLifecycleOwner) {
            viewModel.appList.clear()
            viewModel.appList.addAll(it)
            mAdapter.notifyDataSetChanged()
            binding.indicator.parent.isIndeterminate = false
            binding.indicator.parent.visibility = View.GONE
            binding.swipeRefresh.isRefreshing = false
        }
        viewModel.appsUpdateData.observe(viewLifecycleOwner) {
            Log.i("AppListFragment", it.isSuccess.toString())
            it.getOrNull()?.let { data ->
                UpdateListUtil.appsUpdate.clear()
                UpdateListUtil.appsUpdate.addAll(data)
                binding.fab.visibility = View.VISIBLE
            }
        }
        viewModel.updateCheckEncoded.observe(viewLifecycleOwner) {
            viewModel.getAppsUpdate()
        }
        if (viewModel.appList.isEmpty()) {
            binding.indicator.parent.isIndeterminate = true
            binding.indicator.parent.visibility = View.VISIBLE
            viewModel.getItems(requireContext())
        }

    }

    override fun onResume() {
        super.onResume()
        if (viewModel.isInit) {
            viewModel.isInit = false
            initFab()
            initView()
            initRefresh()
            initScroll()
        }

        (requireParentFragment() as? IOnTabClickContainer)?.tabController = this

    }

    override fun onPause() {
        super.onPause()
        (requireParentFragment() as? IOnTabClickContainer)?.tabController = null
    }

    private fun refreshData() {
        viewModel.firstCompletelyVisibleItemPosition = -1
        binding.swipeRefresh.isRefreshing = true
        viewModel.getItems(requireContext())
    }

    override fun onReturnTop(isRefresh: Boolean?) {
        binding.recyclerView.stopScroll()
        if (viewModel.firstCompletelyVisibleItemPosition == 0) {
            refreshData()
        } else {
            viewModel.firstCompletelyVisibleItemPosition = 0
            binding.recyclerView.scrollToPosition(0)
        }
    }

}