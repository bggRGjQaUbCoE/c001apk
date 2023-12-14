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
import com.example.c001apk.R
import com.example.c001apk.adapter.AppAdapter
import com.example.c001apk.constant.RecyclerView.checkForGaps
import com.example.c001apk.constant.RecyclerView.markItemDecorInsetsDirty
import com.example.c001apk.databinding.ActivityCarouselBinding
import com.example.c001apk.ui.fragment.minterface.AppListener
import com.example.c001apk.ui.fragment.topic.TopicContentFragment
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.TopicBlackListUtil
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.view.StaggerItemDecoration
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.tabs.TabLayoutMediator
import java.lang.reflect.Method

class CarouselActivity : BaseActivity(), AppListener {

    private lateinit var binding: ActivityCarouselBinding
    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var mAdapter: AppAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var sLayoutManager: StaggeredGridLayoutManager
    private lateinit var mCheckForGapMethod: Method
    private lateinit var mMarkItemDecorInsetsDirtyMethod: Method

    override fun onResume() {
        super.onResume()
        if (viewModel.isResume) {
            viewModel.isResume = false
            initData()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarouselBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.url = intent.getStringExtra("url")!!
        viewModel.title = intent.getStringExtra("title")!!

        if (!viewModel.isInit) {
            if (viewModel.tabList.isNotEmpty()) {
                binding.tabLayout.visibility = View.VISIBLE
                binding.viewPager.visibility = View.VISIBLE
                initView()
            } else {
                binding.swipeRefresh.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.VISIBLE
                initRvView()
                initData()
                initRefresh()
                initScroll()
            }
            if (viewModel.barTitle != "")
                initBar(viewModel.barTitle)
            else
                initBar(viewModel.title)
        }

        viewModel.carouselData.observe(this) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val response = result.getOrNull()
                if (!response.isNullOrEmpty()) {

                    if (viewModel.isInit) {
                        viewModel.isInit = false

                        viewModel.barTitle =
                            if (response[response.size - 1].extraDataArr == null)
                                viewModel.title
                            else
                                response[response.size - 1].extraDataArr?.pageTitle.toString()
                        initBar(viewModel.barTitle)

                        var index = 0
                        for (element in response) {
                            if (element.entityTemplate == "iconTabLinkGridCard") {
                                binding.tabLayout.visibility = View.VISIBLE
                                binding.viewPager.visibility = View.VISIBLE
                                break
                            } else index++
                        }

                        if (binding.tabLayout.visibility == View.VISIBLE) {
                            for (element in response[index].entities) {
                                viewModel.tabList.add(element.title)
                                viewModel.fragmentList.add(
                                    TopicContentFragment.newInstance(
                                        element.url,
                                        element.title,
                                        false
                                    )
                                )
                                initView()
                            }
                        } else {
                            initRvView()
                            initData()
                            initRefresh()
                            initScroll()
                            binding.swipeRefresh.visibility = View.VISIBLE
                            binding.recyclerView.visibility = View.VISIBLE
                            for (element in response)
                                if (element.entityType == "feed")
                                    if (!BlackListUtil.checkUid(element.userInfo?.uid.toString()) && !TopicBlackListUtil.checkTopic(
                                            element.tags + element.ttitle
                                        )
                                    )
                                        viewModel.carouselList.add(element)
                            mAdapter.setLoadState(mAdapter.LOADING_COMPLETE, null)
                            mAdapter.notifyDataSetChanged()
                        }
                    } else {
                        if (viewModel.isRefreshing)
                            viewModel.carouselList.clear()
                        if (viewModel.isRefreshing || viewModel.isLoadMore) {
                            viewModel.listSize = viewModel.carouselList.size
                            for (element in response)
                                if (element.entityType == "feed")
                                    if (!BlackListUtil.checkUid(element.userInfo?.uid.toString()) && !TopicBlackListUtil.checkTopic(
                                            element.tags + element.ttitle
                                        )
                                    )
                                        viewModel.carouselList.add(element)
                        }
                        mAdapter.setLoadState(mAdapter.LOADING_COMPLETE, null)
                        if (viewModel.isLoadMore)
                            if (viewModel.isEnd)
                                mAdapter.notifyItemChanged(viewModel.carouselList.size)
                            else
                                mAdapter.notifyItemRangeChanged(
                                    viewModel.listSize,
                                    viewModel.carouselList.size - viewModel.listSize + 1
                                )
                        else
                            mAdapter.notifyDataSetChanged()
                    }
                    binding.indicator.isIndeterminate = false
                    binding.indicator.visibility = View.GONE
                } else {
                    if (::mAdapter.isInitialized) {
                        mAdapter.setLoadState(mAdapter.LOADING_END, null)
                        mAdapter.notifyItemChanged(0)
                    }
                    viewModel.isEnd = true
                    result.exceptionOrNull()?.printStackTrace()
                }
                viewModel.isLoadMore = false
                viewModel.isRefreshing = false
                binding.swipeRefresh.isRefreshing = false
            }
        }

        viewModel.likeFeedData.observe(this) { result ->
            if (viewModel.isPostLikeFeed) {
                viewModel.isPostLikeFeed = false

                val response = result.getOrNull()
                if (response != null) {
                    if (response.data != null) {
                        viewModel.carouselList[viewModel.likePosition].likenum =
                            response.data.count
                        viewModel.carouselList[viewModel.likePosition].userAction?.like = 1
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
                        viewModel.carouselList[viewModel.likePosition].likenum =
                            response.data.count
                        viewModel.carouselList[viewModel.likePosition].userAction?.like = 0
                        mAdapter.notifyItemChanged(viewModel.likePosition, "like")
                    } else
                        Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            @SuppressLint("NotifyDataSetChanged")
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (viewModel.lastVisibleItemPosition == viewModel.carouselList.size
                        && !viewModel.isEnd && !viewModel.isRefreshing && !viewModel.isLoadMore
                    ) {
                        mAdapter.setLoadState(mAdapter.LOADING, null)
                        mAdapter.notifyItemChanged(viewModel.carouselList.size)
                        viewModel.isLoadMore = true
                        viewModel.page++
                        viewModel.isNew = true
                        viewModel.getCarouselList()
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (viewModel.carouselList.isNotEmpty()) {
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
                this,
                rikka.preference.simplemenu.R.attr.colorPrimary
            )
        )
        binding.swipeRefresh.setOnRefreshListener {
            binding.indicator.isIndeterminate = false
            binding.indicator.visibility = View.GONE
            refreshData()
        }
    }

    private fun initRvView() {
        val space = resources.getDimensionPixelSize(R.dimen.normal_space)
        mAdapter = AppAdapter(this, viewModel.carouselList)
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
                addItemDecoration(LinearItemDecoration(space))
            else
                addItemDecoration(StaggerItemDecoration(space))
        }

    }

    private fun initView() {
        binding.viewPager.offscreenPageLimit = viewModel.tabList.size
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int) = viewModel.fragmentList[position]
            override fun getItemCount() = viewModel.tabList.size
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = viewModel.tabList[position]
        }.attach()
    }


    private fun initData() {
        if (viewModel.carouselList.isEmpty()) {
            binding.indicator.visibility = View.VISIBLE
            binding.indicator.isIndeterminate = true
            viewModel.isNew = true
            refreshData()
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
        viewModel.getCarouselList()
    }

    private fun initBar(title: String) {
        binding.toolBar.title = title
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
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