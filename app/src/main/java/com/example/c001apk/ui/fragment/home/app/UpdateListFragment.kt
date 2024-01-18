package com.example.c001apk.ui.fragment.home.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.ThemeUtils
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.adapter.UpdateListAdapter
import com.example.c001apk.databinding.FragmentHomeFeedBinding
import com.example.c001apk.ui.fragment.BaseFragment
import com.example.c001apk.util.UpdateListUtil
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.viewmodel.AppViewModel

class UpdateListFragment : BaseFragment<FragmentHomeFeedBinding>() {

    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var mAdapter: UpdateListAdapter
    private lateinit var mLayoutManager: LinearLayoutManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!viewModel.isInit) {
            initView()
            initRefresh()
        }

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

    private fun initView() {
        mAdapter = UpdateListAdapter(UpdateListUtil.appsUpdate, viewModel, this.requireActivity())
        mLayoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
            if (itemDecorationCount == 0)
                addItemDecoration(LinearItemDecoration(10.dp))
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.isInit) {
            viewModel.isInit = false
            initView()
            initRefresh()
        }
    }


    private fun refreshData() {
        binding.swipeRefresh.isRefreshing = true
        binding.swipeRefresh.isRefreshing = false
    }

}