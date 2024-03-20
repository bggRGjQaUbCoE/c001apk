package com.example.c001apk.ui.hometopic

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.databinding.FragmentHomeTopicBinding
import com.example.c001apk.ui.base.BaseFragment
import com.example.c001apk.ui.main.INavViewContainer
import com.example.c001apk.view.MyLinearSmoothScroller
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeTopicFragment : BaseFragment<FragmentHomeTopicBinding>(),
    BrandLabelAdapter.OnLabelClickListener {

    private val viewModel by viewModels<HomeTopicViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.type = it.getString("TYPE")
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(type: String) =
            HomeTopicFragment().apply {
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
            initObserve()
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!viewModel.isInit) {
            initView()
            initObserve()
        }

        binding.errorLayout.retry.setOnClickListener {
            binding.errorLayout.parent.visibility = View.GONE
            binding.indicator.parent.isIndeterminate = true
            binding.indicator.parent.visibility = View.VISIBLE
            if (viewModel.type == "topic")
                viewModel.fetchTopicList()
            else
                viewModel.fetchProductList()
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

    }

    private fun initObserve() {
        viewModel.doNext.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                if (it)
                    initView()
                else {
                    binding.indicator.parent.isIndeterminate = false
                    binding.indicator.parent.visibility = View.GONE
                    binding.errorLayout.parent.visibility = View.VISIBLE
                }
            }

        }
    }


    private fun initData() {
        if (viewModel.tabList.isEmpty()) {
            binding.indicator.parent.isIndeterminate = true
            binding.indicator.parent.visibility = View.VISIBLE
            if (viewModel.type == "topic") {
                viewModel.url = "/page?url=V11_VERTICAL_TOPIC"
                viewModel.title = "话题"
                viewModel.fetchTopicList()
            } else
                viewModel.fetchProductList()
        }
    }

    private fun initView() {
        if (viewModel.tabList.isNotEmpty()) {
            binding.recyclerView.apply {
                adapter = BrandLabelAdapter(viewModel.tabList).also {
                    it.setCurrentPosition(viewModel.position)
                    it.setOnLabelClickListener(this@HomeTopicFragment)
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
            childFragmentManager.findFragmentByTag("$position")?.let {
                transaction.show(it)
            }
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