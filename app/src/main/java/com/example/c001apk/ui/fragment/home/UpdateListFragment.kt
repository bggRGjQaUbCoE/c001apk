package com.example.c001apk.ui.fragment.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.ThemeUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.c001apk.R
import com.example.c001apk.adapter.UpdateListAdapter
import com.example.c001apk.databinding.FragmentHomeFeedBinding
import com.example.c001apk.util.UpdateListUtil
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.viewmodel.AppViewModel

class UpdateListFragment : Fragment(){//, IOnTabClickListener {

    private lateinit var binding: FragmentHomeFeedBinding
    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var mAdapter: UpdateListAdapter
    private lateinit var mLayoutManager: LinearLayoutManager

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
            initView()
            initRefresh()
//            initScroll()
        }

    }

//    private fun initScroll() {
//        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {}
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//                viewModel.firstCompletelyVisibleItemPosition =
//                    mLayoutManager.findFirstCompletelyVisibleItemPosition()
//            }
//        })
//    }

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
        Log.i("UpdateListFragment", "initView!!!")
        Log.i("UpdateListFragment", "UpdateListUtil.appsUpdate.size = ${UpdateListUtil.appsUpdate.size}")
        val space = resources.getDimensionPixelSize(R.dimen.normal_space)
        mAdapter = UpdateListAdapter(UpdateListUtil.appsUpdate,viewModel,this.requireActivity())
        mLayoutManager = LinearLayoutManager(activity)
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
            if (itemDecorationCount == 0)
                addItemDecoration(LinearItemDecoration(space))
        }
//        viewModel.appsUpdateData.observe(viewLifecycleOwner) {
//            it.getOrNull()?.let {
//                viewModel.appsUpdate.clear()
//                viewModel.appsUpdate.addAll(it)
//                mAdapter.notifyDataSetChanged()
//                binding.indicator.isIndeterminate = false
//                binding.indicator.visibility = View.GONE
//                binding.swipeRefresh.isRefreshing = false
//            }
//        }
//        binding.indicator.isIndeterminate = true
//        binding.indicator.visibility = View.VISIBLE
//        viewModel.updateCheckEncoded.observe(viewLifecycleOwner) {
//            refreshData()
//        }
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.isInit) {
            viewModel.isInit = false
            initView()
            initRefresh()
//            initScroll()
        }

//        (requireParentFragment() as IOnTabClickContainer).controller = this

    }



    private fun refreshData() {
        binding.swipeRefresh.isRefreshing = true
//        viewModel.getAppsUpdate()
        binding.swipeRefresh.isRefreshing = false
    }

//    override fun onReturnTop(position: Int) {
//        if (position == 1) {
//            if (viewModel.firstCompletelyVisibleItemPosition == 0) {
//                refreshData()
//            } else {
//                binding.recyclerView.smoothScrollToPosition(0)
//                //refreshData()
//            }
//        }
//    }


}