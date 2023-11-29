package com.example.c001apk.ui.fragment.feed

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.ThemeUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.adapter.FeedContentAdapter
import com.example.c001apk.databinding.FragmentFeedBinding
import com.example.c001apk.logic.database.FeedFavoriteDatabase
import com.example.c001apk.logic.model.FeedFavorite
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.model.TotalReplyResponse
import com.example.c001apk.ui.activity.UserActivity
import com.example.c001apk.ui.activity.WebViewActivity
import com.example.c001apk.ui.fragment.ReplyBottomSheetDialog
import com.example.c001apk.ui.fragment.minterface.IOnLikeClickListener
import com.example.c001apk.ui.fragment.minterface.IOnListTypeClickListener
import com.example.c001apk.ui.fragment.minterface.IOnPublishClickListener
import com.example.c001apk.ui.fragment.minterface.IOnReplyClickListener
import com.example.c001apk.ui.fragment.minterface.IOnShowMoreReplyContainer
import com.example.c001apk.ui.fragment.minterface.IOnShowMoreReplyListener
import com.example.c001apk.ui.fragment.minterface.IOnTotalReplyClickListener
import com.example.c001apk.ui.fragment.minterface.OnPostFollowListener
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.ClipboardUtil
import com.example.c001apk.util.DateUtils
import com.example.c001apk.util.DensityTool
import com.example.c001apk.util.ImageUtil
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.ToastUtil
import com.example.c001apk.view.OffsetLinearLayoutManager
import com.example.c001apk.view.StickyItemDecorator
import com.example.c001apk.view.ninegridimageview.NineGridImageView
import com.example.c001apk.view.ninegridimageview.OnImageItemClickListener
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLDecoder


class FeedFragment : Fragment(), IOnTotalReplyClickListener, IOnReplyClickListener,
    IOnLikeClickListener, OnImageItemClickListener, IOnListTypeClickListener,
    IOnShowMoreReplyListener, OnPostFollowListener, IOnPublishClickListener {

    private lateinit var binding: FragmentFeedBinding
    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var bottomSheetDialog: ReplyBottomSheetDialog
    private lateinit var mAdapter: FeedContentAdapter
    private lateinit var mLayoutManager: OffsetLinearLayoutManager
    private lateinit var objectAnimator: ObjectAnimator
    private val feedFavoriteDao by lazy {
        FeedFavoriteDatabase.getDatabase(this@FeedFragment.requireContext()).feedFavoriteDao()
    }

    private fun initAnimator() {
        objectAnimator = ObjectAnimator.ofFloat(binding.titleProfile, "translationY", 120f, 0f)
        objectAnimator.interpolator = AccelerateInterpolator()
        objectAnimator.duration = 150
    }

    companion object {
        @JvmStatic
        fun newInstance(
            type: String?,
            id: String,
            uid: String?,
            uname: String?,
            viewReply: Boolean?
        ) =
            FeedFragment().apply {
                arguments = Bundle().apply {
                    putString("TYPE", type)
                    putString("ID", id)
                    putString("UID", uid)
                    putString("UNAME", uname)
                    if (viewReply != null) {
                        putBoolean("viewReply", viewReply)
                    }
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.feedType = it.getString("TYPE", "feed")
            viewModel.id = it.getString("ID", "")
            viewModel.uid = it.getString("UID", "")
            viewModel.funame = it.getString("UNAME", "")
            viewModel.isViewReply = it.getBoolean("viewReply", false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n", "InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAnimator()
        initBar()
        initView()
        initData()
        initButton()
        initRefresh()
        initScroll()

        if (PrefManager.isLogin) {
            val view1 = LayoutInflater.from(context)
                .inflate(R.layout.dialog_reply_bottom_sheet, null, false)
            bottomSheetDialog = ReplyBottomSheetDialog(requireContext(), view1)
            bottomSheetDialog.setIOnPublishClickListener(this)
            bottomSheetDialog.apply {
                setContentView(view1)
                setCancelable(false)
                setCanceledOnTouchOutside(true)
                window?.apply {
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
                type = "reply"
            }
        }

        binding.reply.setOnClickListener {
            if (PrefManager.SZLMID == "") {
                Toast.makeText(activity, "数字联盟ID不能为空", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.rid = viewModel.id
                viewModel.ruid = viewModel.uid
                viewModel.uname = viewModel.funame
                viewModel.type = "feed"
                initReply()
            }
        }

        viewModel.feedData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val feed = result.getOrNull()
                if (feed?.message != null) {
                    viewModel.errorMessage = feed.message
                    binding.indicator.isIndeterminate = false
                    binding.indicator.visibility = View.GONE
                    showErrorMessage()
                    return@observe
                } else if (feed?.data != null) {
                    viewModel.uid = feed.data.uid
                    viewModel.funame = feed.data.userInfo?.username.toString()
                    viewModel.avatar = feed.data.userAvatar
                    viewModel.device = feed.data.deviceTitle
                    viewModel.replyCount = feed.data.replynum
                    viewModel.dateLine = feed.data.dateline
                    viewModel.feedTypeName = feed.data.feedTypeName
                    viewModel.feedType = feed.data.feedType
                    binding.toolBar.title = viewModel.feedTypeName
                    if (viewModel.isRefreshing) {
                        viewModel.feedContentList.clear()
                        viewModel.isNew = true
                        viewModel.getFeedReply()
                    }
                    if (viewModel.isRefreshing || viewModel.isLoadMore) {
                        viewModel.feedContentList.add(feed)
                        if (feed.data.topReplyRows.isNotEmpty()) {
                            mAdapter.setHaveTop(true)
                            viewModel.topReplyId = feed.data.topReplyRows[0].id
                            viewModel.feedTopReplyList.clear()
                            viewModel.feedTopReplyList.addAll(feed.data.topReplyRows)
                        }
                    }
                } else {
                    viewModel.isEnd = true
                    viewModel.isLoadMore = false
                    viewModel.isRefreshing = false
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(activity, "加载失败", Toast.LENGTH_SHORT).show()
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

        viewModel.feedReplyData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                if (viewModel.isRefreshReply) {
                    viewModel.feedReplyList.clear()
                    viewModel.isRefreshReply = false
                }
                val reply = result.getOrNull()
                if (reply?.message != null) {
                    viewModel.errorMessage = reply.message
                    binding.indicator.isIndeterminate = false
                    binding.indicator.visibility = View.GONE
                    binding.contentLayout.visibility = View.VISIBLE
                    viewModel.isEnd = true
                    viewModel.isLoadMore = false
                    viewModel.isRefreshing = false
                    binding.swipeRefresh.isRefreshing = false
                    mAdapter.setLoadState(mAdapter.LOADING_ERROR, viewModel.errorMessage)
                    mAdapter.notifyItemRangeChanged(0, 3)
                    return@observe
                } else if (!reply?.data.isNullOrEmpty()) {
                    if (viewModel.isRefreshing) {
                        viewModel.feedReplyList.clear()
                        if (viewModel.listType == "lastupdate_desc" && viewModel.feedTopReplyList.isNotEmpty())
                            viewModel.feedReplyList.addAll(viewModel.feedTopReplyList)
                    }
                    if (viewModel.isRefreshing || viewModel.isLoadMore) {
                        viewModel.listSize = viewModel.feedReplyList.size
                        for (element in reply?.data!!) {
                            if (element.entityType == "feed_reply") {
                                if (viewModel.topReplyId != null && element.id == viewModel.topReplyId)
                                    continue
                                if (!BlackListUtil.checkUid(element.uid))
                                    viewModel.feedReplyList.add(element)
                            }
                        }
                    }
                    mAdapter.setLoadState(mAdapter.LOADING_COMPLETE, null)
                } else {
                    viewModel.isEnd = true
                    mAdapter.setLoadState(mAdapter.LOADING_END, null)
                    result.exceptionOrNull()?.printStackTrace()
                }
                if (viewModel.isViewReply) {
                    viewModel.isViewReply = false
                    mLayoutManager.scrollToPositionWithOffset(1, 0)
                }
                binding.replyCount.text = "共${viewModel.replyCount}回复"
                if (viewModel.isLoadMore)
                    if (viewModel.isEnd)
                        mAdapter.notifyItemChanged(viewModel.feedReplyList.size + 2)
                    else
                        mAdapter.notifyItemRangeChanged(
                            viewModel.listSize + 2,
                            viewModel.feedReplyList.size - viewModel.listSize + 1
                        )
                else
                    mAdapter.notifyDataSetChanged()
                binding.indicator.isIndeterminate = false
                binding.indicator.visibility = View.GONE
                binding.contentLayout.visibility = View.VISIBLE
                if (PrefManager.isLogin)
                    binding.reply.visibility = View.VISIBLE
                else
                    binding.reply.visibility = View.GONE
                viewModel.isLoadMore = false
                viewModel.isRefreshing = false
                binding.swipeRefresh.isRefreshing = false
            }
        }

        viewModel.likeReplyData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isPostLikeReply) {
                viewModel.isPostLikeReply = false

                val response = result.getOrNull()
                if (response != null) {
                    if (response.data != null) {
                        viewModel.feedReplyList[viewModel.likeReplyPosition - 1].likenum =
                            response.data
                        viewModel.feedReplyList[viewModel.likeReplyPosition - 1].userAction?.like =
                            1
                        mAdapter.notifyItemChanged(viewModel.likeReplyPosition + 1, "like")
                    } else
                        Toast.makeText(activity, response.message, Toast.LENGTH_SHORT).show()
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

        viewModel.unLikeReplyData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isPostUnLikeReply) {
                viewModel.isPostUnLikeReply = false

                val response = result.getOrNull()
                if (response != null) {
                    if (response.data != null) {
                        viewModel.feedReplyList[viewModel.likeReplyPosition - 1].likenum =
                            response.data
                        viewModel.feedReplyList[viewModel.likeReplyPosition - 1].userAction?.like =
                            0
                        mAdapter.notifyItemChanged(viewModel.likeReplyPosition + 1, "like")
                    } else
                        Toast.makeText(activity, response.message, Toast.LENGTH_SHORT).show()
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

        viewModel.likeFeedData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isPostLikeFeed) {
                viewModel.isPostLikeFeed = false

                val response = result.getOrNull()
                if (response != null) {
                    if (response.data != null) {
                        viewModel.feedContentList[0].data?.likenum = response.data.count
                        viewModel.feedContentList[0].data?.userAction?.like = 1
                        mAdapter.notifyItemChanged(0, "like")
                    } else
                        Toast.makeText(activity, response.message, Toast.LENGTH_SHORT).show()
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

        viewModel.unLikeFeedData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isPostUnLikeFeed) {
                viewModel.isPostUnLikeFeed = false

                val response = result.getOrNull()
                if (response != null) {
                    if (response.data != null) {
                        viewModel.feedContentList[0].data?.likenum = response.data.count
                        viewModel.feedContentList[0].data?.userAction?.like = 0
                        mAdapter.notifyItemChanged(0, "like")
                    } else
                        Toast.makeText(activity, response.message, Toast.LENGTH_SHORT).show()
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

        viewModel.postReplyData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isPostReply) {
                viewModel.isPostReply = false

                val response = result.getOrNull()
                response?.let {
                    if (response.data != null) {
                        if (response.data.messageStatus == 1 || response.data.messageStatus == 2) {
                            bottomSheetDialog.editText.text = null
                            if (response.data.messageStatus == 1)
                                Toast.makeText(activity, "回复成功", Toast.LENGTH_SHORT).show()
                            bottomSheetDialog.dismiss()
                            if (viewModel.type == "feed") {
                                viewModel.feedReplyList.add(
                                    0, TotalReplyResponse.Data(
                                        null,
                                        "feed_reply",
                                        viewModel.id,
                                        viewModel.ruid,
                                        PrefManager.uid,
                                        viewModel.id,
                                        URLDecoder.decode(PrefManager.username, "UTF-8"),
                                        viewModel.uname,
                                        viewModel.replyData["message"].toString(),
                                        "",
                                        null,
                                        System.currentTimeMillis() / 1000,
                                        "0",
                                        "0",
                                        PrefManager.userAvatar,
                                        ArrayList(),
                                        0,
                                        TotalReplyResponse.UserAction(0)
                                    )
                                )
                                mAdapter.notifyItemInserted(1)
                                binding.recyclerView.scrollToPosition(1)
                            } else {
                                viewModel.feedReplyList[viewModel.rPosition - 2].replyRows.add(
                                    viewModel.feedReplyList[viewModel.rPosition - 2].replyRows.size,
                                    HomeFeedResponse.ReplyRows(
                                        viewModel.rid,
                                        PrefManager.uid,
                                        viewModel.rid,
                                        URLDecoder.decode(PrefManager.username, "UTF-8"),
                                        viewModel.replyData["message"].toString(),
                                        viewModel.ruid,
                                        viewModel.uname,
                                        null,
                                        ""
                                    )
                                )
                                mAdapter.notifyItemChanged(viewModel.rPosition)
                            }
                        }
                    } else {
                        Toast.makeText(activity, response.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        viewModel.postFollowUnFollowData.observe(viewLifecycleOwner) { result ->
            if (viewModel.postFollowUnFollow) {
                viewModel.postFollowUnFollow = false

                val response = result.getOrNull()
                if (response != null) {
                    if (viewModel.followType) {
                        viewModel.feedContentList[0].data?.userAction?.followAuthor = 0
                    } else {
                        viewModel.feedContentList[0].data?.userAction?.followAuthor = 1
                    }
                    mAdapter.notifyItemChanged(0)
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

    }

    private fun showErrorMessage() {
        binding.errorMessage.visibility = View.VISIBLE
        binding.errorMessage.text = viewModel.errorMessage
    }

    @SuppressLint("SetTextI18n")
    private fun initButton() {
        binding.name1.setOnClickListener {
            val intent = Intent(requireContext(), UserActivity::class.java)
            intent.putExtra("id", viewModel.uid)
            requireActivity().startActivity(intent)
        }
        binding.avatar1.setOnClickListener {
            val intent = Intent(requireContext(), UserActivity::class.java)
            intent.putExtra("id", viewModel.uid)
            requireActivity().startActivity(intent)
        }
        mAdapter.setListType(viewModel.listType)
        binding.replyCount.text = "共${viewModel.replyCount}回复"
        binding.lastUpdate.setOnClickListener {
            refreshReply("lastupdate_desc")
        }
        binding.dateLine.setOnClickListener {
            refreshReply("dateline_desc")
        }
        binding.popular.setOnClickListener {
            refreshReply("popular")
        }
        binding.author.setOnClickListener {
            refreshReply("")
        }
    }

    private fun refreshReply(listType: String) {
        if (listType == "lastupdate_desc" && viewModel.feedTopReplyList.isNotEmpty())
            mAdapter.setHaveTop(true)
        else
            mAdapter.setHaveTop(false)
        binding.recyclerView.stopScroll()
        if (viewModel.firstCompletelyVisibleItemPosition > 1)
            viewModel.isViewReply = true
        viewModel.fromFeedAuthor = if (listType == "") 1
        else 0
        mAdapter.setListType(listType)
        viewModel.listType = listType
        viewModel.page = 1
        viewModel.isEnd = false
        viewModel.isRefreshing = true
        viewModel.isLoadMore = false
        viewModel.isNew = true
        viewModel.isRefreshReply = true
        binding.indicator.visibility = View.VISIBLE
        binding.indicator.isIndeterminate = true
        viewModel.getFeedReply()
    }

    private fun initReply() {
        bottomSheetDialog.apply {
            rid = viewModel.rid
            ruid = viewModel.ruid
            uname = viewModel.uname
            setData()
            show()
        }
    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            @SuppressLint("NotifyDataSetChanged")
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (viewModel.lastVisibleItemPosition == viewModel.feedReplyList.size + 2
                        && !viewModel.isEnd && !viewModel.isRefreshing && !viewModel.isLoadMore
                    ) {
                        mAdapter.setLoadState(mAdapter.LOADING, null)
                        mAdapter.notifyItemChanged(viewModel.feedReplyList.size + 2)
                        viewModel.isLoadMore = true
                        viewModel.page++
                        viewModel.isNew = true
                        viewModel.getFeedReply()
                    }
                }
            }


            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (viewModel.feedContentList.isNotEmpty()) {
                    viewModel.firstCompletelyVisibleItemPosition =
                        mLayoutManager.findFirstCompletelyVisibleItemPosition()
                    viewModel.firstVisibleItemPosition =
                        mLayoutManager.findFirstVisibleItemPosition()
                    viewModel.lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition()

                    if (viewModel.firstCompletelyVisibleItemPosition == 0) {
                        if (binding.titleProfile.visibility != View.GONE)
                            binding.titleProfile.visibility = View.GONE
                        if (objectAnimator.isRunning) {
                            objectAnimator.cancel()
                        }
                    } else if (viewModel.firstVisibleItemPosition >= 1) {
                        if (binding.titleProfile.visibility != View.VISIBLE)
                            showTitleProfile()
                    } else if (getScrollYDistance() >= DensityTool.dp2px(requireContext(), 50f)) {
                        if (binding.titleProfile.visibility != View.VISIBLE)
                            showTitleProfile()
                    } else {
                        if (binding.titleProfile.visibility != View.GONE) {
                            binding.titleProfile.visibility = View.GONE
                            if (objectAnimator.isRunning) {
                                objectAnimator.cancel()
                            }
                        }
                    }
                }


                /*if (dy > 0 && binding.reply.visibility == View.VISIBLE) {
                    binding.reply.hide()
                } else if (dy < 0 && binding.reply.visibility != View.VISIBLE) {
                    binding.reply.show()
                }*/

            }
        })
    }

    private fun showTitleProfile() {
        if (binding.name1.text == "") {
            binding.name1.text = viewModel.funame
            binding.date.text = DateUtils.fromToday(viewModel.dateLine)
            if (viewModel.device != "") {
                binding.device.text = viewModel.device
                val drawable: Drawable =
                    requireContext().getDrawable(R.drawable.ic_device)!!
                drawable.setBounds(
                    0,
                    0,
                    binding.device.textSize.toInt(),
                    binding.device.textSize.toInt()
                )
                binding.device.setCompoundDrawables(drawable, null, null, null)
            }
            ImageUtil.showIMG(binding.avatar1, viewModel.avatar)
        }
        binding.titleProfile.visibility = View.VISIBLE
        objectAnimator.start()
    }

    @SuppressLint("RestrictedApi")
    private fun initRefresh() {
        binding.swipeRefresh.setColorSchemeColors(
            ThemeUtils.getThemeAttrColor(
                requireContext(),
                rikka.preference.simplemenu.R.attr.colorPrimary
            )
        )
        binding.swipeRefresh.setOnRefreshListener {
            binding.indicator.isIndeterminate = false
            binding.indicator.visibility = View.GONE
            refreshData()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initData() {
        if (viewModel.feedContentList.isEmpty()) {
            if (objectAnimator.isRunning) {
                objectAnimator.cancel()
            }
            binding.titleProfile.visibility = View.GONE
            binding.indicator.visibility = View.VISIBLE
            binding.indicator.isIndeterminate = true
            refreshData()
        } else {
            binding.contentLayout.visibility = View.VISIBLE
            if (viewModel.errorMessage != null) {
                mAdapter.setLoadState(mAdapter.LOADING_ERROR, viewModel.errorMessage)
                mAdapter.notifyItemChanged(2)
            }
            if (getScrollYDistance() >= DensityTool.dp2px(requireContext(), 50f)) {
                showTitleProfile()
            } else {
                if (binding.titleProfile.visibility != View.GONE)
                    binding.titleProfile.visibility = View.GONE
                if (objectAnimator.isRunning) {
                    objectAnimator.cancel()
                }
            }
            if (PrefManager.isLogin)
                binding.reply.visibility = View.VISIBLE
            else
                binding.reply.visibility = View.GONE
        }
    }

    private fun refreshData() {
        viewModel.page = 1
        viewModel.isEnd = false
        viewModel.isRefreshing = true
        viewModel.isLoadMore = false
        viewModel.isNew = true
        viewModel.getFeed()
    }

    private fun initView() {
        val space = resources.getDimensionPixelSize(R.dimen.normal_space)
        mAdapter =
            FeedContentAdapter(requireContext(), viewModel.feedContentList, viewModel.feedReplyList)
        mAdapter.setIOnReplyClickListener(this)
        mAdapter.setIOnTotalReplyClickListener(this)
        mAdapter.setIOnLikeReplyListener(this)
        mAdapter.setOnImageItemClickListener(this)
        mAdapter.setIOnListTypeClickListener(this)
        mAdapter.setOnPostFollowListener(this)
        mLayoutManager = OffsetLinearLayoutManager(activity)
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
            if (itemDecorationCount == 0)
                addItemDecoration(
                    StickyItemDecorator(
                        space,
                        object : StickyItemDecorator.SortShowListener {
                            override fun showSort(show: Boolean) {
                                binding.tabLayout.visibility = if (show) View.VISIBLE else View.GONE
                            }
                        })
                )
        }
    }

    private fun initBar() {
        binding.toolBar.apply {
            title = viewModel.feedTypeName
            setNavigationIcon(R.drawable.ic_back)
            setNavigationOnClickListener {
                requireActivity().finish()
            }
            setOnClickListener {
                binding.recyclerView.stopScroll()
                binding.titleProfile.visibility = View.GONE
                mLayoutManager.scrollToPositionWithOffset(0, 0)
            }
            inflateMenu(R.menu.feed_menu)
            val favorite = menu.findItem(R.id.favorite)
            CoroutineScope(Dispatchers.IO).launch {
                if (feedFavoriteDao.isFavorite(viewModel.id)) {
                    withContext(Dispatchers.Main) {
                        favorite.title = "取消收藏"
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        favorite.title = "收藏"
                    }
                }
            }
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.showReply -> {
                        binding.recyclerView.stopScroll()
                        if (viewModel.firstVisibleItemPosition <= 0)
                            mLayoutManager.scrollToPositionWithOffset(1, 0)
                        else {
                            binding.titleProfile.visibility = View.GONE
                            mLayoutManager.scrollToPositionWithOffset(0, 0)
                        }
                    }

                    R.id.block -> {
                        MaterialAlertDialogBuilder(requireContext()).apply {
                            setTitle("确定将 ${viewModel.funame} 加入黑名单？")
                            setNegativeButton(android.R.string.cancel, null)
                            setPositiveButton(android.R.string.ok) { _, _ ->
                                BlackListUtil.saveUid(viewModel.uid)
                                //requireActivity().finish()
                            }
                            show()
                        }
                    }

                    R.id.share -> {
                        IntentUtil.shareText(
                            this@FeedFragment.requireContext(),
                            "https://www.coolapk1s.com/feed/${viewModel.id}"
                        )
                    }

                    R.id.copyLink -> {
                        ClipboardUtil.copyText(
                            this@FeedFragment.requireContext(),
                            "https://www.coolapk1s.com/feed/${viewModel.id}"
                        )
                    }

                    R.id.report -> {
                        val intent = Intent(requireContext(), WebViewActivity::class.java)
                        intent.putExtra(
                            "url",
                            "https://m.coolapk.com/mp/do?c=feed&m=report&type=feed&id=${viewModel.id}"
                        )
                        requireContext().startActivity(intent)
                    }


                    R.id.favorite -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            if (feedFavoriteDao.isFavorite(viewModel.id)) {
                                feedFavoriteDao.delete(viewModel.id)
                                withContext(Dispatchers.Main) {
                                    favorite.title = "收藏"
                                    ToastUtil.toast("已取消收藏")
                                }
                            } else {
                                try {
                                    val fav = FeedFavorite(
                                        viewModel.id,
                                        viewModel.uid,
                                        viewModel.funame,
                                        viewModel.avatar,
                                        viewModel.device,
                                        viewModel.feedContentList[0].data?.message.toString(), // 还未加载完会空指针
                                        viewModel.feedContentList[0].data?.dateline.toString()
                                    )
                                    feedFavoriteDao.insert(fav)
                                    withContext(Dispatchers.Main) {
                                        favorite.title = "取消收藏"
                                        ToastUtil.toast("已收藏")
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    ToastUtil.toast("请稍后再试")
                                }
                            }

                        }
                    }
                }
                return@setOnMenuItemClickListener true
            }
        }
    }

    override fun onShowTotalReply(position: Int, uid: String, id: String) {
        val mBottomSheetDialogFragment =
            Reply2ReplyBottomSheetDialog.newInstance(position, viewModel.uid, uid, id)
        mBottomSheetDialogFragment.show(childFragmentManager, "Dialog")
    }

    override fun onReply2Reply(
        rPosition: Int,
        r2rPosition: Int?,
        id: String,
        uid: String,
        uname: String,
        type: String
    ) {
        if (PrefManager.isLogin && !viewModel.isShowMoreReply) {
            if (PrefManager.SZLMID == "") {
                Toast.makeText(activity, "数字联盟ID不能为空", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.rPosition = rPosition
                viewModel.rid = id
                viewModel.ruid = uid
                viewModel.uname = uname
                viewModel.type = type
                initReply()
            }
        }
        viewModel.isShowMoreReply = false
    }

    override fun onPostLike(type: String?, isLike: Boolean, id: String, position: Int?) {
        if (type == "reply") {
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
            if (isLike) {
                viewModel.isPostUnLikeFeed = true
                viewModel.postUnLikeFeed()
            } else {
                viewModel.isPostLikeFeed = true
                viewModel.postLikeFeed()
            }
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

    override fun onRefreshReply(listType: String) {
        when (listType) {
            "lastupdate_desc" -> binding.buttonToggle.check(R.id.lastUpdate)
            "dateline_desc" -> binding.buttonToggle.check(R.id.dateLine)
            "popular" -> binding.buttonToggle.check(R.id.popular)
            "" -> binding.buttonToggle.check(R.id.author)
        }
        refreshReply(listType)
    }

    override fun onShowMoreReply(position: Int, uid: String, id: String) {
        viewModel.isShowMoreReply = true
        val mBottomSheetDialogFragment =
            Reply2ReplyBottomSheetDialog.newInstance(position, viewModel.uid, uid, id)
        mBottomSheetDialogFragment.show(childFragmentManager, "Dialog")
    }

    override fun onResume() {
        super.onResume()
        IOnShowMoreReplyContainer.controller = this
    }

    private fun getScrollYDistance(): Int {
        val position = mLayoutManager.findFirstVisibleItemPosition()
        val firstVisibleChildView = mLayoutManager.findViewByPosition(position)
        var itemHeight = 0
        var top = 0
        firstVisibleChildView?.let {
            itemHeight = firstVisibleChildView.height
            top = firstVisibleChildView.top
        }
        return position * itemHeight - top
    }

    override fun onPostFollow(isFollow: Boolean, uid: String, position: Int) {
        viewModel.uid = uid
        if (isFollow) {
            viewModel.followType = true
            viewModel.postFollowUnFollow = true
            viewModel.url = "/v6/user/unfollow"
            viewModel.postFollowUnFollow()
        } else {
            viewModel.followType = false
            viewModel.postFollowUnFollow = true
            viewModel.url = "/v6/user/follow"
            viewModel.postFollowUnFollow()
        }
    }

    override fun onPublish(message: String, replyAndForward: String) {
        viewModel.replyData["message"] = message
        viewModel.replyData["replyAndForward"] = replyAndForward
        viewModel.isPostReply = true
        viewModel.postReply()
    }

}