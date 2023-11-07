package com.example.c001apk.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.ThemeUtils
import androidx.core.view.postDelayed
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.adapter.AppAdapter
import com.example.c001apk.databinding.ActivityAppBinding
import com.example.c001apk.ui.fragment.minterface.IOnLikeClickListener
import com.example.c001apk.util.DateUtils
import com.example.c001apk.util.ImageShowUtil
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.view.ninegridimageview.NineGridImageView
import com.example.c001apk.view.ninegridimageview.OnImageItemClickListener
import com.example.c001apk.view.ninegridimageview.indicator.CircleIndexIndicator
import com.example.c001apk.viewmodel.AppViewModel
import net.mikaelzero.mojito.Mojito
import net.mikaelzero.mojito.impl.DefaultPercentProgress
import net.mikaelzero.mojito.impl.SimpleMojitoViewCallback

class AppActivity : BaseActivity(), IOnLikeClickListener, OnImageItemClickListener {

    private lateinit var binding: ActivityAppBinding
    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var mAdapter: AppAdapter
    private lateinit var mLayoutManager: LinearLayoutManager

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        initView()
        initData()
        initRefresh()
        initScroll()

        viewModel.appInfoData.observe(this) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val appInfo = result.getOrNull()
                if (appInfo != null) {
                    binding.name.text = appInfo.title
                    binding.version.text = "版本: ${appInfo.version}(${appInfo.apkversioncode})"
                    binding.size.text = "大小: ${appInfo.apksize}"
                    if (appInfo.lastupdate == null)
                        binding.updateTime.text = "更新时间: null"
                    else
                        binding.updateTime.text =
                            "更新时间: ${DateUtils.fromToday(appInfo.lastupdate)}"
                    binding.collapsingToolbar.title = appInfo.title
                    binding.collapsingToolbar.setExpandedTitleColor(this.getColor(com.google.android.material.R.color.mtrl_btn_transparent_bg_color))
                    ImageShowUtil.showIMG(binding.logo, appInfo.logo)
                    viewModel.appId = appInfo.id
                    viewModel.isRefreshing = true
                    viewModel.isNew = true
                    viewModel.getAppComment()
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

        viewModel.appCommentData.observe(this) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val comment = result.getOrNull()
                if (!comment.isNullOrEmpty()) {
                    if (viewModel.isRefreshing)
                        viewModel.appCommentList.clear()
                    if (viewModel.isRefreshing || viewModel.isLoadMore) {
                        for (element in comment)
                            if (element.entityType == "feed")
                                viewModel.appCommentList.add(element)

                    }
                    mAdapter.notifyDataSetChanged()
                    mAdapter.setLoadState(mAdapter.LOADING_COMPLETE)
                } else {
                    mAdapter.setLoadState(mAdapter.LOADING_END)
                    viewModel.isEnd = true
                    result.exceptionOrNull()?.printStackTrace()
                }
                binding.indicator.isIndeterminate = false
                binding.indicator.visibility = View.GONE
                binding.appLayout.visibility = View.VISIBLE
                binding.swipeRefresh.isRefreshing = false
                viewModel.isRefreshing = false
            }
        }

        viewModel.likeFeedData.observe(this) { result ->
            if (viewModel.isPostLikeFeed) {
                viewModel.isPostLikeFeed = false

                val response = result.getOrNull()
                if (response != null) {
                    if (response.data != null) {
                        viewModel.appCommentList[viewModel.likePosition].likenum =
                            response.data.count
                        viewModel.appCommentList[viewModel.likePosition].userAction?.like = 1

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
                        viewModel.appCommentList[viewModel.likePosition].likenum =
                            response.data.count
                        viewModel.appCommentList[viewModel.likePosition].userAction?.like = 0

                        mAdapter.notifyDataSetChanged()
                    } else
                        Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }


    }

    private fun initView() {
        val space = resources.getDimensionPixelSize(R.dimen.normal_space)
        mAdapter = AppAdapter(this, viewModel.appCommentList)
        mAdapter.setIOnLikeReplyListener(this)
        mAdapter.setOnImageItemClickListener(this)
        mLayoutManager = LinearLayoutManager(this)
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
            if (itemDecorationCount == 0)
                addItemDecoration(LinearItemDecoration(space))
        }
    }

    private fun initData() {
        if (viewModel.isInit) {
            viewModel.isInit = false
            binding.indicator.isIndeterminate = true
            binding.indicator.visibility = View.VISIBLE
            refreshData()
        }
    }

    private fun refreshData() {
        viewModel.page = 1
        viewModel.isRefreshing = true
        viewModel.isEnd = false
        val id = intent.getStringExtra("id")!!
        viewModel.id = id
        viewModel.isNew = true
        viewModel.getAppInfo()
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
            viewModel.page = 1
            viewModel.isRefreshing = true
            viewModel.isEnd = false
            viewModel.isNew = true
            viewModel.getAppComment()
        }
    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (viewModel.lastVisibleItemPosition == viewModel.appCommentList.size
                        && !viewModel.isEnd
                    ) {
                        mAdapter.setLoadState(mAdapter.LOADING)
                        viewModel.isLoadMore = true
                        viewModel.page++
                        viewModel.isNew = true
                        viewModel.getAppComment()

                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (viewModel.appCommentList.isNotEmpty()) {
                    viewModel.lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition()
                    viewModel.firstCompletelyVisibleItemPosition =
                        mLayoutManager.findFirstCompletelyVisibleItemPosition()
                }
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
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

    override fun onClick(
        nineGridView: NineGridImageView,
        imageView: ImageView,
        urlList: List<String>,
        position: Int
    ) {
        val imgList: MutableList<String> = ArrayList()
        for (img in urlList) {
            if (img.substring(img.length - 6, img.length) == ".s.jpg")
                imgList.add(img.replace(".s.jpg", ""))
            else
                imgList.add(img)
        }
        Mojito.start(imageView.context) {
            urls(imgList)
            position(position)
            progressLoader {
                DefaultPercentProgress()
            }
            setIndicator(CircleIndexIndicator())
            views(nineGridView.getImageViews().toTypedArray())
            setOnMojitoListener(object : SimpleMojitoViewCallback() {
                override fun onStartAnim(position: Int) {
                    nineGridView.getImageViewAt(position)?.apply {
                        postDelayed(200) {
                            this.visibility = View.GONE
                        }
                    }
                }

                override fun onMojitoViewFinish(pagePosition: Int) {
                    nineGridView.getImageViews().forEach {
                        it.visibility = View.VISIBLE
                    }
                }

                override fun onViewPageSelected(position: Int) {
                    nineGridView.getImageViews().forEachIndexed { index, imageView ->
                        if (position == index) {
                            imageView.visibility = View.GONE
                        } else {
                            imageView.visibility = View.VISIBLE
                        }
                    }
                }
            })
        }
    }

}