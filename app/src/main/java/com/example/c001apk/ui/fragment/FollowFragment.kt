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
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.RecyclerView.checkForGaps
import com.example.c001apk.util.RecyclerView.markItemDecorInsetsDirty
import com.example.c001apk.util.TopicBlackListUtil
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.view.StaggerItemDecoration
import com.example.c001apk.viewmodel.AppViewModel
import java.lang.reflect.Method

class FollowFragment : BaseFragment<FragmentTopicContentBinding>(), AppListener {

    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var mAdapter: AppAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var sLayoutManager: StaggeredGridLayoutManager
    private lateinit var mCheckForGapMethod: Method
    private lateinit var mMarkItemDecorInsetsDirtyMethod: Method

    companion object {
        @JvmStatic
        fun newInstance(type: String) =
            FollowFragment().apply {
                arguments = Bundle().apply {
                    putString("TYPE", type)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.type = it.getString("TYPE")
        }
    }

    override fun onResume() {
        super.onResume()

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

        viewModel.listData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val feed = result.getOrNull()
                if (feed != null) {
                    if (!feed.message.isNullOrEmpty()) {
                        viewModel.loadState = mAdapter.LOADING_ERROR
                        viewModel.errorMessage = feed.message
                        mAdapter.setLoadState(viewModel.loadState, viewModel.errorMessage)
                        viewModel.isEnd = true
                        viewModel.isLoadMore = false
                        viewModel.isRefreshing = false
                        binding.indicator.parent.isIndeterminate = false
                        binding.indicator.parent.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing = false
                        mAdapter.notifyItemChanged(viewModel.dataList.size)
                        return@observe
                    } else if (!feed.data.isNullOrEmpty()) {
                        if (viewModel.isRefreshing) viewModel.dataList.clear()
                        if (viewModel.isRefreshing || viewModel.isLoadMore) {
                            viewModel.listSize = viewModel.dataList.size
                            for (element in feed.data)
                                if (element.entityType == "feed"
                                    || element.entityType == "contacts"
                                    || element.entityType == "apk"
                                    || element.entityType == "feed_reply"
                                )
                                    if (!BlackListUtil.checkUid(element.userInfo?.uid.toString())
                                        && !TopicBlackListUtil.checkTopic(
                                            element.tags + element.ttitle
                                        )
                                    )
                                        viewModel.dataList.add(element)
                        }
                        viewModel.loadState = mAdapter.LOADING_COMPLETE
                        mAdapter.setLoadState(viewModel.loadState, null)
                    } else if (feed.data?.isEmpty() == true) {
                        if (viewModel.isRefreshing) viewModel.dataList.clear()
                        viewModel.loadState = mAdapter.LOADING_END
                        mAdapter.setLoadState(viewModel.loadState, null)
                        viewModel.isEnd = true
                    }
                } else {
                    viewModel.loadState = mAdapter.LOADING_ERROR
                    viewModel.errorMessage = getString(R.string.loading_failed)
                    mAdapter.setLoadState(viewModel.loadState, viewModel.errorMessage)
                    viewModel.isEnd = true
                    result.exceptionOrNull()?.printStackTrace()
                }
                if (viewModel.isLoadMore)
                    if (viewModel.isEnd)
                        mAdapter.notifyItemChanged(viewModel.dataList.size)
                    else
                        mAdapter.notifyItemRangeChanged(
                            viewModel.listSize,
                            viewModel.dataList.size - viewModel.listSize + 1
                        )
                else
                    mAdapter.notifyDataSetChanged()
                binding.indicator.parent.isIndeterminate = false
                binding.indicator.parent.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                viewModel.isRefreshing = false
                viewModel.isLoadMore = false
            }
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
                        binding.indicator.parent.isIndeterminate = false
                        binding.indicator.parent.visibility = View.GONE
                        binding.swipeRefresh.isRefreshing = false
                        mAdapter.notifyItemChanged(viewModel.dataList.size)
                        return@observe
                    } else if (!data.data.isNullOrEmpty()) {
                        if (viewModel.isRefreshing)
                            viewModel.dataList.clear()
                        if (viewModel.isRefreshing || viewModel.isLoadMore) {
                            viewModel.listSize = viewModel.dataList.size
                            for (element in data.data)
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
                                        viewModel.dataList.add(element)
                        }
                        viewModel.loadState = mAdapter.LOADING_COMPLETE
                        mAdapter.setLoadState(viewModel.loadState, null)
                    } else if (data.data?.isEmpty() == true) {
                        if (viewModel.isRefreshing)
                            viewModel.dataList.clear()
                        viewModel.loadState = mAdapter.LOADING_END
                        mAdapter.setLoadState(viewModel.loadState, null)
                        viewModel.isEnd = true
                    }
                } else {
                    viewModel.loadState = mAdapter.LOADING_ERROR
                    viewModel.errorMessage = getString(R.string.loading_failed)
                    mAdapter.setLoadState(viewModel.loadState, viewModel.errorMessage)
                    viewModel.isEnd = true
                    result.exceptionOrNull()?.printStackTrace()
                }
                if (viewModel.isLoadMore)
                    if (viewModel.isEnd)
                        mAdapter.notifyItemChanged(viewModel.dataList.size)
                    else
                        mAdapter.notifyItemRangeChanged(
                            viewModel.listSize,
                            viewModel.dataList.size - viewModel.listSize + 1
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

        viewModel.postDeleteData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val response = result.getOrNull()
                if (response != null) {
                    if (response.data == "删除成功") {
                        Toast.makeText(requireContext(), response.data, Toast.LENGTH_SHORT).show()
                        viewModel.dataList.removeAt(viewModel.position)
                        mAdapter.notifyItemRemoved(viewModel.position)
                    } else if (!response.message.isNullOrEmpty()) {
                        Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

    }

    private fun initView() {
        mAdapter = AppAdapter(requireContext(), viewModel.dataList)
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
        if (viewModel.dataList.isEmpty()) {
            binding.indicator.parent.visibility = View.VISIBLE
            binding.indicator.parent.isIndeterminate = true
            refreshData()
        } else {
            mAdapter.setLoadState(viewModel.loadState, viewModel.errorMessage)
            mAdapter.notifyItemChanged(viewModel.dataList.size)
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
        when (viewModel.type) {
            "follow" -> {
                viewModel.getFeedList()
            }

            "apk" -> {
                viewModel.getFeedList()
            }

            "reply" -> {
                viewModel.getFeedList()
            }

            "replyToMe" -> {
                viewModel.getFeedList()
            }

            "topic" -> {
                viewModel.url = "#/topic/userFollowTagList"
                viewModel.title = "我关注的话题"
                viewModel.getTopicData()
            }

            "product" -> {
                viewModel.url = "#/product/followProductList"
                viewModel.title = "我关注的数码吧"
                viewModel.getTopicData()
            }

            "favorite" -> {
                viewModel.url = "#/collection/followList"
                viewModel.title = "我关注的收藏单"
                viewModel.getTopicData()
            }

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

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                    if (viewModel.dataList.isNotEmpty() && !viewModel.isEnd) {
                        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                            viewModel.lastVisibleItemPosition =
                                mLayoutManager.findLastVisibleItemPosition()
                        } else {
                            val result =
                                mCheckForGapMethod.invoke(binding.recyclerView.layoutManager) as Boolean
                            if (result)
                                mMarkItemDecorInsetsDirtyMethod.invoke(binding.recyclerView)

                            val positions = sLayoutManager.findLastVisibleItemPositions(null)
                            for (pos in positions) {
                                if (pos > viewModel.lastVisibleItemPosition) {
                                    viewModel.lastVisibleItemPosition = pos
                                }
                            }
                        }
                    }

                    if (viewModel.lastVisibleItemPosition == viewModel.dataList.size
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
        mAdapter.notifyItemChanged(viewModel.dataList.size)
        viewModel.isLoadMore = true
        viewModel.isNew = true
        when (viewModel.type) {
            "follow" -> {
                viewModel.getFeedList()
            }

            "apk" -> {
                viewModel.getFeedList()
            }

            "reply" -> {
                viewModel.getFeedList()
            }

            "replyToMe" -> {
                viewModel.getFeedList()
            }

            else -> {
                viewModel.getTopicData()
            }
        }
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

    override fun onPostLike(type: String?, isLike: Boolean, id: String, position: Int?) {}

    override fun onRefreshReply(listType: String) {}

    override fun onDeleteFeedReply(id: String, position: Int, rPosition: Int?) {
        viewModel.isNew = true
        viewModel.position = position
        viewModel.url = "/v6/feed/deleteReply"
        viewModel.deleteId = id
        viewModel.postDelete()
    }

    override fun onShowCollection(id: String, title: String) {}

    override fun onReload() {
        viewModel.isEnd = false
        loadMore()
    }

}