package com.example.c001apk.ui.fragment.home.app

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.ThemeUtils
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.adapter.AppListAdapter
import com.example.c001apk.databinding.FragmentHomeFeedBinding
import com.example.c001apk.ui.activity.AppUpdateActivity
import com.example.c001apk.ui.fragment.minterface.INavViewContainer
import com.example.c001apk.ui.fragment.minterface.IOnTabClickContainer
import com.example.c001apk.ui.fragment.minterface.IOnTabClickListener
import com.example.c001apk.util.DensityTool
import com.example.c001apk.util.UpdateListUtil
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AppListFragment : Fragment(), IOnTabClickListener {

    private lateinit var binding: FragmentHomeFeedBinding
    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var mAdapter: AppListAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private val fabViewBehavior by lazy { HideBottomViewOnScrollBehavior<FloatingActionButton>() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

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
                DensityTool.dp2px(requireContext(), 25f).toInt(),
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                    DensityTool.getNavigationBarHeight(requireContext())
                            + DensityTool.dp2px(requireContext(), 105f).toInt()
                else
                    DensityTool.dp2px(requireContext(), 25f).toInt()
            )
            lp.gravity = Gravity.BOTTOM or Gravity.END
            layoutParams = lp
            (layoutParams as CoordinatorLayout.LayoutParams).behavior = fabViewBehavior
            setOnClickListener {
                val intent = Intent(requireContext(), AppUpdateActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {}
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (viewModel.appList.isNotEmpty()) {
                    viewModel.firstCompletelyVisibleItemPosition =
                        mLayoutManager.findFirstCompletelyVisibleItemPosition()

                    if (dy > 0) {
                        (activity as INavViewContainer).hideNavigationView()
                    } else if (dy < 0) {
                        (activity as INavViewContainer).showNavigationView()
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
            binding.indicator.isIndeterminate = false
            binding.indicator.visibility = View.GONE
            refreshData()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initView() {
        val space = resources.getDimensionPixelSize(R.dimen.normal_space)
        mAdapter = AppListAdapter(viewModel.appList)
        mLayoutManager = LinearLayoutManager(activity)
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
            if (itemDecorationCount == 0)
                addItemDecoration(LinearItemDecoration(space))
        }
        viewModel.items.observe(viewLifecycleOwner) {
            viewModel.appList.clear()
            viewModel.appList.addAll(it)
            mAdapter.notifyDataSetChanged()
            binding.indicator.isIndeterminate = false
            binding.indicator.visibility = View.GONE
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
            binding.indicator.isIndeterminate = true
            binding.indicator.visibility = View.VISIBLE
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

        (requireParentFragment() as IOnTabClickContainer).tabController = this

    }

    private fun refreshData() {
        binding.swipeRefresh.isRefreshing = true
        viewModel.getItems(requireContext())
    }

    override fun onReturnTop(isRefresh: Boolean?) {
        if (viewModel.firstCompletelyVisibleItemPosition == 0) {
            refreshData()
        } else {
            binding.recyclerView.smoothScrollToPosition(0)
            //refreshData()
        }
    }

}