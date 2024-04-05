package com.example.c001apk.ui.feed

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.R
import com.example.c001apk.adapter.FooterAdapter
import com.example.c001apk.adapter.FooterState
import com.example.c001apk.adapter.HeaderAdapter
import com.example.c001apk.adapter.ItemListener
import com.example.c001apk.constant.Constants.SZLM_ID
import com.example.c001apk.databinding.FragmentFeedBinding
import com.example.c001apk.databinding.ItemCaptchaBinding
import com.example.c001apk.logic.model.FeedEntity
import com.example.c001apk.ui.base.BaseFragment
import com.example.c001apk.ui.feed.reply.IOnPublishClickListener
import com.example.c001apk.ui.feed.reply.Reply2ReplyBottomSheetDialog
import com.example.c001apk.ui.feed.reply.ReplyBottomSheetDialog
import com.example.c001apk.ui.feed.reply.ReplyRefreshListener
import com.example.c001apk.ui.others.CopyActivity
import com.example.c001apk.ui.others.WebViewActivity
import com.example.c001apk.util.ClipboardUtil
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.ToastUtil
import com.example.c001apk.view.StaggerItemDecoration
import com.example.c001apk.view.StickyItemDecorator
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs

@AndroidEntryPoint
class FeedFragment : BaseFragment<FragmentFeedBinding>(), IOnPublishClickListener {

    private val viewModel by viewModels<FeedViewModel>(ownerProducer = { requireActivity() })
    private val isPortrait by lazy { resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT }
    private lateinit var feedDataAdapter: FeedDataAdapter
    private lateinit var feedReplyAdapter: FeedReplyAdapter
    private lateinit var feedFixAdapter: FeedFixAdapter
    private lateinit var footerAdapter: FooterAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var sLayoutManager: StaggeredGridLayoutManager
    private val fabViewBehavior by lazy { HideBottomViewOnScrollBehavior<FloatingActionButton>() }
    private var bottomSheetDialog: ReplyBottomSheetDialog? = null
    private var dialog: AlertDialog? = null
    private var isShowReply: Boolean = false
    private var firstVisibleItemPosition = 0
    private val alpha by lazy {
        ObjectAnimator.ofFloat(binding.titleProfile, "alpha", 0f, 1f).also {
            it.setDuration(500)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initToolBar()
        initData()
        initRefresh()
        initScroll()
        initReplyBtn()
        initBottomSheet()
        initObserve()

    }

    private fun initRefresh() {
        binding.swipeRefresh.apply {
            setColorSchemeColors(
                MaterialColors.getColor(
                    requireContext(),
                    com.google.android.material.R.attr.colorPrimary,
                    0
                )
            )
            setOnRefreshListener {
                refreshData()
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun initBottomSheet() {
        if (PrefManager.isLogin) {
            val view1 = LayoutInflater.from(context)
                .inflate(R.layout.dialog_reply_bottom_sheet, null, false)
            bottomSheetDialog = ReplyBottomSheetDialog(requireContext(), view1)
            bottomSheetDialog?.setIOnPublishClickListener(this)
            bottomSheetDialog?.apply {
                setContentView(view1)
                setCancelable(false)
                setCanceledOnTouchOutside(true)
                window?.apply {
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
                type = "reply"
            }
        }
    }

    private fun initReplyBtn() {
        if (PrefManager.isLogin) {
            binding.reply.apply {
                isVisible = true
                layoutParams = CoordinatorLayout.LayoutParams(
                    CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                    CoordinatorLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.BOTTOM or Gravity.END
                    behavior = fabViewBehavior
                }
                setOnClickListener {
                    if (PrefManager.SZLMID == "") {
                        Toast.makeText(requireContext(), SZLM_ID, Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        viewModel.rid = viewModel.id
                        viewModel.ruid = viewModel.uid
                        viewModel.uname = viewModel.funame
                        viewModel.type = "feed"
                        initReply()
                    }
                }
            }
            ViewCompat.setOnApplyWindowInsetsListener(binding.reply) { _, insets ->
                val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
                binding.reply.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                    rightMargin = 25.dp
                    bottomMargin = if (isPortrait) navigationBars.bottom + 25.dp
                    else 25.dp
                }
                insets
            }
        } else
            binding.reply.isVisible = false
    }

    private fun initReply() {
        bottomSheetDialog?.apply {
            rid = viewModel.rid.toString()
            ruid = viewModel.ruid.toString()
            uname = viewModel.uname.toString()
            setData()
            show()
        }
    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    viewModel.lastVisibleItemPosition =
                        if (isPortrait) mLayoutManager.findLastVisibleItemPosition()
                        else sLayoutManager.findLastVisibleItemPositions(null).max()

                    if (viewModel.lastVisibleItemPosition == viewModel.listSize + viewModel.itemCount + 1
                        && !viewModel.isEnd && !viewModel.isRefreshing && !viewModel.isLoadMore
                    ) {
                        loadMore()
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                firstVisibleItemPosition =
                    if (isPortrait) mLayoutManager.findFirstVisibleItemPosition()
                    else sLayoutManager.findFirstVisibleItemPositions(null).min()
                val shouldShow =
                    if (firstVisibleItemPosition <= 1) scrollYDistance >= 40.dp
                    else true
                binding.toolBar.title = if (shouldShow) null else viewModel.feedTypeName
                if (shouldShow && !binding.titleProfile.isVisible)
                    alpha.start()
                binding.titleProfile.isVisible = shouldShow
            }
        })
    }

    private fun loadMore() {
        viewModel.isLoadMore = true
        viewModel.fetchFeedReply()
    }

    private fun initObserve() {
        viewModel.feedUserState.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                if (it)
                    feedDataAdapter.notifyItemChanged(0, true)
            }
        }

        viewModel.notify.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                if (it) {
                    viewModel.position?.let { position ->
                        feedReplyAdapter.notifyItemChanged(position)
                    }
                }
            }
        }

        viewModel.scroll.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                if (it) {
                    scrollToPosition(viewModel.itemCount)
                }
            }
        }

        viewModel.closeSheet.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                if (it && bottomSheetDialog?.isShowing == true) {
                    bottomSheetDialog?.editText?.text = null
                    bottomSheetDialog?.dismiss()
                }
            }
        }

        viewModel.toastText.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.createDialog.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandledOrReturnNull()?.let {
                val binding = ItemCaptchaBinding.inflate(
                    LayoutInflater.from(requireContext()), null, false
                )
                binding.captchaImg.setImageBitmap(it)
                binding.captchaText.highlightColor = ColorUtils.setAlphaComponent(
                    MaterialColors.getColor(
                        requireContext(),
                        com.google.android.material.R.attr.colorPrimaryDark,
                        0
                    ), 128
                )
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setView(binding.root)
                    setTitle("captcha")
                    setNegativeButton(android.R.string.cancel, null)
                    setPositiveButton("验证并继续") { _, _ ->
                        viewModel.requestValidateData = HashMap()
                        viewModel.requestValidateData["type"] = "err_request_captcha"
                        viewModel.requestValidateData["code"] = binding.captchaText.text.toString()
                        viewModel.requestValidateData["mobile"] = ""
                        viewModel.requestValidateData["idcard"] = ""
                        viewModel.requestValidateData["name"] = ""
                        viewModel.onPostRequestValidate()
                    }
                    show()
                }
            }
        }

        viewModel.footerState.observe(viewLifecycleOwner) {
            footerAdapter.setLoadState(it)
            if (it !is FooterState.Loading) {
                binding.swipeRefresh.isRefreshing = false
                binding.indicator.parent.isIndeterminate = false
                binding.indicator.parent.isVisible = false
            }
            if (dialog != null) {
                dialog?.dismiss()
                dialog === null
            }
        }

        viewModel.feedReplyData.observe(viewLifecycleOwner) {
            viewModel.listSize = it.size
            feedReplyAdapter.submitList(it)
            if (viewModel.isViewReply) {
                viewModel.isViewReply = false
                if (firstVisibleItemPosition > viewModel.itemCount)
                    scrollToPosition(viewModel.itemCount)
            }
        }

    }

    private fun scrollToPosition(position: Int) {
        binding.recyclerView.scrollToPosition(position)
    }

    private fun initData() {
        if (viewModel.isInit) {
            viewModel.isInit = false
            viewModel.isTop?.let { feedReplyAdapter.setHaveTop(it, viewModel.topReplyId) }
            refreshData()
        }
    }

    private fun refreshData() {
        firstVisibleItemPosition = 0
        viewModel.lastVisibleItemPosition = 0
        viewModel.firstItem = null
        viewModel.lastItem = null
        viewModel.page = 1
        viewModel.isEnd = false
        viewModel.isRefreshing = true
        viewModel.isLoadMore = false
        viewModel.fetchFeedReply()
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        feedDataAdapter = FeedDataAdapter(
            ItemClickListener(),
            viewModel.feedDataList,
            viewModel.articleList
        )
        feedReplyAdapter = FeedReplyAdapter(viewModel.blackListRepo, ItemClickListener())
        feedFixAdapter =
            FeedFixAdapter(viewModel.replyCount.toString(), RefreshReplyListener())

        binding.apply {
            refreshListener = RefreshReplyListener()
            listener = ItemClickListener()
            username = viewModel.funame
            avatarUrl = viewModel.avatar
            dateline = viewModel.dateLine
            deviceTitle = viewModel.device
        }
        footerAdapter = FooterAdapter(ReloadListener())

        binding.replyCount.text = "共 ${viewModel.replyCount} 回复"
        setListType()

        binding.recyclerView.apply {
            adapter =
                ConcatAdapter(
                    HeaderAdapter(),
                    feedDataAdapter,
                    feedFixAdapter,
                    feedReplyAdapter,
                    footerAdapter
                )
            layoutManager =
                if (isPortrait) {
                    binding.tabLayout.isVisible = true
                    mLayoutManager = LinearLayoutManager(requireContext())
                    mLayoutManager
                } else {
                    binding.tabLayout.isVisible = false
                    sLayoutManager =
                        StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                    sLayoutManager
                }
            if (viewModel.isViewReply) {
                viewModel.isViewReply = false
                if (isPortrait) {
                    footerAdapter.setLoadState(FooterState.LoadingReply)
                    scrollToPosition(viewModel.itemCount)
                } else {
                    footerAdapter.setLoadState(FooterState.Loading)
                }
            } else {
                footerAdapter.setLoadState(FooterState.Loading)
            }
            if (itemDecorationCount == 0)
                if (isPortrait)
                    addItemDecoration(
                        StickyItemDecorator(requireContext(), 1, viewModel.itemCount,
                            object : StickyItemDecorator.SortShowListener {
                                override fun showSort(show: Boolean) {
                                    binding.tabLayout.visibility =
                                        if (show) View.VISIBLE else View.GONE
                                }
                            })
                    )
                else
                    addItemDecoration(StaggerItemDecoration(10.dp))
        }
    }

    private fun initToolBar() {
        binding.toolBar.apply {
            title = viewModel.feedTypeName
            setNavigationIcon(R.drawable.ic_back)
            setNavigationOnClickListener {
                requireActivity().finish()
            }
            setOnClickListener {
                binding.recyclerView.stopScroll()
                scrollToPosition(0)
            }
            inflateMenu(R.menu.feed_menu)

            menu.findItem(R.id.showReply).isVisible =
                resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE
            menu.findItem(R.id.report).isVisible = PrefManager.isLogin
            menu.findItem(R.id.showQuestion).isVisible = viewModel.feedType == "answer"

            val favorite = menu.findItem(R.id.favorite)
            lifecycleScope.launch(Dispatchers.Main) {
                val isFavorite = viewModel.isFavorite(viewModel.id)
                favorite.title = if (isFavorite) "取消收藏"
                else "收藏"
            }
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.showQuestion -> {
                        viewModel.feedDataList?.getOrNull(0)?.fid?.let {
                            IntentUtil.startActivity<FeedActivity>(requireContext()) {
                                putExtra("id", it)
                            }
                        }
                    }

                    R.id.showReply -> {
                        binding.recyclerView.stopScroll()
                        if (firstVisibleItemPosition <= viewModel.itemCount - 1)
                            mLayoutManager.scrollToPositionWithOffset(viewModel.itemCount, 0)
                        else scrollToPosition(0)
                    }

                    R.id.block -> {
                        MaterialAlertDialogBuilder(requireContext()).apply {
                            setTitle("确定将 ${viewModel.funame} 加入黑名单？")
                            setNegativeButton(android.R.string.cancel, null)
                            setPositiveButton(android.R.string.ok) { _, _ ->
                                viewModel.saveUid(viewModel.uid.toString())
                            }
                            show()
                        }
                    }

                    R.id.share -> {
                        IntentUtil.shareText(
                            requireContext(),
                            "https://www.coolapk1s.com/feed/${viewModel.id}"
                        )
                    }

                    R.id.copyLink -> {
                        ClipboardUtil.copyText(
                            requireContext(),
                            "https://www.coolapk1s.com/feed/${viewModel.id}"
                        )
                    }

                    R.id.report -> {
                        IntentUtil.startActivity<WebViewActivity>(requireContext()) {
                            putExtra(
                                "url",
                                "https://m.coolapk.com/mp/do?c=feed&m=report&type=feed&id=${viewModel.id}"
                            )
                        }
                    }


                    R.id.favorite -> {
                        lifecycleScope.launch(Dispatchers.Main) {
                            val isFavorite = favorite.title == "取消收藏"
                            if (isFavorite) {
                                viewModel.delete(viewModel.id)
                                favorite.title = "收藏"
                                ToastUtil.toast(requireContext(), "已取消收藏")
                            } else {
                                try {
                                    val fav = FeedEntity(
                                        viewModel.id,
                                        viewModel.uid.toString(),
                                        viewModel.funame.toString(),
                                        viewModel.avatar.toString(),
                                        viewModel.device.toString(),
                                        if (!viewModel.articleList.isNullOrEmpty())
                                            viewModel.articleMsg.toString()
                                        else {
                                            with(viewModel.feedDataList?.getOrNull(0)?.message.toString()) {
                                                if (this.length > 150) this.substring(0, 150)
                                                else this
                                            }
                                        }, // 还未加载完会空指针
                                        if (!viewModel.articleList.isNullOrEmpty()) viewModel.articleDateLine.toString()
                                        else viewModel.feedDataList?.getOrNull(0)?.dateline.toString()
                                    )
                                    viewModel.insert(fav)
                                    favorite.title = "取消收藏"
                                    ToastUtil.toast(requireContext(), "已收藏")
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    ToastUtil.toast(requireContext(), "请稍后再试")
                                }
                            }
                        }
                    }

                }
                return@setOnMenuItemClickListener true
            }
        }
    }

    inner class ReloadListener : FooterAdapter.FooterListener {
        override fun onReLoad() {
            loadMore()
        }
    }

    inner class RefreshReplyListener : ReplyRefreshListener {
        @SuppressLint("InflateParams")
        override fun onRefreshReply(listType: String) {
            viewModel.listType = listType
            setListType()
            viewModel.firstItem = null
            viewModel.lastItem = null
            if (listType == "lastupdate_desc" && viewModel.feedTopReplyList.isNotEmpty())
                viewModel.isTop?.let { feedReplyAdapter.setHaveTop(it, viewModel.topReplyId) }
            else
                feedReplyAdapter.setHaveTop(false, null)
            binding.recyclerView.stopScroll()
            if (firstVisibleItemPosition > 1)
                viewModel.isViewReply = true
            viewModel.fromFeedAuthor = if (listType == "") 1
            else 0
            viewModel.page = 1
            viewModel.isEnd = false
            viewModel.isRefreshing = true
            viewModel.isLoadMore = false
            viewModel.isRefreshReply = true
            dialog = MaterialAlertDialogBuilder(
                requireContext(),
                R.style.ThemeOverlay_MaterialAlertDialog_Rounded
            ).apply {
                setView(
                    LayoutInflater.from(requireContext())
                        .inflate(R.layout.dialog_refresh, null, false)
                )
                setCancelable(false)
            }.create()
            dialog?.show()
            val decorView: View? = dialog?.window?.decorView
            val paddingTop: Int = decorView?.paddingTop ?: 0
            val paddingBottom: Int = decorView?.paddingBottom ?: 0
            val paddingLeft: Int = decorView?.paddingLeft ?: 0
            val paddingRight: Int = decorView?.paddingRight ?: 0
            val width = 80.dp + paddingLeft + paddingRight
            val height = 80.dp + paddingTop + paddingBottom
            dialog?.window?.setLayout(width, height)
            viewModel.fetchFeedReply()
        }
    }

    private fun setListType() {
        when (viewModel.listType) {
            "lastupdate_desc" -> binding.buttonToggle.check(R.id.lastUpdate)
            "dateline_desc" -> binding.buttonToggle.check(R.id.dateLine)
            "popular" -> binding.buttonToggle.check(R.id.popular)
            "" -> binding.buttonToggle.check(R.id.author)
        }
        feedFixAdapter.setListType(viewModel.listType)
    }

    inner class ItemClickListener : ItemListener {
        override fun onViewFeed(
            view: View,
            id: String?,
            uid: String?,
            username: String?,
            userAvatar: String?,
            deviceTitle: String?,
            message: String?,
            dateline: String?,
            rid: Any?,
            isViewReply: Any?
        ) {
            super.onViewFeed(
                view,
                id,
                uid,
                username,
                userAvatar,
                deviceTitle,
                message,
                dateline,
                rid,
                isViewReply
            )
            if (!uid.isNullOrEmpty() && PrefManager.isRecordHistory)
                viewModel.saveHistory(
                    id.toString(), uid.toString(), username.toString(), userAvatar.toString(),
                    deviceTitle.toString(), message.toString(), dateline.toString()
                )
        }

        override fun onFollowUser(uid: String, followAuthor: Int) {
            if (PrefManager.isLogin) {
                val url = if (followAuthor == 1) "/v6/user/unfollow" else "/v6/user/follow"
                viewModel.onFollowUnFollow(url, uid, followAuthor)
            }
        }

        override fun onExpand(
            view: View,
            id: String,
            uid: String,
            text: String?,
            position: Int,
            rPosition: Int?
        ) {
            PopupMenu(view.context, view).apply {
                menuInflater.inflate(R.menu.feed_reply_menu, menu).apply {
                    menu.findItem(R.id.delete).isVisible = PrefManager.uid == uid
                    menu.findItem(R.id.report).isVisible = PrefManager.isLogin
                }
                setOnMenuItemClickListener(
                    PopClickListener(
                        id,
                        uid,
                        text,
                        position,
                        rPosition
                    )
                )
                show()
            }
        }

        override fun onReply(
            id: String,
            uid: String,
            username: String?,
            position: Int,
            rPosition: Int?
        ) {
            if (isShowReply) {
                isShowReply = false
                return
            }
            if (PrefManager.isLogin) {
                if (PrefManager.SZLMID == "") {
                    Toast.makeText(requireContext(), SZLM_ID, Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.rid = id
                    viewModel.ruid = uid
                    viewModel.uname = username
                    viewModel.type = "reply"
                    viewModel.position = position
                    viewModel.rPosition = rPosition
                    initReply()
                }
            }
        }

        override fun onLikeClick(type: String, id: String, isLike: Int) {
            if (PrefManager.isLogin)
                if (PrefManager.SZLMID.isEmpty())
                    Toast.makeText(requireContext(), SZLM_ID, Toast.LENGTH_SHORT).show()
                else if (type == "feed")
                    viewModel.onLikeFeed(id, isLike)
                else
                    viewModel.onLikeReply(id, isLike)
        }

        override fun showTotalReply(id: String, uid: String, position: Int, rPosition: Int?) {
            isShowReply = true
            val mBottomSheetDialogFragment =
                Reply2ReplyBottomSheetDialog.newInstance(
                    position,
                    viewModel.uid.toString(),
                    uid,
                    id
                )
            val feedReplyList = viewModel.feedReplyData.value ?: emptyList()
            if (rPosition == null || rPosition == -1)
                mBottomSheetDialogFragment.oriReply.add(feedReplyList[position])
            else
                feedReplyList[position].replyRows?.getOrNull(rPosition)?.let {
                    mBottomSheetDialogFragment.oriReply.add(it)
                }

            mBottomSheetDialogFragment.show(childFragmentManager, "Dialog")
        }

    }

    override fun onPublish(message: String, replyAndForward: String) {
        viewModel.replyData["message"] = message
        viewModel.replyData["replyAndForward"] = replyAndForward
        viewModel.onPostReply()
    }

    inner class PopClickListener(
        private val id: String,
        private val uid: String,
        private val text: String?,
        private val position: Int,
        private val rPosition: Int?
    ) :
        PopupMenu.OnMenuItemClickListener {
        override fun onMenuItemClick(item: MenuItem?): Boolean {
            when (item?.itemId) {
                R.id.block -> {
                    viewModel.saveUid(uid)
                    val replyList = viewModel.feedReplyData.value?.toMutableList() ?: ArrayList()
                    if (rPosition == null || rPosition == -1) {
                        replyList.removeAt(position)
                    } else {
                        replyList[position].replyRows?.removeAt(rPosition)
                    }
                    viewModel.feedReplyData.postValue(replyList)
                    feedReplyAdapter.notifyItemChanged(position)
                }

                R.id.report -> {
                    IntentUtil.startActivity<WebViewActivity>(requireContext()) {
                        putExtra(
                            "url",
                            "https://m.coolapk.com/mp/do?c=feed&m=report&type=feed_reply&id=$id"
                        )
                    }
                }

                R.id.delete -> {
                    viewModel.position = position
                    viewModel.postDeleteFeedReply("/v6/feed/deleteReply", id, position, rPosition)
                }

                R.id.copy -> {
                    IntentUtil.startActivity<CopyActivity>(requireContext()) {
                        putExtra("text", text)
                    }
                }

                R.id.show -> {
                    ItemClickListener().showTotalReply(
                        id,
                        uid,
                        position,
                        rPosition
                    )
                }
            }
            return true
        }
    }

    private val scrollYDistance: Int
        get() {
            val firstVisibleChildView =
                if (isPortrait) mLayoutManager.findViewByPosition(firstVisibleItemPosition)
                else sLayoutManager.findViewByPosition(firstVisibleItemPosition)
            return abs(firstVisibleChildView?.top ?: 0)
        }

    override fun onDestroy() {
        bottomSheetDialog?.dismiss()
        dialog?.dismiss()
        bottomSheetDialog = null
        dialog = null
        super.onDestroy()
    }

}