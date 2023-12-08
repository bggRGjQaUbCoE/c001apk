package com.example.c001apk.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
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
import com.example.c001apk.databinding.ActivityUserBinding
import com.example.c001apk.ui.fragment.minterface.IOnLikeClickListener
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.ClipboardUtil.copyText
import com.example.c001apk.util.CountUtil
import com.example.c001apk.util.DateUtils
import com.example.c001apk.util.ImageUtil
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.TopicBlackListUtil
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.view.ninegridimageview.NineGridImageView
import com.example.c001apk.view.ninegridimageview.OnImageItemClickListener
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class UserActivity : BaseActivity(), IOnLikeClickListener, OnImageItemClickListener {

    private lateinit var binding: ActivityUserBinding
    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var mAdapter: AppAdapter
    private lateinit var mLayoutManager: LinearLayoutManager

    @SuppressLint(
        "ResourceAsColor", "SetTextI18n", "NotifyDataSetChanged", "UseCompatLoadingForDrawables",
        "RestrictedApi", "ResourceType"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        if (viewModel.uname != "") {
            showUserInfo()
            binding.followBtn.visibility = View.VISIBLE
            binding.infoLayout.visibility = View.VISIBLE
        } else if (viewModel.errorMessage != null) {
            showErrorMessage()
        }
        initView()
        initData()
        initRefresh()
        initScroll()

        binding.avatar.setOnClickListener {
            ImageUtil.startBigImgViewSimple(binding.avatar, viewModel.avatar)
        }

        binding.cover.setOnClickListener {
            ImageUtil.startBigImgViewSimple(binding.cover, viewModel.cover)
        }

        binding.uid.setOnClickListener {
            copyText(this, viewModel.uid)
        }

        binding.followBtn.setOnClickListener {
            if (viewModel.followType) {
                viewModel.postFollowUnFollow = true
                viewModel.url = "/v6/user/unfollow"
                viewModel.postFollowUnFollow()
            } else {
                viewModel.postFollowUnFollow = true
                viewModel.url = "/v6/user/follow"
                viewModel.postFollowUnFollow()
            }
        }

        viewModel.userData.observe(this) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val user = result.getOrNull()
                if (user?.message != null) {
                    viewModel.errorMessage = user.message
                    binding.indicator.isIndeterminate = false
                    binding.indicator.visibility = View.GONE
                    showErrorMessage()
                    return@observe
                } else if (user?.data != null) {
                    viewModel.uid = user.data.uid
                    viewModel.followType = user.data.isFollow == 1
                    viewModel.avatar = user.data.userAvatar
                    viewModel.cover = user.data.cover
                    viewModel.uname = user.data.username
                    viewModel.avatar = user.data.userAvatar
                    viewModel.cover = user.data.cover
                    viewModel.level = "Lv.${user.data.level}"
                    viewModel.bio = user.data.bio
                    viewModel.like = "${CountUtil.view(user.data.beLikeNum)}获赞"
                    viewModel.follow = "${CountUtil.view(user.data.follow)}关注"
                    viewModel.fans = "${CountUtil.view(user.data.fans)}粉丝"
                    viewModel.loginTime = DateUtils.fromToday(user.data.logintime) + "活跃"
                    showUserInfo()

                    viewModel.uid = user.data.uid
                    viewModel.isRefreshing = true
                    viewModel.isNew = true
                    viewModel.getUserFeed()
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

        viewModel.userFeedData.observe(this) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val feed = result.getOrNull()
                if (!feed.isNullOrEmpty()) {
                    if (viewModel.isRefreshing) viewModel.feedList.clear()
                    if (viewModel.isRefreshing || viewModel.isLoadMore) {
                        viewModel.listSize = viewModel.feedList.size
                        for (element in feed) {
                            if (element.entityTemplate == "feed")
                                if (!BlackListUtil.checkUid(element.userInfo?.uid.toString()) && !TopicBlackListUtil.checkTopic(
                                        element.tags + element.ttitle
                                    )
                                )
                                    viewModel.feedList.add(element)
                        }
                    }
                    mAdapter.setLoadState(mAdapter.LOADING_COMPLETE, null)
                } else {
                    mAdapter.setLoadState(mAdapter.LOADING_END, null)
                    viewModel.isEnd = true
                    result.exceptionOrNull()?.printStackTrace()
                }
                if (viewModel.isLoadMore)
                    if (viewModel.isEnd)
                        mAdapter.notifyItemChanged(viewModel.feedList.size)
                    else
                        mAdapter.notifyItemRangeChanged(
                            viewModel.listSize,
                            viewModel.feedList.size - viewModel.listSize + 1
                        )
                else
                    mAdapter.notifyDataSetChanged()
                binding.infoLayout.visibility = View.VISIBLE
                binding.followBtn.visibility = View.VISIBLE
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
                        viewModel.feedList[viewModel.likePosition].likenum =
                            response.data.count
                        viewModel.feedList[viewModel.likePosition].userAction?.like = 1
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
                        viewModel.feedList[viewModel.likePosition].likenum =
                            response.data.count
                        viewModel.feedList[viewModel.likePosition].userAction?.like = 0
                        mAdapter.notifyItemChanged(viewModel.likePosition, "like")
                    } else
                        Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

        viewModel.postFollowUnFollowData.observe(this) { result ->
            if (viewModel.postFollowUnFollow) {
                viewModel.postFollowUnFollow = false

                val response = result.getOrNull()
                if (response != null) {
                    if (viewModel.followType) {
                        binding.followBtn.text = "关注"
                    } else {
                        binding.followBtn.text = "已关注"
                    }
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }


    }

    private fun showUserInfo() {
        binding.collapsingToolbar.title = viewModel.uname
        binding.collapsingToolbar.setCollapsedTitleTextColor(this.getColor(R.color.white))
        binding.collapsingToolbar.setExpandedTitleColor(this.getColor(com.google.android.material.R.color.mtrl_btn_transparent_bg_color))
        ImageUtil.showUserCover(binding.cover, viewModel.cover)
        ImageUtil.showAvatar(binding.avatar, viewModel.avatar)
        binding.name.text = viewModel.uname
        binding.uid.text = viewModel.uid
        binding.level.text = viewModel.level
        binding.level.visibility = View.VISIBLE
        if (viewModel.bio == "") binding.bio.visibility = View.GONE
        else binding.bio.text = viewModel.bio
        binding.like.text = viewModel.like
        binding.follow.text = viewModel.follow
        binding.fans.text = viewModel.fans
        binding.loginTime.text = viewModel.loginTime
        if (!viewModel.followType) {
            binding.followBtn.text = "关注"
        } else {
            binding.followBtn.text = "已关注"
        }
        val intent = Intent(this, FFFListActivity::class.java)
        intent.putExtra("uid", viewModel.uid)
        binding.follow.setOnClickListener {
            intent.putExtra("type", "follow")
            startActivity(intent)
        }
        binding.fans.setOnClickListener {
            intent.putExtra("type", "fans")
            startActivity(intent)
        }
    }

    private fun showErrorMessage() {
        binding.swipeRefresh.isEnabled = false
        binding.errorMessage.visibility = View.VISIBLE
        binding.errorMessage.text = viewModel.errorMessage
    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            @SuppressLint("NotifyDataSetChanged")
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (viewModel.lastVisibleItemPosition == viewModel.feedList.size
                        && !viewModel.isEnd && !viewModel.isRefreshing && !viewModel.isLoadMore
                    ) {
                        mAdapter.setLoadState(mAdapter.LOADING, null)
                        mAdapter.notifyItemChanged(viewModel.feedList.size)
                        viewModel.isLoadMore = true
                        viewModel.page++
                        viewModel.isNew = true
                        viewModel.getUserFeed()
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (viewModel.feedList.isNotEmpty()) {
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
        if (viewModel.isInit) {
            viewModel.isInit = false
            binding.indicator.visibility = View.VISIBLE
            binding.indicator.isIndeterminate = true
            refreshData()
        }
    }

    private fun refreshData() {
        viewModel.page = 1
        viewModel.isRefreshing = true
        viewModel.isEnd = false
        if (viewModel.id == "")
            viewModel.id = intent.getStringExtra("id")!!
        viewModel.isNew = true
        viewModel.getUser()
    }

    private fun initView() {
        val space = resources.getDimensionPixelSize(R.dimen.normal_space)
        mAdapter = AppAdapter(this, viewModel.feedList)
        mAdapter.setIOnLikeReplyListener(this)
        mAdapter.setOnImageItemClickListener(this)
        mLayoutManager = LinearLayoutManager(this)
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
            if (itemDecorationCount == 0) addItemDecoration(LinearItemDecoration(space))
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.user_menu, menu)

        val itemBlock = menu!!.findItem(R.id.block)
        val spannableString = SpannableString(itemBlock.title)
        spannableString.setSpan(
            ForegroundColorSpan(
                ThemeUtils.getThemeAttrColor(
                    this,
                    rikka.preference.simplemenu.R.attr.colorControlNormal
                )
            ),
            0,
            spannableString.length,
            0
        )
        itemBlock.title = spannableString


        val itemShare = menu.findItem(R.id.share)
        val spannableString1 = SpannableString(itemShare.title)
        spannableString1.setSpan(
            ForegroundColorSpan(
                ThemeUtils.getThemeAttrColor(
                    this,
                    rikka.preference.simplemenu.R.attr.colorControlNormal
                )
            ),
            0,
            spannableString1.length,
            0
        )
        itemShare.title = spannableString1

        val itemReport = menu.findItem(R.id.report)
        val spannableString2 = SpannableString(itemReport.title)
        spannableString2.setSpan(
            ForegroundColorSpan(
                ThemeUtils.getThemeAttrColor(
                    this,
                    rikka.preference.simplemenu.R.attr.colorControlNormal
                )
            ),
            0,
            spannableString2.length,
            0
        )
        itemReport.title = spannableString2

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()

            R.id.search -> {
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra("pageType", "user")
                intent.putExtra("pageParam", viewModel.uid)
                intent.putExtra("title", binding.name.text)
                startActivity(intent)
            }

            R.id.block -> {
                MaterialAlertDialogBuilder(this).apply {
                    setTitle("确定将 ${viewModel.uname} 加入黑名单？")
                    setNegativeButton(android.R.string.cancel, null)
                    setPositiveButton(android.R.string.ok) { _, _ ->
                        BlackListUtil.saveUid(viewModel.uid)
                    }
                    show()
                }
            }

            R.id.share -> {
                IntentUtil.shareText(this, "https://www.coolapk.com/u/${viewModel.uid}")
            }

            R.id.report -> {
                val intent = Intent(this, WebViewActivity::class.java)
                intent.putExtra(
                    "url",
                    "https://m.coolapk.com/mp/do?c=user&m=report&id=${viewModel.uid}"
                )
                this.startActivity(intent)
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