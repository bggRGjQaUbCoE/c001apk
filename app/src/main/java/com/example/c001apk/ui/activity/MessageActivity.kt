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
import com.example.c001apk.R
import com.example.c001apk.adapter.MessageContentAdapter
import com.example.c001apk.databinding.ActivityMessageBinding
import com.example.c001apk.ui.fragment.minterface.AppListener
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.view.StaggerItemDecoration
import com.example.c001apk.viewmodel.AppViewModel
import java.lang.reflect.Method

class MessageActivity : BaseActivity(), AppListener {

    private lateinit var binding: ActivityMessageBinding
    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var messageContentAdapter: MessageContentAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var sLayoutManager: StaggeredGridLayoutManager
    private lateinit var mCheckForGapMethod: Method
    private lateinit var mMarkItemDecorInsetsDirtyMethod: Method

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.type = intent.getStringExtra("type")!!

        initBar()
        initView()
        initData()
        initRefresh()
        initScroll()

        viewModel.messageData.observe(this) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val feed = result.getOrNull()
                if (!feed.isNullOrEmpty()) {
                    if (viewModel.isRefreshing) viewModel.messageList.clear()
                    if (viewModel.isRefreshing || viewModel.isLoadMore) {
                        for (element in feed)
                            if (element.entityType == "feed"
                                || element.entityType == "feed_reply"
                                || element.entityType == "notification"
                            )
                                if (!BlackListUtil.checkUid(element.uid))
                                    viewModel.messageList.add(element)
                    }
                    messageContentAdapter.setLoadState(messageContentAdapter.LOADING_COMPLETE)
                } else {
                    messageContentAdapter.setLoadState(messageContentAdapter.LOADING_END)
                    viewModel.isEnd = true
                    result.exceptionOrNull()?.printStackTrace()
                }
                messageContentAdapter.notifyDataSetChanged()
                binding.indicator.isIndeterminate = false
                binding.indicator.visibility = View.GONE
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
                        messageContentAdapter.notifyItemChanged(viewModel.likePosition, "like")
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
                        messageContentAdapter.notifyItemChanged(viewModel.likePosition, "like")
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
            @SuppressLint("NotifyDataSetChanged")
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (viewModel.lastVisibleItemPosition == viewModel.messageList.size
                        && !viewModel.isEnd && !viewModel.isRefreshing && !viewModel.isLoadMore
                    ) {
                        messageContentAdapter.setLoadState(messageContentAdapter.LOADING)
                        messageContentAdapter.notifyDataSetChanged()
                        viewModel.isLoadMore = true
                        viewModel.page++
                        viewModel.isNew = true
                        viewModel.getMessage()

                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (viewModel.messageList.isNotEmpty()) {
                    if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        viewModel.lastVisibleItemPosition =
                            mLayoutManager.findLastVisibleItemPosition()
                        viewModel.firstCompletelyVisibleItemPosition =
                            mLayoutManager.findFirstCompletelyVisibleItemPosition()
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
            }
        })
    }

    @SuppressLint("RestrictedApi")
    private fun initRefresh() {
        binding.swipeRefresh.setColorSchemeColors(
            ThemeUtils.getThemeAttrColor(
                this, rikka.preference.simplemenu.R.attr.colorPrimary
            )
        )
        binding.swipeRefresh.setOnRefreshListener {
            binding.indicator.isIndeterminate = false
            binding.indicator.visibility = View.GONE
            refreshData()
        }
    }

    private fun initData() {
        if (viewModel.messageList.isEmpty()) {
            binding.indicator.isIndeterminate = true
            binding.indicator.visibility = View.VISIBLE
            refreshData()
        }
    }

    private fun initView() {
        val space = resources.getDimensionPixelSize(R.dimen.normal_space)
        messageContentAdapter = MessageContentAdapter(this, viewModel.type, viewModel.messageList)
        messageContentAdapter.setAppListener(this)
        mLayoutManager = LinearLayoutManager(this)
        sLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // https://codeantenna.com/a/2NDTnG37Vg
            mCheckForGapMethod =
                StaggeredGridLayoutManager::class.java.getDeclaredMethod("checkForGaps")
            mCheckForGapMethod.isAccessible = true
            mMarkItemDecorInsetsDirtyMethod =
                RecyclerView::class.java.getDeclaredMethod("markItemDecorInsetsDirty")
            mMarkItemDecorInsetsDirtyMethod.isAccessible = true
        }
        binding.recyclerView.apply {
            adapter = messageContentAdapter
            layoutManager =
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                    mLayoutManager
                else sLayoutManager
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                addItemDecoration(LinearItemDecoration(space))
            else
                addItemDecoration(StaggerItemDecoration(space))
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

    override fun onDeleteReply(id: String, position: Int, rPosition: Int?) {}

    override fun onShowCollection(id: String, title: String) {}

}