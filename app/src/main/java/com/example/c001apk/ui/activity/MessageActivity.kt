package com.example.c001apk.ui.activity

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.ThemeUtils
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.R
import com.example.c001apk.adapter.MessageContentAdapter
import com.example.c001apk.databinding.ActivityMessageBinding
import com.example.c001apk.ui.fragment.minterface.AppListener
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.RecyclerView.checkForGaps
import com.example.c001apk.util.RecyclerView.markItemDecorInsetsDirty
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.view.StaggerItemDecoration
import com.example.c001apk.viewmodel.AppViewModel
import java.lang.reflect.Method

class MessageActivity : BaseActivity<ActivityMessageBinding>(), AppListener {

    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var mAdapter: MessageContentAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var sLayoutManager: StaggeredGridLayoutManager
    private lateinit var mCheckForGapMethod: Method
    private lateinit var mMarkItemDecorInsetsDirtyMethod: Method

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.type = intent.getStringExtra("type")

        initBar()
        initView()
        initData()
        initRefresh()
        initScroll()

        viewModel.messageData.observe(this) { result ->
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
                        mAdapter.notifyItemChanged(viewModel.messageList.size)
                        return@observe
                    } else if (!feed.data.isNullOrEmpty()) {
                        if (viewModel.isRefreshing) viewModel.messageList.clear()
                        if (viewModel.isRefreshing || viewModel.isLoadMore) {
                            viewModel.listSize = viewModel.messageList.size
                            for (element in feed.data)
                                if (element.entityType == "feed"
                                    || element.entityType == "feed_reply"
                                    || element.entityType == "notification"
                                )
                                    if (!BlackListUtil.checkUid(element.uid))
                                        viewModel.messageList.add(element)
                        }
                        viewModel.loadState = mAdapter.LOADING_COMPLETE
                        mAdapter.setLoadState(viewModel.loadState, null)
                    } else if (feed.data?.isEmpty() == true) {
                        if (viewModel.isRefreshing) viewModel.messageList.clear()
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
                        mAdapter.notifyItemChanged(viewModel.messageList.size)
                    else
                        mAdapter.notifyItemRangeChanged(
                            viewModel.listSize,
                            viewModel.messageList.size - viewModel.listSize + 1
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

        viewModel.likeFeedData.observe(this) { result ->
            if (viewModel.isPostLikeFeed) {
                viewModel.isPostLikeFeed = false

                val response = result.getOrNull()
                if (response != null) {
                    if (response.data != null) {
                        viewModel.dataList[viewModel.likePosition].likenum = response.data.count
                        viewModel.dataList[viewModel.likePosition].userAction?.like = 1
                        mAdapter.notifyItemChanged(viewModel.likePosition, "like")
                    } else
                        Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

        viewModel.unLikeFeedData.observe(this) { result ->
            if (viewModel.isPostUnLikeFeed) {
                viewModel.isPostUnLikeFeed = false

                val response = result.getOrNull()
                if (response != null) {
                    if (response.data != null) {
                        viewModel.dataList[viewModel.likePosition].likenum = response.data.count
                        viewModel.dataList[viewModel.likePosition].userAction?.like = 0
                        mAdapter.notifyItemChanged(viewModel.likePosition, "like")
                    } else
                        Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }

    private fun initBar() {
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        when (viewModel.type) {
            "atMe" -> {
                binding.toolBar.title = "@我的动态"
            }

            "atCommentMe" -> {
                binding.toolBar.title = "@我的评论"
            }

            "feedLike" -> {
                binding.toolBar.title = "我收到的赞"
            }

            "contactsFollow" -> {
                binding.toolBar.title = "好友关注"
            }

            "list" -> {
                binding.toolBar.title = "私信"
            }
        }
    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                    if (viewModel.messageList.isNotEmpty() && !viewModel.isEnd) {
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

                    if (viewModel.lastVisibleItemPosition == viewModel.messageList.size
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
        mAdapter.notifyItemChanged(viewModel.messageList.size)
        viewModel.isLoadMore = true
        viewModel.isNew = true
        viewModel.getMessage()
    }

    @SuppressLint("RestrictedApi")
    private fun initRefresh() {
        binding.swipeRefresh.setColorSchemeColors(
            ThemeUtils.getThemeAttrColor(
                this, rikka.preference.simplemenu.R.attr.colorPrimary
            )
        )
        binding.swipeRefresh.setOnRefreshListener {
            binding.indicator.parent.isIndeterminate = false
            binding.indicator.parent.visibility = View.GONE
            refreshData()
        }
    }

    private fun initData() {
        if (viewModel.messageList.isEmpty()) {
            binding.indicator.parent.isIndeterminate = true
            binding.indicator.parent.visibility = View.VISIBLE
            refreshData()
        } else {
            mAdapter.setLoadState(viewModel.loadState, viewModel.errorMessage)
            mAdapter.notifyItemChanged(viewModel.messageList.size)
        }
    }

    private fun initView() {
        mAdapter =
            MessageContentAdapter(this, viewModel.type.toString(), viewModel.messageList)
        mAdapter.setAppListener(this)
        mLayoutManager = LinearLayoutManager(this)
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

    private fun refreshData() {
        viewModel.firstVisibleItemPosition = -1
        viewModel.lastVisibleItemPosition = -1
        viewModel.page = 1
        viewModel.isRefreshing = true
        viewModel.isEnd = false
        viewModel.isNew = true
        when (viewModel.type) {
            "atMe" -> viewModel.url = "/v6/notification/atMeList"
            "atCommentMe" -> viewModel.url = "/v6/notification/atCommentMeList"
            "feedLike" -> viewModel.url = "/v6/notification/feedLikeList"
            "contactsFollow" -> viewModel.url = "/v6/notification/contactsFollowList"
            "list" -> viewModel.url = "/v6/message/list"

        }
        viewModel.getMessage()
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

    override fun onReload() {
        viewModel.isEnd = false
        loadMore()
    }

}