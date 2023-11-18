package com.example.c001apk.ui.fragment.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.ThemeUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.adapter.AppListAdapter
import com.example.c001apk.databinding.FragmentHomeFeedBinding
import com.example.c001apk.ui.activity.AppUpdateActivity
import com.example.c001apk.ui.fragment.minterface.IOnTabClickContainer
import com.example.c001apk.ui.fragment.minterface.IOnTabClickListener
import com.example.c001apk.util.UpdateListUtil
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AppListFragment : Fragment(), IOnTabClickListener {

    private lateinit var binding: FragmentHomeFeedBinding
    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var mAdapter: AppListAdapter
    private lateinit var mLayoutManager: LinearLayoutManager

    private lateinit var mFAB: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeFeedBinding.inflate(inflater, container, false)
        mFAB = FloatingActionButton(requireContext()).apply {
            setImageResource(R.drawable.ic_update)
            setOnClickListener {
                val intent = Intent(requireContext(), AppUpdateActivity::class.java)
                startActivity(intent)
            }
            visibility = View.GONE
        }
        val lp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = (Gravity.BOTTOM or Gravity.END)
            marginEnd = resources.getDimensionPixelSize(R.dimen.normal_space)
            bottomMargin = resources.getDimensionPixelSize(R.dimen.normal_space)
        }
        binding.root.addView(mFAB, lp)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!viewModel.isInit) {
            initView()
            initRefresh()
            initScroll()
        }

    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {}
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                viewModel.firstCompletelyVisibleItemPosition =
                    mLayoutManager.findFirstCompletelyVisibleItemPosition()
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
                mFAB.visibility = View.VISIBLE
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
            initView()
            initRefresh()
            initScroll()
        }

        (requireParentFragment() as IOnTabClickContainer).controller = this

    }


    private fun refreshData() {
        binding.swipeRefresh.isRefreshing = true
        viewModel.getItems(requireContext())
    }

    override fun onReturnTop(position: Int) {
        if (position == 1) {
            if (viewModel.firstCompletelyVisibleItemPosition == 0) {
                refreshData()
            } else {
                binding.recyclerView.smoothScrollToPosition(0)
                //refreshData()
            }
        }
    }

}