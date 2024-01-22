package com.example.c001apk.ui.fragment.home.topic

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.adapter.BrandLabelAdapter
import com.example.c001apk.databinding.FragmentHomeTopicBinding
import com.example.c001apk.logic.model.TopicBean
import com.example.c001apk.ui.fragment.BaseFragment
import com.example.c001apk.ui.fragment.minterface.INavViewContainer
import com.example.c001apk.view.MyLinearSmoothScroller
import com.example.c001apk.viewmodel.AppViewModel


class TopicFragment : BaseFragment<FragmentHomeTopicBinding>(),
    BrandLabelAdapter.OnLabelClickListener {

    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.type = it.getString("TYPE")
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(type: String) =
            TopicFragment().apply {
                arguments = Bundle().apply {
                    putString("TYPE", type)
                }
            }
    }

    override fun onResume() {
        super.onResume()

        if (viewModel.isInit) {
            viewModel.isInit = false
            initData()
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!viewModel.isInit)
            initView()

        binding.errorLayout.retry.setOnClickListener {
            binding.errorLayout.parent.visibility = View.GONE
            binding.indicator.parent.isIndeterminate = true
            binding.indicator.parent.visibility = View.VISIBLE
            viewModel.isNew = true
            if (viewModel.type == "topic")
                viewModel.getDataList()
            else
                viewModel.getProductList()
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    (activity as INavViewContainer).hideNavigationView()
                } else if (dy < 0) {
                    (activity as INavViewContainer).showNavigationView()
                }
            }
        })

        viewModel.dataListData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val topic = result.getOrNull()
                if (!topic?.data.isNullOrEmpty()) {
                    if (viewModel.tabList.isEmpty()) {
                        for (element in topic?.data!![0].entities) {
                            viewModel.tabList.add(element.title)
                            viewModel.topicList.add(
                                TopicBean(
                                    element.url,
                                    element.title
                                )
                            )
                        }
                        initView()
                    }
                } else {
                    binding.indicator.parent.isIndeterminate = false
                    binding.indicator.parent.visibility = View.GONE
                    binding.errorLayout.parent.visibility = View.VISIBLE
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

        viewModel.productCategoryData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val data = result.getOrNull()
                if (!data?.data.isNullOrEmpty()) {
                    if (viewModel.tabList.isEmpty()) {
                        for (element in data?.data!!) {
                            viewModel.tabList.add(element.title)
                            viewModel.topicList.add(
                                TopicBean(
                                    element.url,
                                    element.title
                                )
                            )
                        }
                        initView()
                    }
                } else {
                    binding.indicator.parent.isIndeterminate = false
                    binding.indicator.parent.visibility = View.GONE
                    binding.errorLayout.parent.visibility = View.VISIBLE
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

    }


    private fun initData() {
        if (viewModel.tabList.isEmpty()) {
            binding.indicator.parent.isIndeterminate = true
            binding.indicator.parent.visibility = View.VISIBLE
            viewModel.isNew = true
            if (viewModel.type == "topic") {
                viewModel.url = "/page?url=V11_VERTICAL_TOPIC"
                viewModel.title = "话题"
                viewModel.getDataList()
            } else
                viewModel.getProductList()
        }
    }

    private fun initView() {
        if (viewModel.tabList.isNotEmpty()) {
            binding.recyclerView.apply {
                adapter = BrandLabelAdapter(requireContext(), viewModel.tabList).also {
                    it.setCurrentPosition(viewModel.position)
                    it.setOnLabelClickListener(this@TopicFragment)
                }
                layoutManager = LinearLayoutManager(requireContext())

            }
            onLabelClicked(viewModel.position)
            binding.indicator.parent.isIndeterminate = false
            binding.indicator.parent.visibility = View.GONE
            binding.errorLayout.parent.visibility = View.GONE
            binding.topicLayout.visibility = View.VISIBLE
        } else {
            binding.errorLayout.parent.visibility = View.VISIBLE
        }
    }

    override fun onLabelClicked(position: Int) {
        viewModel.position = position
        scrollToCenter()
        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
        hideFragment(transaction)
        if (childFragmentManager.findFragmentByTag("$position") == null) {
            transaction.add(
                R.id.frameView,
                HomeTopicContentFragment.newInstance(
                    viewModel.topicList[position].url,
                    viewModel.topicList[position].title
                ),
                "$position"
            )
        } else {
            transaction.show(
                childFragmentManager.findFragmentByTag("$position")!!
            )
        }
        transaction.commit()
    }

    private fun scrollToCenter() {
        val scroller = MyLinearSmoothScroller(binding.recyclerView.context)
        scroller.targetPosition = viewModel.position
        binding.recyclerView.layoutManager?.startSmoothScroll(scroller)
    }

    private fun hideFragment(transaction: FragmentTransaction) {
        for (position in 0 until viewModel.tabList.size) {
            childFragmentManager.findFragmentByTag("$position")?.let {
                transaction.hide(it)
            }
        }
    }

}