package com.example.c001apk.ui.activity.fff

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.ThemeUtils
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityFfflistBinding
import com.example.c001apk.ui.activity.BaseActivity
import com.example.c001apk.util.LinearItemDecoration
import com.example.c001apk.util.PrefManager

class FFFListActivity : BaseActivity(), IOnLikeClickListener {

    private lateinit var binding: ActivityFfflistBinding
    private val viewModel by lazy { ViewModelProvider(this)[FFFListViewModel::class.java] }
    private lateinit var mAdapter: FFFListAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private var firstCompletelyVisibleItemPosition = -1
    private var lastVisibleItemPosition = -1
    private lateinit var type: String
    private lateinit var uid: String
    private var likePosition = -1

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFfflistBinding.inflate(layoutInflater)
        setContentView(binding.root)


        type = intent.getStringExtra("type")!!
        uid = intent.getStringExtra("uid")!!

        initBar()
        initView()
        initData()
        initRefresh()
        initScroll()

        viewModel.listData.observe(this) { result ->
            val feed = result.getOrNull()
            if (!feed.isNullOrEmpty()) {
                if (viewModel.isRefreh) viewModel.dataList.clear()
                if (viewModel.isRefreh || viewModel.isLoadMore) {
                    for (element in feed)
                        if (element.entityTemplate == "feed" || element.entityType == "contacts")
                            viewModel.dataList.add(element)
                }
                mAdapter.notifyDataSetChanged()
                mAdapter.setLoadState(mAdapter.LOADING_COMPLETE)
            } else {
                mAdapter.setLoadState(mAdapter.LOADING_END)
                viewModel.isEnd = true
                result.exceptionOrNull()?.printStackTrace()
            }
            binding.indicator.isIndeterminate = false
            binding.swipeRefresh.isRefreshing = false
            viewModel.isRefreh = false
        }

        viewModel.likeFeedData.observe(this) { result ->
            val response = result.getOrNull()
            if (response != null) {
                if (response.data != null) {
                    viewModel.dataList[likePosition].likenum = response.data.count
                    viewModel.dataList[likePosition].userAction.like = 1
                    mAdapter.notifyDataSetChanged()
                } else
                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
            } else {
                result.exceptionOrNull()?.printStackTrace()
            }
        }

        viewModel.unLikeFeedData.observe(this) { result ->
            val response = result.getOrNull()
            if (response != null) {
                if (response.data != null) {
                    viewModel.dataList[likePosition].likenum = response.data.count
                    viewModel.dataList[likePosition].userAction.like = 0
                    mAdapter.notifyDataSetChanged()
                } else
                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
            } else {
                result.exceptionOrNull()?.printStackTrace()
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
        when (type) {
            "feed" -> binding.toolBar.title = "我的动态"

            "follow" -> {
                if (uid == PrefManager.uid)
                    binding.toolBar.title = "好友"
                else
                    binding.toolBar.title = "TA关注的人"
            }

            "fans" -> {
                if (uid == PrefManager.uid)
                    binding.toolBar.title = "关注我的人"
                else
                    binding.toolBar.title = "TA的粉丝"
            }
        }
    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (lastVisibleItemPosition == viewModel.dataList.size) {
                        if (!viewModel.isEnd) {
                            mAdapter.setLoadState(mAdapter.LOADING)
                            viewModel.isLoadMore = true
                            viewModel.page++
                            viewModel.getFeedList()
                        }
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (viewModel.dataList.isNotEmpty()) {
                    lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition()
                    firstCompletelyVisibleItemPosition =
                        mLayoutManager.findFirstCompletelyVisibleItemPosition()
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
            viewModel.page = 1
            viewModel.isRefreh = true
            viewModel.isEnd = false
            viewModel.getFeedList()
        }
    }

    private fun initData() {
        if (viewModel.dataList.isEmpty())
            refreshData()
    }

    private fun initView() {
        val space = resources.getDimensionPixelSize(R.dimen.normal_space)
        mAdapter = FFFListAdapter(this, type, viewModel.dataList)
        mAdapter.setIOnLikeReplyListener(this)
        mLayoutManager = LinearLayoutManager(this)
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
            if (itemDecorationCount == 0) addItemDecoration(LinearItemDecoration(space))
        }
    }

    private fun refreshData() {
        viewModel.page = 1
        viewModel.isRefreh = true
        viewModel.isEnd = false
        viewModel.type = type
        viewModel.uid = uid
        viewModel.getFeedList()
    }

    override fun onPostLike(isLike: Boolean, id: String, position: Int) {
        viewModel.likeFeedId = id
        this.likePosition = position
        if (isLike)
            viewModel.postUnLikeFeed()
        else
            viewModel.postLikeFeed()
    }


}