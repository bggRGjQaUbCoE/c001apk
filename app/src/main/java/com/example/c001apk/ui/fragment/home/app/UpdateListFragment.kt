package com.example.c001apk.ui.fragment.home.app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.adapter.UpdateListAdapter
import com.example.c001apk.databinding.FragmentHomeFeedBinding
import com.example.c001apk.logic.model.UpdateCheckResponse
import com.example.c001apk.ui.fragment.BaseFragment
import com.example.c001apk.ui.fragment.search.SearchContentFragment
import com.example.c001apk.util.Utils.getColorFromAttr
import com.example.c001apk.view.LinearItemDecoration

class UpdateListFragment : BaseFragment<FragmentHomeFeedBinding>() {

    private val viewModel by lazy { ViewModelProvider(this)[UpdateListViewModel::class.java] }
    private lateinit var mAdapter: UpdateListAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var appsUpdateList: List<UpdateCheckResponse.Data>

    companion object {
        @JvmStatic
        fun newInstance(appsUpdateList: ArrayList<UpdateCheckResponse.Data>) =
            UpdateListFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList("list", appsUpdateList)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            appsUpdateList =
                it.getParcelableArrayList<UpdateCheckResponse.Data>("list") as List<UpdateCheckResponse.Data>
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initRefresh()
        initObserve()

    }

    private fun initObserve() {
        viewModel.doNext.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let { link ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                try {
                    requireContext().startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(requireContext(), "打开失败", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        }
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

    private fun initView() {
        mAdapter = UpdateListAdapter(appsUpdateList, viewModel)
        mLayoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
            if (itemDecorationCount == 0)
                addItemDecoration(LinearItemDecoration(10.dp))
        }
    }

    private fun refreshData() {
        binding.swipeRefresh.isRefreshing = true
        binding.swipeRefresh.isRefreshing = false
    }

}