package com.example.c001apk.ui.collection

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
import com.example.c001apk.R
import com.example.c001apk.adapter.AppAdapter
import com.example.c001apk.adapter.FooterAdapter
import com.example.c001apk.adapter.FooterState
import com.example.c001apk.adapter.HeaderAdapter
import com.example.c001apk.adapter.ItemListener
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.constant.Constants.SZLM_ID
import com.example.c001apk.databinding.FragmentCollectionBinding
import com.example.c001apk.logic.model.Like
import com.example.c001apk.ui.base.BaseFragment
import com.example.c001apk.ui.coolpic.CoolPicActivity
import com.example.c001apk.ui.home.IOnTabClickListener
import com.example.c001apk.util.PrefManager
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.view.StaggerItemDecoration
import com.google.android.material.color.MaterialColors
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CollectionFragment : BaseFragment<FragmentCollectionBinding>(), IOnTabClickListener {

    private val id by lazy { arguments?.getString("id").orEmpty() }
    private val title by lazy { arguments?.getString("title").orEmpty() }

    @Inject
    lateinit var viewModelAssistedFactory: CollectionViewModel.Factory
    private val viewModel by viewModels<CollectionViewModel> {
        CollectionViewModel.provideFactory(
            viewModelAssistedFactory,
            id,
            title,
            if (id.isEmpty()) "/v6/collection/list"
            else if (id == "recommend") "/v6/picture/list?tag=$title&type=recommend"
            else if (id == "hot") "/v6/picture/list?tag=$title&type=hot"
            else if (id == "newest") "/v6/picture/list?tag=$title&type=newest"
            else "/v6/collection/itemList"
        )
    }
    private lateinit var mAdapter: AppAdapter
    private lateinit var footerAdapter: FooterAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var sLayoutManager: StaggeredGridLayoutManager

    companion object {
        @JvmStatic
        fun newInstance(id: String, title: String) =
            CollectionFragment().apply {
                arguments = Bundle().apply {
                    putString("id", id)
                    putString("title", title)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appBar.setLiftable(true)

        initBar()
        if (!viewModel.isInit) {
            initView()
            initRefresh()
            initScroll()
            initObserve()
            initError()
        }

    }

    private fun initError() {
        binding.errorLayout.retry.setOnClickListener {
            binding.errorLayout.parent.isVisible = false
            viewModel.loadingState.value = LoadingState.Loading
        }
    }

    private fun initBar() {
        if (viewModel.id == "recommend" || viewModel.id == "hot" || viewModel.id == "newest")
            binding.appBar.isVisible = false
        else
            binding.toolBar.apply {
                title = viewModel.title.ifEmpty { "我的收藏单" }
                setNavigationIcon(R.drawable.ic_back)
                setNavigationOnClickListener {
                    if (viewModel.id.isEmpty())
                        requireActivity().finish()
                    else
                        requireActivity().supportFragmentManager.popBackStack()
                }
            }
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
                        isVisible = true
                        text = it.errMsg
                    }
                }

                is LoadingState.LoadingFailed -> {
                    binding.errorLayout.apply {
                        parent.isVisible = true
                        msg.text = it.msg
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

        viewModel.dataListData.observe(viewLifecycleOwner) {
            viewModel.listSize = it.size
            mAdapter.submitList(it)
        }

        viewModel.toastText.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initView() {
        mAdapter = AppAdapter(viewModel.repository, ItemClickListener())
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
        viewModel.fetchCollectionList()
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

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                    if (viewModel.listSize != -1 && !viewModel.isEnd && isAdded) {
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
        viewModel.fetchCollectionList()
    }

    inner class ReloadListener : FooterAdapter.FooterListener {
        override fun onReLoad() {
            loadMore()
        }
    }

    inner class ItemClickListener : ItemListener {
        override fun onViewFeed(
            view: View,
            id: String?,
            uid: String?,
            username: String?,
            userAvatar: String?,
            deviceTitle: String?,
            message: String?,
            dateline: String?,
            rid: Any?,
            isViewReply: Any?
        ) {
            super.onViewFeed(
                view,
                id,
                uid,
                username,
                userAvatar,
                deviceTitle,
                message,
                dateline,
                rid,
                isViewReply
            )
            if (!uid.isNullOrEmpty() && PrefManager.isRecordHistory)
                viewModel.saveHistory(
                    id.toString(), uid.toString(), username.toString(), userAvatar.toString(),
                    deviceTitle.toString(), message.toString(), dateline.toString()
                )
        }

        override fun onShowCollection(id: String, title: String) {
            requireActivity().supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(
                    R.anim.right_in,
                    R.anim.left_out_fragment,
                    R.anim.left_in,
                    R.anim.right_out
                )
                .replace(
                    R.id.fragment,
                    newInstance(id, title)
                )
                .addToBackStack(null)
                .commit()
        }

        override fun onLikeClick(type: String, id: String, position: Int, likeData: Like) {
            if (PrefManager.isLogin) {
                if (PrefManager.SZLMID.isEmpty())
                    Toast.makeText(requireContext(), SZLM_ID, Toast.LENGTH_SHORT).show()
                else viewModel.onPostLikeFeed(id, position, likeData)
            }
        }

        override fun onBlockUser(id: String, uid: String, position: Int) {
            viewModel.saveUid(uid)
            val currentList = viewModel.dataListData.value?.toMutableList() ?: ArrayList()
            currentList.removeAt(position)
            viewModel.dataListData.postValue(currentList)
        }

        override fun onDeleteClicked(entityType: String, id: String, position: Int) {
            viewModel.onDeleteFeed("/v6/feed/deleteFeed", id, position)
        }
    }

    override fun onResume() {
        super.onResume()
        initLift()
        if (activity is CoolPicActivity)
            (activity as? CoolPicActivity)?.tabController = this
        if (viewModel.isInit) {
            viewModel.isInit = false
            initView()
            viewModel.loadingState.value = LoadingState.Loading
            initRefresh()
            initScroll()
            initObserve()
            initError()
        }
    }

    override fun onStart() {
        super.onStart()
        initLift()
    }

    override fun onStop() {
        super.onStop()
        detachLift()
    }

    override fun onPause() {
        super.onPause()
        detachLift()

        if (activity is CoolPicActivity)
            (activity as? CoolPicActivity)?.tabController = null

    }

    private fun detachLift() {
        binding.recyclerView.borderViewDelegate.borderVisibilityChangedListener = null
    }

    private fun initLift() {
        if (activity is CoolPicActivity) {
            val parent = activity as CoolPicActivity
            parent.binding.appBar.setLifted(
                !binding.recyclerView.borderViewDelegate.isShowingTopBorder
            )
            binding.recyclerView.borderViewDelegate
                .setBorderVisibilityChangedListener { top, _, _, _ ->
                    parent.binding.appBar.setLifted(!top)
                }
        } else {
            binding.appBar.setLifted(
                !binding.recyclerView.borderViewDelegate.isShowingTopBorder
            )
            binding.recyclerView.borderViewDelegate
                .setBorderVisibilityChangedListener { top, _, _, _ ->
                    binding.appBar.setLifted(!top)
                }
        }
    }

    override fun onReturnTop(isRefresh: Boolean?) {
        binding.swipeRefresh.isRefreshing = true
        binding.recyclerView.scrollToPosition(0)
        refreshData()
    }

}