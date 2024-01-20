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
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.R
import com.example.c001apk.adapter.AppAdapter
import com.example.c001apk.databinding.ActivityFfflistBinding
import com.example.c001apk.ui.fragment.FollowFragment
import com.example.c001apk.ui.fragment.minterface.AppListener
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.TopicBlackListUtil
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.view.StaggerItemDecoration
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.tabs.TabLayoutMediator

class FFFListActivity : BaseActivity<ActivityFfflistBinding>(), AppListener {

    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var mAdapter: AppAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var sLayoutManager: StaggeredGridLayoutManager

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.isEnable = intent.getBooleanExtra("isEnable", false)
        viewModel.type = intent.getStringExtra("type")
        viewModel.uid = intent.getStringExtra("uid")

        initBar()
        if (viewModel.isEnable) {
            binding.tabLayout.visibility = View.VISIBLE
            binding.viewPager.visibility = View.VISIBLE
            binding.swipeRefresh.visibility = View.GONE
            binding.recyclerView.visibility = View.GONE
            if (viewModel.tabList.isEmpty()) {
                if (viewModel.type == "follow") {
                    viewModel.tabList.apply {
                        add("用户")
                        add("话题")
                        add("数码")
                        add("应用")
                    }
                } else if (viewModel.type == "reply") {
                    viewModel.tabList.apply {
                        add("我的回复")
                        add("我收到的回复")
                    }
                }
            }
            initViewPager()
        } else {
            binding.tabLayout.visibility = View.GONE
            binding.viewPager.visibility = View.GONE
            binding.swipeRefresh.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.VISIBLE
            initView()
            initData()
            initRefresh()
            initScroll()
        }

        viewModel.listData.observe(this) { result ->
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
                            for (element in feed.data) {
                                if (element.entityType == "feed"
                                    || element.entityType == "contacts"
                                    || element.entityType == "feed_reply"
                                    || element.entityType == "recentHistory"
                                )
                                    if (!BlackListUtil.checkUid(element.userInfo?.uid.toString())
                                        && !TopicBlackListUtil.checkTopic(
                                            element.tags + element.ttitle
                                        )
                                    )
                                        viewModel.dataList.add(element)
                            }
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

        viewModel.likeReplyData.observe(this) { result ->
            if (viewModel.isPostLikeReply) {
                viewModel.isPostLikeReply = false

                val response = result.getOrNull()
                if (response != null) {
                    if (response.data != null) {
                        viewModel.dataList[viewModel.likeReplyPosition].likenum =
                            response.data
                        viewModel.dataList[viewModel.likeReplyPosition].userAction?.like =
                            1
                        mAdapter.notifyItemChanged(viewModel.likeReplyPosition, "like")
                    } else
                        Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

        viewModel.unLikeReplyData.observe(this) { result ->
            if (viewModel.isPostUnLikeReply) {
                viewModel.isPostUnLikeReply = false

                val response = result.getOrNull()
                if (response != null) {
                    if (response.data != null) {
                        viewModel.dataList[viewModel.likeReplyPosition].likenum =
                            response.data
                        viewModel.dataList[viewModel.likeReplyPosition].userAction?.like =
                            0
                        mAdapter.notifyItemChanged(viewModel.likeReplyPosition, "like")
                    } else
                        Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

        viewModel.postDeleteData.observe(this) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val response = result.getOrNull()
                if (response != null) {
                    if (response.data == "删除成功") {
                        Toast.makeText(this, response.data, Toast.LENGTH_SHORT).show()
                        viewModel.dataList.removeAt(viewModel.position)
                        mAdapter.notifyItemRemoved(viewModel.position)
                    } else if (!response.message.isNullOrEmpty()) {
                        Toast.makeText(this, response.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

    }

    private fun initViewPager() {
        binding.viewPager.offscreenPageLimit = viewModel.tabList.size
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int) =
                if (viewModel.type == "follow") {
                    when (position) {
                        0 -> FollowFragment.newInstance("follow")
                        1 -> FollowFragment.newInstance("topic")
                        2 -> FollowFragment.newInstance("product")
                        3 -> FollowFragment.newInstance("apk")
                        else -> throw IllegalArgumentException()
                    }
                } else {
                    when (position) {
                        0 -> FollowFragment.newInstance("reply")
                        1 -> FollowFragment.newInstance("replyToMe")
                        else -> throw IllegalArgumentException()
                    }
                }

            override fun getItemCount() = viewModel.tabList.size

        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = viewModel.tabList[position]
        }.attach()
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
        binding.toolBar.title = when (viewModel.type) {
            "feed" -> "我的动态"

            "follow" -> {
                if (viewModel.uid == PrefManager.uid)
                    "我的关注"
                else
                    "TA关注的人"
            }

            "fans" -> {
                if (viewModel.uid == PrefManager.uid)
                    "关注我的人"
                else
                    "TA的粉丝"
            }

            "like" -> "我的赞"

            "reply" -> "我的回复"

            "recentHistory" -> "我的常去"

            else -> viewModel.type

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
                        viewModel.loadState = mAdapter.LOADING
                        loadMore()
                    }
                }
            }
        })
    }

    private fun loadMore() {
        mAdapter.setLoadState(viewModel.loadState, null)
        mAdapter.notifyItemChanged(viewModel.dataList.size)
        viewModel.isLoadMore = true
        viewModel.isNew = true
        viewModel.getFeedList()
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
        if (viewModel.dataList.isEmpty()) {
            binding.indicator.parent.isIndeterminate = true
            binding.indicator.parent.visibility = View.VISIBLE
            refreshData()
        } else {
            mAdapter.setLoadState(viewModel.loadState, viewModel.errorMessage)
            mAdapter.notifyItemChanged(viewModel.dataList.size)
        }
    }

    private fun initView() {
        mAdapter = AppAdapter(this, viewModel.dataList)
        mAdapter.setAppListener(this)
        mLayoutManager = LinearLayoutManager(this)
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

    private fun refreshData() {
        viewModel.firstVisibleItemPosition = -1
        viewModel.lastVisibleItemPosition = -1
        viewModel.page = 1
        viewModel.isRefreshing = true
        viewModel.isEnd = false
        viewModel.isNew = true
        viewModel.getFeedList()
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
        if (type == "feed_reply") {
            viewModel.likeReplyPosition = position!!
            viewModel.likeReplyId = id
            if (isLike) {
                viewModel.isPostUnLikeReply = true
                viewModel.postUnLikeReply()
            } else {
                viewModel.isPostLikeReply = true
                viewModel.postLikeReply()
            }
        } else {
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
    }

    override fun onRefreshReply(listType: String) {}

    override fun onDeleteFeedReply(id: String, position: Int, rPosition: Int?) {
        viewModel.isNew = true
        viewModel.position = position
        viewModel.url = "/v6/feed/deleteFeed"
        viewModel.deleteId = id
        viewModel.postDelete()
    }

    override fun onShowCollection(id: String, title: String) {}

    override fun onReload() {
        viewModel.isEnd = true
        loadMore()
    }

}