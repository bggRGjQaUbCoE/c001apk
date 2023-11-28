package com.example.c001apk.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.ThemeUtils
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.adapter.AppAdapter
import com.example.c001apk.databinding.ActivityAppBinding
import com.example.c001apk.ui.fragment.minterface.IOnLikeClickListener
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.DateUtils
import com.example.c001apk.util.ImageUtil
import com.example.c001apk.util.TopicBlackListUtil
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.view.ninegridimageview.NineGridImageView
import com.example.c001apk.view.ninegridimageview.OnImageItemClickListener
import com.example.c001apk.viewmodel.AppViewModel

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

        if (viewModel.title != "") {
            showAppInfo()
            binding.appLayout.visibility = View.VISIBLE
        } else if (viewModel.errorMessage != null) {
            showErrorMessage()
        }
        initView()
        initData()
        initRefresh()
        initScroll()

        viewModel.appInfoData.observe(this) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val appInfo = result.getOrNull()
                if (appInfo?.message != null) {
                    viewModel.errorMessage = appInfo.message
                    binding.indicator.isIndeterminate = false
                    binding.indicator.visibility = View.GONE
                    showErrorMessage()
                    return@observe
                } else if (appInfo?.data != null) {
                    viewModel.commentStatusText = appInfo.data.commentStatusText
                    viewModel.title = appInfo.data.title
                    viewModel.version =
                        "版本: ${appInfo.data.version}(${appInfo.data.apkversioncode})"
                    viewModel.size = "大小: ${appInfo.data.apksize}"
                    viewModel.lastupdate = if (appInfo.data.lastupdate == null) "更新时间: null"
                    else "更新时间: ${DateUtils.fromToday(appInfo.data.lastupdate)}"
                    viewModel.logo = appInfo.data.logo
                    viewModel.appId = appInfo.data.id
                    viewModel.packageName = appInfo.data.apkname
                    viewModel.versionCode = appInfo.data.apkversioncode
                    showAppInfo()

                    if (viewModel.commentStatusText == "允许评论") {
                        viewModel.isRefreshing = true
                        viewModel.isNew = true
                        viewModel.getAppComment()
                    } else {
                        viewModel.isEnd = true
                        binding.indicator.isIndeterminate = false
                        binding.indicator.visibility = View.GONE
                        binding.appLayout.visibility = View.VISIBLE
                        binding.swipeRefresh.isEnabled = false
                        binding.swipeRefresh.isRefreshing = false
                        viewModel.isRefreshing = false
                        viewModel.isLoadMore = false
                        mAdapter.setLoadState(mAdapter.LOADING_ERROR, viewModel.commentStatusText)
                        mAdapter.notifyDataSetChanged()
                    }
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
                                if (!BlackListUtil.checkUid(element.userInfo?.uid.toString()) && !TopicBlackListUtil.checkTopic(
                                        element.tags + element.ttitle
                                    )
                                )
                                    viewModel.appCommentList.add(element)

                    }
                    mAdapter.setLoadState(mAdapter.LOADING_COMPLETE, null)
                } else {
                    mAdapter.setLoadState(mAdapter.LOADING_END, null)
                    viewModel.isEnd = true
                    result.exceptionOrNull()?.printStackTrace()
                }
                mAdapter.notifyDataSetChanged()
                binding.indicator.isIndeterminate = false
                binding.indicator.visibility = View.GONE
                binding.appLayout.visibility = View.VISIBLE
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

    private fun showErrorMessage() {
        binding.swipeRefresh.isEnabled = false
        binding.errorMessage.visibility = View.VISIBLE
        binding.errorMessage.text = viewModel.errorMessage
    }

    private fun showAppInfo() {
        binding.name.text = viewModel.title
        binding.version.text = viewModel.version
        binding.size.text = viewModel.size
        binding.updateTime.text = viewModel.lastupdate
        binding.collapsingToolbar.title = viewModel.title
        binding.collapsingToolbar.setExpandedTitleColor(this.getColor(com.google.android.material.R.color.mtrl_btn_transparent_bg_color))
        ImageUtil.showIMG(binding.logo, viewModel.logo)
        binding.btnDownload.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                viewModel.downloadLinkData.observe(this@AppActivity) { result ->
                    val link = result.getOrNull()
                    if (link != null) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                        startActivity(intent)
                    } else {
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
                viewModel.getDownloadLink()
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
            itemAnimator = null
            if (itemDecorationCount == 0)
                addItemDecoration(LinearItemDecoration(space))
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initData() {
        if (viewModel.isInit) {
            viewModel.isInit = false
            binding.indicator.isIndeterminate = true
            binding.indicator.visibility = View.VISIBLE
            refreshData()
        } else if (viewModel.commentStatusText != "允许评论") {
            binding.swipeRefresh.isEnabled = false
            mAdapter.setLoadState(mAdapter.LOADING_ERROR, viewModel.commentStatusText)
            mAdapter.notifyDataSetChanged()
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
            @SuppressLint("NotifyDataSetChanged")
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (viewModel.lastVisibleItemPosition == viewModel.appCommentList.size
                        && !viewModel.isEnd && !viewModel.isRefreshing && !viewModel.isLoadMore
                    ) {
                        mAdapter.setLoadState(mAdapter.LOADING, null)
                        mAdapter.notifyDataSetChanged()
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.topic_product_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(
            when (viewModel.appCommentTitle) {
                "最近回复" -> R.id.topicLatestReply
                "热度排序" -> R.id.topicHot
                "最新发布" -> R.id.topicLatestPublish
                else -> throw IllegalArgumentException("type error")
            }
        )?.isChecked = true
        return super.onPrepareOptionsMenu(menu)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()

            R.id.search -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra("pageType", "apk")
                intent.putExtra("pageParam", viewModel.appId)
                intent.putExtra("title", viewModel.title)
                startActivity(intent)
            }

            R.id.topicLatestReply -> {
                viewModel.appCommentSort = ""
                viewModel.appCommentTitle = "最近回复"
                viewModel.appCommentList.clear()
                mAdapter.notifyDataSetChanged()
                binding.indicator.visibility = View.VISIBLE
                binding.indicator.isIndeterminate = true
                viewModel.isNew = true
                viewModel.isRefreshing = true
                viewModel.isLoadMore = false
                viewModel.isEnd = false
                viewModel.page = 1
                viewModel.getAppComment()
            }

            R.id.topicHot -> {
                viewModel.appCommentSort = "%26sort%3Dpopular"
                viewModel.appCommentTitle = "热度排序"
                viewModel.appCommentList.clear()
                mAdapter.notifyDataSetChanged()
                binding.indicator.visibility = View.VISIBLE
                binding.indicator.isIndeterminate = true
                viewModel.isNew = true
                viewModel.isRefreshing = true
                viewModel.isLoadMore = false
                viewModel.isEnd = false
                viewModel.page = 1
                viewModel.getAppComment()
            }

            R.id.topicLatestPublish -> {
                viewModel.appCommentSort = "%26sort%3Ddateline_desc"
                viewModel.appCommentTitle = "最新发布"
                viewModel.appCommentList.clear()
                mAdapter.notifyDataSetChanged()
                binding.indicator.visibility = View.VISIBLE
                binding.indicator.isIndeterminate = true
                viewModel.isNew = true
                viewModel.isRefreshing = true
                viewModel.isLoadMore = false
                viewModel.isEnd = false
                viewModel.page = 1
                viewModel.getAppComment()
            }
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
        ImageUtil.startBigImgView(
            nineGridView,
            imageView,
            urlList,
            position
        )
    }

}