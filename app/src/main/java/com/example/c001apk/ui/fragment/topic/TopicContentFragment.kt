package com.example.c001apk.ui.fragment.topic

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
import com.example.c001apk.ui.fragment.BaseFragment
import com.example.c001apk.ui.fragment.minterface.AppListener
import com.example.c001apk.ui.fragment.minterface.IOnSearchMenuClickContainer
import com.example.c001apk.ui.fragment.minterface.IOnSearchMenuClickListener
import com.example.c001apk.ui.fragment.minterface.IOnTabClickContainer
import com.example.c001apk.ui.fragment.minterface.IOnTabClickListener
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.TopicBlackListUtil
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.view.StaggerItemDecoration
import com.example.c001apk.viewmodel.AppViewModel

class TopicContentFragment : BaseFragment<FragmentTopicContentBinding>(), AppListener,
    IOnSearchMenuClickListener, IOnTabClickListener {

    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var mAdapter: AppAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var sLayoutManager: StaggeredGridLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.url = it.getString("url")
            viewModel.title = it.getString("title")
            viewModel.isEnable = it.getBoolean("isEnable")
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(url: String, title: String, isEnable: Boolean) =
            TopicContentFragment().apply {
                arguments = Bundle().apply {
                    putString("url", url)
                    putString("title", title)
                    putBoolean("isEnable", isEnable)
                }
            }
    }

    override fun onPause() {
        super.onPause()
        if (viewModel.isEnable)
            (requireParentFragment() as IOnTabClickContainer).tabController = null

        if (viewModel.title == "讨论")
            (requireParentFragment() as IOnSearchMenuClickContainer).controller = null
    }

    override fun onResume() {
        super.onResume()

        if (viewModel.isEnable)
            (requireParentFragment() as IOnTabClickContainer).tabController = this

        if (viewModel.title == "讨论")
            (requireParentFragment() as IOnSearchMenuClickContainer).controller = this

        if (viewModel.isInit) {
            viewModel.isInit = false
            initView()
            initData()
            initRefresh()
            initScroll()
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

        viewModel.topicDataLiveData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val data = result.getOrNull()
                if (data != null) {
                    if (!data.message.isNullOrEmpty()) {
                        viewModel.loadState = mAdapter.LOADING_ERROR
                        viewModel.errorMessage = data.message
                        mAdapter.setLoadState(viewModel.loadState, viewModel.errorMessage)
                        viewModel.isEnd = true
                        viewModel.isLoadMore = false
                        viewModel.isRefreshing = false
                        binding.swipeRefresh.isRefreshing = false
                        binding.indicator.parent.isIndeterminate = false
                        binding.indicator.parent.visibility = View.GONE
                        mAdapter.notifyItemChanged(viewModel.topicDataList.size)
                        return@observe
                    } else if (!data.data.isNullOrEmpty()) {
                        if (viewModel.isRefreshing)
                            viewModel.topicDataList.clear()
                        if (viewModel.isRefreshing || viewModel.isLoadMore) {
                            viewModel.lastItem = data.data.last().id
                            viewModel.listSize = viewModel.topicDataList.size
                            for (element in data.data) {
                                if (element.id == viewModel.lastItem)
                                    continue
                                if (element.entityType == "feed"
                                    || element.entityType == "topic"
                                    || element.entityType == "product"
                                    || element.entityType == "user"
                                )
                                    if (!BlackListUtil.checkUid(element.userInfo?.uid.toString())
                                        && !TopicBlackListUtil.checkTopic(
                                            element.tags + element.ttitle
                                        )
                                    )
                                        viewModel.topicDataList.add(element)
                            }
                        }
                        viewModel.loadState = mAdapter.LOADING_COMPLETE
                        mAdapter.setLoadState(viewModel.loadState, null)
                    } else if (data.data?.isEmpty() == true) {
                        if (viewModel.isRefreshing)
                            viewModel.topicDataList.clear()
                        viewModel.loadState = mAdapter.LOADING_END
                        mAdapter.setLoadState(viewModel.loadState, null)
                        viewModel.isEnd = true
                    }
                } else {
                    viewModel.errorMessage = getString(R.string.loading_failed)
                    viewModel.loadState = mAdapter.LOADING_ERROR
                    mAdapter.setLoadState(viewModel.loadState, viewModel.errorMessage)
                    viewModel.isEnd = true
                    result.exceptionOrNull()?.printStackTrace()
                }
                if (viewModel.isLoadMore)
                    if (viewModel.isEnd)
                        mAdapter.notifyItemChanged(viewModel.topicDataList.size)
                    else
                        mAdapter.notifyItemRangeChanged(
                            viewModel.listSize,
                            viewModel.topicDataList.size - viewModel.listSize + 1
                        )
                else
                    mAdapter.notifyDataSetChanged()
                binding.indicator.parent.isIndeterminate = false
                binding.indicator.parent.visibility = View.GONE
                viewModel.isLoadMore = false
                viewModel.isRefreshing = false
                binding.swipeRefresh.isRefreshing = false
            }
        }

        viewModel.likeFeedData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isPostLikeFeed) {
                viewModel.isPostLikeFeed = false

                val response = result.getOrNull()
                if (response != null) {
                    if (response.data != null) {
                        viewModel.topicDataList[viewModel.likePosition].likenum =
                            response.data.count
                        viewModel.topicDataList[viewModel.likePosition].userAction?.like = 1
                        mAdapter.notifyItemChanged(viewModel.likePosition, "like")
                    } else
                        Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT)
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
                        viewModel.topicDataList[viewModel.likePosition].likenum =
                            response.data.count
                        viewModel.topicDataList[viewModel.likePosition].userAction?.like = 0
                        mAdapter.notifyItemChanged(viewModel.likePosition, "like")
                    } else
                        Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT)
                            .show()
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                    if (viewModel.topicDataList.isNotEmpty() && isAdded) {
                        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                            viewModel.lastVisibleItemPosition =
                                mLayoutManager.findLastVisibleItemPosition()
                            viewModel.firstCompletelyVisibleItemPosition =
                                mLayoutManager.findFirstCompletelyVisibleItemPosition()
                        } else {
                            val positions = sLayoutManager.findLastVisibleItemPositions(null)
                            for (pos in positions) {
                                if (pos > viewModel.lastVisibleItemPosition) {
                                    viewModel.lastVisibleItemPosition = pos
                                }
                            }
                        }
                    }

                    if (viewModel.lastVisibleItemPosition == viewModel.topicDataList.size
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
        mAdapter.notifyItemChanged(viewModel.topicDataList.size)
        viewModel.isLoadMore = true
        viewModel.isNew = true
        viewModel.getTopicData()
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
        mAdapter = AppAdapter(requireContext(), viewModel.topicDataList)
        mAdapter.setAppListener(this)
        mLayoutManager = LinearLayoutManager(requireContext())
        sLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager =
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                    mLayoutManager
                else sLayoutManager
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                addItemDecoration(LinearItemDecoration(10.dp))
            else
                addItemDecoration(StaggerItemDecoration(10.dp))
        }
    }

    private fun initData() {
        if (viewModel.topicDataList.isEmpty()) {
            binding.indicator.parent.visibility = View.VISIBLE
            binding.indicator.parent.isIndeterminate = true
            refreshData()
        } else {
            mAdapter.setLoadState(viewModel.loadState, viewModel.errorMessage)
            mAdapter.notifyItemChanged(viewModel.topicDataList.size)
        }
    }

    private fun refreshData() {
        viewModel.subtitle = ""
        viewModel.firstVisibleItemPosition = -1
        viewModel.lastVisibleItemPosition = -1
        viewModel.lastItem = null
        viewModel.page = 1
        viewModel.isEnd = false
        viewModel.isRefreshing = true
        viewModel.isLoadMore = false
        viewModel.isNew = true
        viewModel.getTopicData()
    }

    override fun onShowTotalReply(position: Int, uid: String, id: String, rPosition: Int?) {}

    override fun onPostFollow(isFollow: Boolean, uid: String, position: Int) {}

    override fun onReply2Reply(
        rPosition: Int,
        r2rPosition: Int?,
        id: String,
        uid: String,
        uname: String,
        type: String
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

    @SuppressLint("NotifyDataSetChanged")
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
        viewModel.topicDataList.clear()
        mAdapter.notifyDataSetChanged()
        binding.indicator.parent.visibility = View.VISIBLE
        binding.indicator.parent.isIndeterminate = true
        refreshData()
    }

    override fun onReturnTop(isRefresh: Boolean?) {
        binding.recyclerView.stopScroll()
        if (viewModel.firstCompletelyVisibleItemPosition == 0) {
            binding.swipeRefresh.isRefreshing = true
            refreshData()
        } else {
            viewModel.firstCompletelyVisibleItemPosition = 0
            binding.recyclerView.scrollToPosition(0)
        }
    }

    override fun onReload() {
        viewModel.isEnd = false
        loadMore()
    }

}