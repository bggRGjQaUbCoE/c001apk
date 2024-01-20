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
import com.example.c001apk.databinding.ActivityCarouselBinding
import com.example.c001apk.logic.model.TopicBean
import com.example.c001apk.ui.fragment.minterface.AppListener
import com.example.c001apk.ui.fragment.topic.TopicContentFragment
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.TopicBlackListUtil
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.view.StaggerItemDecoration
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.tabs.TabLayoutMediator

class CarouselActivity : BaseActivity<ActivityCarouselBinding>(), AppListener {

    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var mAdapter: AppAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var sLayoutManager: StaggeredGridLayoutManager

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

        viewModel.url = intent.getStringExtra("url")
        viewModel.title = intent.getStringExtra("title")

        binding.errorLayout.retry.setOnClickListener {
            binding.errorLayout.parent.visibility = View.GONE
            binding.indicator.parent.visibility = View.VISIBLE
            binding.indicator.parent.isIndeterminate = true
            refreshData()
        }

        if (!viewModel.isResume && viewModel.isInit) {
            binding.errorLayout.parent.visibility = View.VISIBLE
        }

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
            if (!viewModel.barTitle.isNullOrEmpty())
                initBar(viewModel.barTitle.toString())
            else
                initBar(viewModel.title.toString())
        }

        viewModel.carouselData.observe(this) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val response = result.getOrNull()
                if (!response?.data.isNullOrEmpty()) {

                    if (viewModel.isInit) {
                        viewModel.isInit = false

                        viewModel.barTitle =
                            if (response?.data!![response.data.size - 1].extraDataArr == null)
                                viewModel.title
                            else
                                response.data[response.data.size - 1].extraDataArr?.pageTitle.toString()
                        initBar(viewModel.barTitle.toString())

                        var index = 0
                        for (element in response.data) {
                            if (element.entityTemplate == "iconTabLinkGridCard") {
                                binding.tabLayout.visibility = View.VISIBLE
                                binding.viewPager.visibility = View.VISIBLE
                                break
                            } else index++
                        }

                        if (binding.tabLayout.visibility == View.VISIBLE) {
                            for (element in response.data[index].entities) {
                                viewModel.tabList.add(element.title)
                                viewModel.topicList.add(
                                    TopicBean(
                                        element.url,
                                        element.title
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
                            for (element in response.data)
                                if (element.entityType == "feed")
                                    if (!BlackListUtil.checkUid(element.userInfo?.uid.toString())
                                        && !TopicBlackListUtil.checkTopic(
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
                            for (element in response?.data!!)
                                if (element.entityType == "feed")
                                    if (!BlackListUtil.checkUid(element.userInfo?.uid.toString())
                                        && !TopicBlackListUtil.checkTopic(
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
                } else if (response?.data?.isEmpty() == true) {
                    if (viewModel.isRefreshing)
                        viewModel.carouselList.clear()
                    if (::mAdapter.isInitialized) {
                        viewModel.loadState = mAdapter.LOADING_END
                        mAdapter.setLoadState(viewModel.loadState, null)
                        mAdapter.notifyItemChanged(viewModel.carouselList.size)
                    }
                    viewModel.isEnd = true
                } else {
                    if (::mAdapter.isInitialized) {
                        viewModel.loadState = mAdapter.LOADING_ERROR
                        viewModel.errorMessage = getString(R.string.loading_failed)
                        mAdapter.setLoadState(viewModel.loadState, viewModel.errorMessage)
                        mAdapter.notifyItemChanged(viewModel.carouselList.size)
                    } else if (viewModel.carouselList.isEmpty())
                        binding.errorLayout.parent.visibility = View.VISIBLE
                    viewModel.isEnd = true
                    result.exceptionOrNull()?.printStackTrace()
                }
                binding.indicator.parent.isIndeterminate = false
                binding.indicator.parent.visibility = View.GONE
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
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                    if (viewModel.carouselList.isNotEmpty() && !viewModel.isEnd) {
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

                    if (viewModel.lastVisibleItemPosition == viewModel.carouselList.size
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
        mAdapter.notifyItemChanged(viewModel.carouselList.size)
        viewModel.isLoadMore = true
        viewModel.isNew = true
        viewModel.getCarouselList()
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
            binding.indicator.parent.isIndeterminate = false
            binding.indicator.parent.visibility = View.GONE
            refreshData()
        }
    }

    private fun initRvView() {
        mAdapter = AppAdapter(this, viewModel.carouselList)
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

    private fun initView() {
        binding.viewPager.offscreenPageLimit = viewModel.tabList.size
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int) =
                TopicContentFragment.newInstance(
                    viewModel.topicList[position].url,
                    viewModel.topicList[position].title,
                    false
                )

            override fun getItemCount() = viewModel.tabList.size
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = viewModel.tabList[position]
        }.attach()
    }

    private fun initData() {
        if (viewModel.carouselList.isEmpty()) {
            binding.indicator.parent.visibility = View.VISIBLE
            binding.indicator.parent.isIndeterminate = true
            refreshData()
        } else {
            mAdapter.setLoadState(viewModel.loadState, viewModel.errorMessage)
            mAdapter.notifyItemChanged(viewModel.carouselList.size)
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

    override fun onDeleteFeedReply(id: String, position: Int, rPosition: Int?) {}

    override fun onShowCollection(id: String, title: String) {}

    override fun onReload() {
        viewModel.isEnd = false
        loadMore()
    }

}