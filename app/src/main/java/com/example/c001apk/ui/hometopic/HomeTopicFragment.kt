package com.example.c001apk.ui.hometopic

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.constant.Constants
import com.example.c001apk.databinding.FragmentHomeTopicBinding
import com.example.c001apk.ui.base.BaseFragment
import com.example.c001apk.ui.main.INavViewContainer
import com.example.c001apk.view.MyLinearSmoothScroller
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeTopicFragment : BaseFragment<FragmentHomeTopicBinding>(),
    BrandLabelAdapter.OnLabelClickListener {

    @Inject
    lateinit var viewModelAssistedFactory: HomeTopicViewModel.Factory
    private val viewModel by viewModels<HomeTopicViewModel> {
        HomeTopicViewModel.provideFactory(
            viewModelAssistedFactory,
            arguments?.getString("type").orEmpty()
        )
    }

    companion object {
        @JvmStatic
        fun newInstance(type: String) =
            HomeTopicFragment().apply {
                arguments = Bundle().apply {
                    putString("type", type)
                }
            }
    }

    override fun onResume() {
        super.onResume()

        if (viewModel.isInit) {
            viewModel.isInit = false
            viewModel.loadingState.value = LoadingState.Loading
            initObserve()
            initError()
            initScroll()
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!viewModel.isInit) {
            initObserve()
            initError()
            initScroll()
        }

    }

    private fun initError() {
        binding.errorLayout.retry.setOnClickListener {
            binding.indicator.parent.isIndeterminate = true
            binding.errorLayout.parent.isVisible = false
            binding.indicator.parent.isVisible = true
            if (viewModel.type == "topic")
                viewModel.fetchTopicList()
            else
                viewModel.fetchProductList()
        }
    }

    private fun initScroll() {
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
        viewModel.loadingState.observe(viewLifecycleOwner) {
            when (it) {
                LoadingState.Loading -> {
                    binding.indicator.parent.isIndeterminate = true
                    binding.indicator.parent.isVisible = true
                    if (viewModel.type == "topic") {
                        viewModel.url = "/page?url=V11_VERTICAL_TOPIC"
                        viewModel.title = "话题"
                        viewModel.fetchTopicList()
                    } else
                        viewModel.fetchProductList()
                }

                LoadingState.LoadingDone -> {
                    initView()
                }

                is LoadingState.LoadingError -> {
                    binding.errorMessage.errMsg.apply {
                        text = it.errMsg
                        isVisible = true
                    }
                }

                is LoadingState.LoadingFailed -> {
                    binding.errorLayout.apply {
                        msg.text = it.msg
                        retry.text = if (it.msg == Constants.LOADING_EMPTY) getString(R.string.refresh)
                        else getString(R.string.retry)
                        parent.isVisible = true
                    }
                }
            }
            if (it !is LoadingState.Loading) {
                binding.indicator.parent.isIndeterminate = false
                binding.indicator.parent.isVisible = false
            }
        }
    }

    private fun initView() {
        binding.topicLayout.isVisible = true
        binding.recyclerView.apply {
            adapter = BrandLabelAdapter(viewModel.tabList).also {
                it.setCurrentPosition(viewModel.position)
                it.setOnLabelClickListener(this@HomeTopicFragment)
            }
            layoutManager = LinearLayoutManager(requireContext())

        }
        onLabelClicked(viewModel.position)
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