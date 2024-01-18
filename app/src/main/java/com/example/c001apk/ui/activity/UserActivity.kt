package com.example.c001apk.ui.activity

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
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
import com.example.c001apk.adapter.AppAdapter
import com.example.c001apk.databinding.ActivityUserBinding
import com.example.c001apk.ui.fragment.minterface.AppListener
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.ClipboardUtil.copyText
import com.example.c001apk.util.CountUtil
import com.example.c001apk.util.DateUtils
import com.example.c001apk.util.ImageUtil
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.TopicBlackListUtil
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.view.StaggerItemDecoration
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class UserActivity : BaseActivity<ActivityUserBinding>(), AppListener {

    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var mAdapter: AppAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var sLayoutManager: StaggeredGridLayoutManager

    @SuppressLint(
        "ResourceAsColor", "SetTextI18n", "NotifyDataSetChanged", "UseCompatLoadingForDrawables",
        "RestrictedApi", "ResourceType"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        if (!viewModel.uname.isNullOrEmpty()) {
            showUserInfo()
            binding.followBtn.visibility = if (PrefManager.isLogin) View.VISIBLE
            else View.GONE
            binding.infoLayout.visibility = View.VISIBLE
        } else if (viewModel.errorMessage != null) {
            showErrorMessage()
        }
        initView()
        initData()
        initRefresh()
        initScroll()

        binding.avatar.setOnClickListener {
            ImageUtil.startBigImgViewSimple(binding.avatar, viewModel.avatar.toString())
        }

        binding.cover.setOnClickListener {
            ImageUtil.startBigImgViewSimple(binding.cover, viewModel.cover.toString())
        }

        binding.name.setOnClickListener {
            copyText(this, viewModel.uname.toString())
        }

        binding.uid.setOnClickListener {
            copyText(this, viewModel.uid.toString())
        }

        binding.errorLayout.retry.setOnClickListener {
            binding.errorLayout.parent.visibility = View.GONE
            binding.indicator.parent.visibility = View.VISIBLE
            binding.indicator.parent.isIndeterminate = true
            refreshData()
        }

        binding.followBtn.setOnClickListener {
            if (PrefManager.isLogin)
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
                    binding.indicator.parent.isIndeterminate = false
                    binding.indicator.parent.visibility = View.GONE
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
                    viewModel.uid = null
                    binding.indicator.parent.isIndeterminate = false
                    binding.indicator.parent.visibility = View.GONE
                    binding.errorLayout.parent.visibility = View.VISIBLE
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

        viewModel.userFeedData.observe(this) { result ->
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
                        mAdapter.notifyItemChanged(viewModel.feedList.size)
                        return@observe
                    } else if (!feed.data.isNullOrEmpty()) {
                        if (viewModel.isRefreshing) viewModel.feedList.clear()
                        if (viewModel.isRefreshing || viewModel.isLoadMore) {
                            viewModel.listSize = viewModel.feedList.size
                            for (element in feed.data) {
                                if (element.entityType == "feed")
                                    if (!BlackListUtil.checkUid(element.userInfo?.uid.toString())
                                        && !TopicBlackListUtil.checkTopic(
                                            element.tags + element.ttitle
                                        )
                                    )
                                        viewModel.feedList.add(element)
                            }
                        }
                        viewModel.loadState = mAdapter.LOADING_COMPLETE
                        mAdapter.setLoadState(viewModel.loadState, null)
                    } else if (feed.data?.isEmpty() == true) {
                        if (viewModel.isRefreshing) viewModel.feedList.clear()
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
                        mAdapter.notifyItemChanged(viewModel.feedList.size)
                    else
                        mAdapter.notifyItemRangeChanged(
                            viewModel.listSize,
                            viewModel.feedList.size - viewModel.listSize + 1
                        )
                else
                    mAdapter.notifyDataSetChanged()
                binding.infoLayout.visibility = View.VISIBLE
                binding.followBtn.visibility = if (PrefManager.isLogin) View.VISIBLE
                else View.GONE
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

    @SuppressLint("SetTextI18n")
    private fun showUserInfo() {
        binding.collapsingToolbar.title = viewModel.uname
        binding.collapsingToolbar.setCollapsedTitleTextColor(this.getColor(R.color.white))
        binding.collapsingToolbar.setExpandedTitleColor(this.getColor(com.google.android.material.R.color.mtrl_btn_transparent_bg_color))
        ImageUtil.showUserCover(binding.cover, viewModel.cover)
        ImageUtil.showIMG(binding.avatar, viewModel.avatar)
        binding.name.text = viewModel.uname
        binding.uid.text = "uid: ${viewModel.uid}"
        binding.level.text = viewModel.level
        binding.level.visibility = View.VISIBLE
        if (viewModel.bio.isNullOrEmpty()) binding.bio.visibility = View.GONE
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
        binding.follow.setOnClickListener {
            IntentUtil.startActivity<FFFListActivity>(this) {
                putExtra("uid", viewModel.uid)
                putExtra("isEnable", false)
                putExtra("type", "follow")
            }
        }
        binding.fans.setOnClickListener {
            IntentUtil.startActivity<FFFListActivity>(this) {
                putExtra("uid", viewModel.uid)
                putExtra("isEnable", false)
                putExtra("type", "fans")
            }
        }
    }

    private fun showErrorMessage() {
        binding.swipeRefresh.isEnabled = false
        binding.errorMessage.parent.visibility = View.VISIBLE
        binding.errorMessage.parent.text = viewModel.errorMessage
    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                    if (viewModel.feedList.isNotEmpty() && !viewModel.isEnd) {
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

                    if (viewModel.lastVisibleItemPosition == viewModel.feedList.size
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
        mAdapter.notifyItemChanged(viewModel.feedList.size)
        viewModel.isLoadMore = true
        viewModel.isNew = true
        viewModel.getUserFeed()
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
        if (viewModel.isInit) {
            viewModel.isInit = false
            binding.indicator.parent.visibility = View.VISIBLE
            binding.indicator.parent.isIndeterminate = true
            refreshData()
        } else if (viewModel.uid == null) {
            binding.errorLayout.parent.visibility = View.VISIBLE
        } else {
            mAdapter.setLoadState(viewModel.loadState, viewModel.errorMessage)
            mAdapter.notifyItemChanged(viewModel.feedList.size)
        }
    }

    private fun refreshData() {
        viewModel.firstVisibleItemPosition = -1
        viewModel.lastVisibleItemPosition = -1
        viewModel.page = 1
        viewModel.isRefreshing = true
        viewModel.isEnd = false
        if (viewModel.id.isNullOrEmpty())
            viewModel.id = intent.getStringExtra("id")
        viewModel.isNew = true
        viewModel.getUser()
    }

    private fun initView() {
        mAdapter = AppAdapter(this, viewModel.feedList)
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

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.user_menu, menu)

        val itemBlock = menu?.findItem(R.id.block)
        val spannableString = SpannableString(itemBlock?.title)
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
        itemBlock?.title = spannableString


        val itemShare = menu?.findItem(R.id.share)
        val spannableString1 = SpannableString(itemShare?.title)
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
        itemShare?.title = spannableString1

        val itemReport = menu?.findItem(R.id.report)
        val spannableString2 = SpannableString(itemReport?.title)
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
        itemReport?.title = spannableString2

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()

            R.id.search -> {
                IntentUtil.startActivity<SearchActivity>(this) {
                    putExtra("pageType", "user")
                    putExtra("pageParam", viewModel.uid)
                    putExtra("title", binding.name.text)
                }
            }

            R.id.block -> {
                MaterialAlertDialogBuilder(this).apply {
                    setTitle("确定将 ${viewModel.uname} 加入黑名单？")
                    setNegativeButton(android.R.string.cancel, null)
                    setPositiveButton(android.R.string.ok) { _, _ ->
                        BlackListUtil.saveUid(viewModel.uid.toString())
                    }
                    show()
                }
            }

            R.id.share -> {
                IntentUtil.shareText(this, "https://www.coolapk.com/u/${viewModel.uid}")
            }

            R.id.report -> {
                IntentUtil.startActivity<WebViewActivity>(this) {
                    putExtra(
                        "url",
                        "https://m.coolapk.com/mp/do?c=user&m=report&id=${viewModel.uid}"
                    )
                }
            }

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