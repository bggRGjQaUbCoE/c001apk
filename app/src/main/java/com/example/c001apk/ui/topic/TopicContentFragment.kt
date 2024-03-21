package com.example.c001apk.ui.topic

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.adapter.AppAdapter
import com.example.c001apk.adapter.FooterAdapter
import com.example.c001apk.adapter.FooterState
import com.example.c001apk.adapter.HeaderAdapter
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.databinding.FragmentTopicContentBinding
import com.example.c001apk.ui.base.BaseFragment
import com.example.c001apk.ui.carousel.CarouselActivity
import com.example.c001apk.ui.home.IOnTabClickContainer
import com.example.c001apk.ui.home.IOnTabClickListener
import com.example.c001apk.ui.search.IOnSearchMenuClickContainer
import com.example.c001apk.ui.search.IOnSearchMenuClickListener
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.view.StaggerItemDecoration
import com.google.android.material.color.MaterialColors
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TopicContentFragment : BaseFragment<FragmentTopicContentBinding>(),
    IOnSearchMenuClickListener, IOnTabClickListener {

    @Inject
    lateinit var viewModelAssistedFactory: TopicContentViewModel.Factory
    private val viewModel by viewModels<TopicContentViewModel> {
        TopicContentViewModel.provideFactory(
            viewModelAssistedFactory,
            arguments?.getString("url").orEmpty(),
            arguments?.getString("title").orEmpty(),
        )
    }
    private lateinit var mAdapter: AppAdapter
    private lateinit var footerAdapter: FooterAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var sLayoutManager: StaggeredGridLayoutManager

    companion object {
        @JvmStatic
        fun newInstance(url: String, title: String) =
            TopicContentFragment().apply {
                arguments = Bundle().apply {
                    putString("url", url)
                    putString("title", title)
                }
            }
    }

    override fun onStop() {
        super.onStop()
        detachLift()
    }

    override fun onPause() {
        super.onPause()

        detachLift()

        if (parentFragment is TopicFragment)
            (parentFragment as? IOnTabClickContainer)?.tabController = null
        else if (activity is CarouselActivity)
            (activity as? IOnTabClickContainer)?.tabController = null

        if (viewModel.title == "讨论")
            (parentFragment as? IOnSearchMenuClickContainer)?.controller = null
    }

    override fun onResume() {
        super.onResume()

        if (parentFragment is TopicFragment)
            (parentFragment as? IOnTabClickContainer)?.tabController = this
        else if (activity is CarouselActivity)
            (activity as? IOnTabClickContainer)?.tabController = this

        if (viewModel.title == "讨论")
            (parentFragment as? IOnSearchMenuClickContainer)?.controller = this

        if (viewModel.isInit) {
            viewModel.isInit = false
            initView()
            viewModel.loadingState.value = LoadingState.Loading
            initRefresh()
            initScroll()
            initObserve()
            initError()
        }

        initLift()

    }

    private fun initError() {
        binding.errorLayout.retry.setOnClickListener {
            binding.errorLayout.parent.isVisible = false
            viewModel.loadingState.value = LoadingState.Loading
        }
    }

    override fun onStart() {
        super.onStart()
        initLift()
    }

    private fun initObserve() {
        viewModel.loadingState.observe(viewLifecycleOwner) {
            when (it) {
                LoadingState.Loading -> {
                    binding.indicator.parent.isIndeterminate = true
                    binding.indicator.parent.isVisible = true
                    refreshData()
                }

                LoadingState.LoadingDone -> {
                    binding.swipeRefresh.isEnabled = true
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
                        parent.isVisible = true
                    }
                }
            }
            if (it !is LoadingState.Loading) {
                binding.indicator.parent.isIndeterminate = false
                binding.indicator.parent.isVisible = false
            }
        }

        viewModel.footerState.observe(viewLifecycleOwner) {
            footerAdapter.setLoadState(it)
            if (it !is FooterState.Loading) {
                binding.swipeRefresh.isRefreshing = false
            }
        }

        viewModel.topicData.observe(viewLifecycleOwner) {
            viewModel.listSize = it.size
            mAdapter.submitList(it)
        }

        viewModel.toastText.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!viewModel.isInit) {
            initView()
            initRefresh()
            initScroll()
            initObserve()
            initError()
        }

    }

    private fun initLift() {
        if (activity is CarouselActivity) {
            val parent = activity as CarouselActivity
            parent.binding.appBar.setLifted(
                !binding.recyclerView.borderViewDelegate.isShowingTopBorder
            )
            binding.recyclerView.borderViewDelegate
                .setBorderVisibilityChangedListener { top, _, _, _ ->
                    parent.binding.appBar.setLifted(!top)
                }
        } else if (parentFragment is TopicFragment) {
            val parent = parentFragment as TopicFragment
            parent.binding.appBar.setLifted(
                !binding.recyclerView.borderViewDelegate.isShowingTopBorder
            )
            binding.recyclerView.borderViewDelegate
                .setBorderVisibilityChangedListener { top, _, _, _ ->
                    parent.binding.appBar.setLifted(!top)
                }
        }
    }

    private fun detachLift() {
        binding.recyclerView.borderViewDelegate.borderVisibilityChangedListener = null
    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                    if (viewModel.listSize != -1 && isAdded) {
                        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                            viewModel.lastVisibleItemPosition =
                                mLayoutManager.findLastVisibleItemPosition()
                        } else {
                            val positions = sLayoutManager.findLastVisibleItemPositions(null)
                            viewModel.lastVisibleItemPosition = positions[0]
                            positions.forEach { pos ->
                                if (pos > viewModel.lastVisibleItemPosition) {
                                    viewModel.lastVisibleItemPosition = pos
                                }
                            }
                        }
                    }

                    if (viewModel.lastVisibleItemPosition == viewModel.listSize + 1
                        && !viewModel.isEnd && !viewModel.isRefreshing && !viewModel.isLoadMore
                    ) {
                        loadMore()
                    }
                }
            }
        })
    }

    private fun loadMore() {
        viewModel.isLoadMore = true
        viewModel.fetchTopicData()
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
        mAdapter = AppAdapter(viewModel.repository, viewModel.ItemClickListener())
        footerAdapter = FooterAdapter(ReloadListener())
        binding.recyclerView.apply {
            adapter = ConcatAdapter(HeaderAdapter(), mAdapter, footerAdapter)
            layoutManager =
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    mLayoutManager = LinearLayoutManager(requireContext())
                    mLayoutManager
                } else {
                    sLayoutManager =
                        StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                    sLayoutManager
                }
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                addItemDecoration(LinearItemDecoration(10.dp))
            else
                addItemDecoration(StaggerItemDecoration(10.dp))
        }
    }

    private fun refreshData() {
        viewModel.lastVisibleItemPosition = 0
        viewModel.lastItem = null
        viewModel.page = 1
        viewModel.isEnd = false
        viewModel.isRefreshing = true
        viewModel.isLoadMore = false
        viewModel.fetchTopicData()
    }


    override fun onSearch(type: String, value: String, id: String?) {
        viewModel.title = value
        when (value) {
            "最近回复" -> viewModel.url =
                "/page?url=/product/feedList?type=feed&id=$id&ignoreEntityById=1"

            "热度排序" -> viewModel.url =
                "/page?url=/product/feedList?type=feed&id=$id&listType=rank_score"

            "最新发布" -> viewModel.url =
                "/page?url=/product/feedList?type=feed&id=$id&ignoreEntityById=1&listType=dateline_desc"
        }
        viewModel.topicData.postValue(emptyList())
        viewModel.footerState.value = FooterState.LoadingDone
        viewModel.loadingState.value = LoadingState.Loading
    }

    override fun onReturnTop(isRefresh: Boolean?) {
        binding.swipeRefresh.isRefreshing = true
        binding.recyclerView.scrollToPosition(0)
        refreshData()
    }

    inner class ReloadListener : FooterAdapter.FooterListener {
        override fun onReLoad() {
            loadMore()
        }
    }

}