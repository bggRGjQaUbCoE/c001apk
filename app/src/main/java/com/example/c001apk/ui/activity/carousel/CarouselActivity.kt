package com.example.c001apk.ui.activity.carousel

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.ThemeUtils
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityCarouselBinding
import com.example.c001apk.ui.fragment.home.feed.HomeFeedAdapter
import com.example.c001apk.ui.fragment.home.feed.IOnLikeClickListener
import com.example.c001apk.ui.fragment.topic.content.TopicContentFragment
import com.example.c001apk.util.LinearItemDecoration
import com.google.android.material.tabs.TabLayoutMediator

class CarouselActivity : AppCompatActivity(), IOnLikeClickListener {

    private lateinit var binding: ActivityCarouselBinding
    private val viewModel by lazy { ViewModelProvider(this)[CarouselViewModel::class.java] }
    private lateinit var mAdapter: HomeFeedAdapter
    private lateinit var mLayoutManager: LinearLayoutManager

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
        }

        viewModel.carouselData.observe(this) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val response = result.getOrNull()
                if (!response.isNullOrEmpty()) {

                    if (viewModel.isInit) {
                        viewModel.isInit = false

                        viewModel.barTitle = response[response.size - 1].extraDataArr.pageTitle
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
                            for (element in response)
                                if (element.entityType == "feed")
                                    viewModel.carouselList.add(element)
                            mAdapter.notifyDataSetChanged()
                            mAdapter.setLoadState(mAdapter.LOADING_COMPLETE)
                        }
                    }else{
                        if (viewModel.isRefreshing)
                            viewModel.carouselList.clear()
                        if (viewModel.isRefreshing || viewModel.isLoadMore)
                            for (element in response)
                                if (element.entityType == "feed")
                                    viewModel.carouselList.add(element)
                        mAdapter.notifyDataSetChanged()
                        mAdapter.setLoadState(mAdapter.LOADING_COMPLETE)
                    }
                } else {
                    mAdapter.setLoadState(mAdapter.LOADING_END)
                    viewModel.isEnd = true
                    result.exceptionOrNull()?.printStackTrace()
                }
                binding.indicator.isIndeterminate = false
                binding.indicator.visibility = View.GONE
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

                        mAdapter.notifyDataSetChanged()
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

                        mAdapter.notifyDataSetChanged()
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
                    if (viewModel.lastVisibleItemPosition == viewModel.carouselList.size
                        && !viewModel.isEnd
                    ) {
                        mAdapter.setLoadState(mAdapter.LOADING)
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
                    viewModel.lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition()
                    viewModel.firstCompletelyVisibleItemPosition =
                        mLayoutManager.findFirstCompletelyVisibleItemPosition()
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
        mAdapter = HomeFeedAdapter(this, viewModel.carouselList)
        mAdapter.setIOnLikeReplyListener(this)
        mLayoutManager = LinearLayoutManager(this)
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
            if (itemDecorationCount == 0)
                addItemDecoration(LinearItemDecoration(space))
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

    override fun onPostLike(isLike: Boolean, id: String, position: Int) {
        viewModel.likeFeedId = id
        viewModel.likePosition = position
        if (isLike) {
            viewModel.isPostUnLikeFeed = true
            viewModel.postUnLikeFeed()
        } else {
            viewModel.isPostLikeFeed = true
            viewModel.postLikeFeed()
        }
    }


}