package com.example.c001apk.ui.fragment

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.ThemeUtils
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.R
import com.example.c001apk.adapter.AppAdapter
import com.example.c001apk.databinding.FragmentTopicContentBinding
import com.example.c001apk.ui.fragment.minterface.AppListener
import com.example.c001apk.ui.fragment.minterface.IOnTabClickContainer
import com.example.c001apk.ui.fragment.minterface.IOnTabClickListener
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.RecyclerView.checkForGaps
import com.example.c001apk.util.RecyclerView.markItemDecorInsetsDirty
import com.example.c001apk.util.TopicBlackListUtil
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.view.StaggerItemDecoration
import com.example.c001apk.viewmodel.AppViewModel
import java.lang.reflect.Method

class AppFragment : BaseFragment<FragmentTopicContentBinding>(), AppListener, IOnTabClickListener {

    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var mAdapter: AppAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var sLayoutManager: StaggeredGridLayoutManager
    private lateinit var mCheckForGapMethod: Method
    private lateinit var mMarkItemDecorInsetsDirtyMethod: Method

    companion object {
        @JvmStatic
        fun newInstance(type: String, id: String) = AppFragment().apply {
            arguments = Bundle().apply {
                putString("type", type)
                putString("id", id)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.type = it.getString("type")
            viewModel.appId = it.getString("id")
            viewModel.appCommentSort = when (viewModel.type) {
                "reply" -> ""
                "hot" -> "&sort=popular"
                else -> "&sort=dateline_desc"
            }
            viewModel.appCommentTitle = when (viewModel.type) {
                "reply" -> "最近回复"
                "hot" -> "热度排序"
                else -> "最新发布"
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!viewModel.isInit) {
            initView()
            initData()
            initRefresh()
            initScroll()
        }

        viewModel.appCommentData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val comment = result.getOrNull()
                if (!comment?.data.isNullOrEmpty()) {
                    if (viewModel.isRefreshing) viewModel.appCommentList.clear()
                    if (viewModel.isRefreshing || viewModel.isLoadMore) {
                        viewModel.listSize = viewModel.appCommentList.size
                        for (element in comment?.data!!)
                            if (element.entityType == "feed")
                                if (!BlackListUtil.checkUid(
                                        element.userInfo?.uid.toString()
                                    ) && !TopicBlackListUtil.checkTopic(
                                        element.tags + element.ttitle
                                    )
                                )
                                    viewModel.appCommentList.add(element)
                    }
                    viewModel.loadState = mAdapter.LOADING_COMPLETE
                    mAdapter.setLoadState(viewModel.loadState, null)
                } else if (comment?.data?.isEmpty() == true) {
                    if (viewModel.isRefreshing) viewModel.appCommentList.clear()
                    viewModel.loadState = mAdapter.LOADING_END
                    mAdapter.setLoadState(viewModel.loadState, null)
                    viewModel.isEnd = true
                } else {
                    viewModel.loadState = mAdapter.LOADING_ERROR
                    viewModel.errorMessage = getString(R.string.loading_failed)
                    mAdapter.setLoadState(viewModel.loadState, viewModel.errorMessage)
                    viewModel.isEnd = true
                    result.exceptionOrNull()?.printStackTrace()
                }
                if (viewModel.isLoadMore) if (viewModel.isEnd) mAdapter.notifyItemChanged(viewModel.appCommentList.size)
                else mAdapter.notifyItemRangeChanged(
                    viewModel.listSize, viewModel.appCommentList.size - viewModel.listSize + 1
                )
                else mAdapter.notifyDataSetChanged()
                binding.indicator.parent.isIndeterminate = false
                binding.indicator.parent.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                viewModel.isRefreshing = false
                viewModel.isLoadMore = false
            }
        }

        viewModel.likeFeedData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isPostLikeFeed) {
                viewModel.isPostLikeFeed = false

                val response = result.getOrNull()
                if (response != null) {
                    if (response.data != null) {
                        viewModel.appCommentList[viewModel.likePosition].likenum =
                            response.data.count
                        viewModel.appCommentList[viewModel.likePosition].userAction?.like = 1
                        mAdapter.notifyItemChanged(viewModel.likePosition, "like")
                    } else Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT)
                        .show()
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

        viewModel.unLikeFeedData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isPostUnLikeFeed) {
                viewModel.isPostUnLikeFeed = false

                val response = result.getOrNull()
                if (response != null) {
                    if (response.data != null) {
                        viewModel.appCommentList[viewModel.likePosition].likenum =
                            response.data.count
                        viewModel.appCommentList[viewModel.likePosition].userAction?.like = 0
                        mAdapter.notifyItemChanged(viewModel.likePosition, "like")
                    } else Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT)
                        .show()
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

    }

    private fun initData() {
        if (viewModel.appCommentList.isEmpty()) {
            binding.indicator.parent.visibility = View.VISIBLE
            binding.indicator.parent.isIndeterminate = true
            refreshData()
        } else {
            mAdapter.setLoadState(viewModel.loadState, viewModel.errorMessage)
            mAdapter.notifyItemChanged(viewModel.appCommentList.size)
        }
    }

    private fun initView() {
        mAdapter = AppAdapter(requireContext(), viewModel.appCommentList)
        mAdapter.setAppListener(this)
        mLayoutManager = LinearLayoutManager(activity)
        sLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // https://codeantenna.com/a/2NDTnG37Vg
            mCheckForGapMethod = checkForGaps
            mCheckForGapMethod.isAccessible = true
            mMarkItemDecorInsetsDirtyMethod = markItemDecorInsetsDirty
            mMarkItemDecorInsetsDirtyMethod.isAccessible = true
        }
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager =
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) mLayoutManager
                else sLayoutManager
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) addItemDecoration(
                LinearItemDecoration(10.dp)
            )
            else addItemDecoration(StaggerItemDecoration(10.dp))
        }
    }

    private fun refreshData() {
        viewModel.firstVisibleItemPosition = -1
        viewModel.lastVisibleItemPosition = -1
        viewModel.page = 1
        viewModel.isEnd = false
        viewModel.isRefreshing = true
        viewModel.isLoadMore = false
        viewModel.isNew = true
        viewModel.getAppComment()
    }

    @SuppressLint("RestrictedApi")
    private fun initRefresh() {
        binding.swipeRefresh.setColorSchemeColors(
            ThemeUtils.getThemeAttrColor(
                requireContext(), rikka.preference.simplemenu.R.attr.colorPrimary
            )
        )
        binding.swipeRefresh.setOnRefreshListener {
            refreshData()
        }
    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                    if (viewModel.appCommentList.isNotEmpty()) {
                        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                            viewModel.lastVisibleItemPosition =
                                mLayoutManager.findLastVisibleItemPosition()
                            viewModel.firstCompletelyVisibleItemPosition =
                                mLayoutManager.findFirstCompletelyVisibleItemPosition()
                        } else {
                            val result =
                                mCheckForGapMethod.invoke(binding.recyclerView.layoutManager) as Boolean
                            if (result) mMarkItemDecorInsetsDirtyMethod.invoke(binding.recyclerView)

                            val positions = sLayoutManager.findLastVisibleItemPositions(null)
                            for (pos in positions) {
                                if (pos > viewModel.lastVisibleItemPosition) {
                                    viewModel.lastVisibleItemPosition = pos
                                }
                            }
                        }
                    }

                    if (viewModel.lastVisibleItemPosition == viewModel.appCommentList.size
                        && !viewModel.isEnd && !viewModel.isRefreshing && !viewModel.isLoadMore
                    ) {
                        viewModel.page++
                        loadMore()
                    }
                }
            }
        })
    }

    private fun loadMore() {
        viewModel.loadState = mAdapter.LOADING
        mAdapter.setLoadState(viewModel.loadState, null)
        mAdapter.notifyItemChanged(viewModel.appCommentList.size)
        viewModel.isLoadMore = true
        viewModel.isNew = true
        viewModel.getAppComment()
    }

    override fun onShowTotalReply(position: Int, uid: String, id: String, rPosition: Int?) {}

    override fun onPostFollow(isFollow: Boolean, uid: String, position: Int) {}

    override fun onReply2Reply(
        rPosition: Int, r2rPosition: Int?, id: String, uid: String, uname: String, type: String
    ) {
    }

    override fun onPostLike(type: String?, isLike: Boolean, id: String, position: Int?) {
        viewModel.likeFeedId = id
        viewModel.likePosition = position!!
        if (isLike) {
            viewModel.isPostUnLikeFeed = true
            viewModel.postUnLikeFeed()
        } else {
            viewModel.isPostLikeFeed = true
            viewModel.postLikeFeed()
        }
    }

    override fun onRefreshReply(listType: String) {}

    override fun onDeleteFeedReply(id: String, position: Int, rPosition: Int?) {}

    override fun onShowCollection(id: String, title: String) {}

    override fun onResume() {
        super.onResume()

        (requireContext() as IOnTabClickContainer).tabController = this

        if (viewModel.isInit) {
            viewModel.isInit = false
            initView()
            initData()
            initRefresh()
            initScroll()
        }

    }

    override fun onReturnTop(isRefresh: Boolean?) {
        if (viewModel.firstCompletelyVisibleItemPosition == 0) {
            binding.swipeRefresh.isRefreshing = true
            refreshData()
        } else binding.recyclerView.scrollToPosition(0)
    }

    override fun onReload() {
        viewModel.isEnd = false
        loadMore()
    }

}