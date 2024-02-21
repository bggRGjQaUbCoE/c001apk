package com.example.c001apk.ui.feed

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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.R
import com.example.c001apk.adapter.FooterAdapter
import com.example.c001apk.adapter.HeaderAdapter
import com.example.c001apk.adapter.ItemListener
import com.example.c001apk.constant.Constants.SZLM_ID
import com.example.c001apk.databinding.FragmentFeedBinding
import com.example.c001apk.databinding.ItemCaptchaBinding
import com.example.c001apk.logic.database.FeedFavoriteDatabase
import com.example.c001apk.logic.model.FeedFavorite
import com.example.c001apk.logic.model.Like
import com.example.c001apk.ui.base.BaseFragment
import com.example.c001apk.ui.feed.reply.IOnPublishClickListener
import com.example.c001apk.ui.feed.reply.Reply2ReplyBottomSheetDialog
import com.example.c001apk.ui.feed.reply.ReplyBottomSheetDialog
import com.example.c001apk.ui.feed.reply.ReplyRefreshListener
import com.example.c001apk.ui.main.INavViewContainer
import com.example.c001apk.ui.others.CopyActivity
import com.example.c001apk.ui.others.WebViewActivity
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.ClipboardUtil
import com.example.c001apk.util.DensityTool
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.ToastUtil
import com.example.c001apk.util.Utils.getColorFromAttr
import com.example.c001apk.view.OffsetLinearLayoutManager
import com.example.c001apk.view.StaggerItemDecoration
import com.example.c001apk.view.StickyItemDecorator
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FeedFragment : BaseFragment<FragmentFeedBinding>(), IOnPublishClickListener {

    private val viewModel by lazy { ViewModelProvider(requireActivity())[FeedViewModel::class.java] }
    private var bottomSheetDialog: ReplyBottomSheetDialog? = null
    private lateinit var feedDataAdapter: FeedDataAdapter
    private lateinit var feedReplyAdapter: FeedReplyAdapter
    private lateinit var feedFixAdapter: FeedFixAdapter
    private lateinit var footerAdapter: FooterAdapter
    private lateinit var mLayoutManager: OffsetLinearLayoutManager
    private lateinit var sLayoutManager: StaggeredGridLayoutManager
    private val feedFavoriteDao by lazy {
        FeedFavoriteDatabase.getDatabase(requireContext()).feedFavoriteDao()
    }
    private val fabViewBehavior by lazy { HideBottomViewOnScrollBehavior<FloatingActionButton>() }
    private var dialog: AlertDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appBar.setLiftable(true)

        initView()
        initToolBar()
        initData()
        initScroll()
        initReplyBtn()
        initBottomSheet()
        initObserve()

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
                visibility = View.VISIBLE
                val lp = CoordinatorLayout.LayoutParams(
                    CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                    CoordinatorLayout.LayoutParams.WRAP_CONTENT
                )
                lp.setMargins(
                    0, 0, 25.dp,
                    if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                        DensityTool.getNavigationBarHeight(requireContext()) + 25.dp
                    else 25.dp
                )
                lp.gravity = Gravity.BOTTOM or Gravity.END
                layoutParams = lp
                (layoutParams as CoordinatorLayout.LayoutParams).behavior = fabViewBehavior

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
        } else
            binding.reply.visibility = View.GONE
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

                    if (viewModel.listSize != -1 && isAdded) {
                        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                            viewModel.lastVisibleItemPosition =
                                mLayoutManager.findLastVisibleItemPosition()
                            viewModel.firstVisibleItemPosition =
                                mLayoutManager.findFirstVisibleItemPosition()
                        } else {
                            val positions = sLayoutManager.findLastVisibleItemPositions(null)
                            viewModel.lastVisibleItemPosition = positions[0]
                            for (pos in positions) {
                                if (pos > viewModel.lastVisibleItemPosition) {
                                    viewModel.lastVisibleItemPosition = pos
                                }
                            }
                        }
                    }

                    if (viewModel.lastVisibleItemPosition == viewModel.listSize + viewModel.itemCount + 1
                        && !viewModel.isEnd && !viewModel.isRefreshing && !viewModel.isLoadMore
                    ) {
                        viewModel.page++
                        loadMore()
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (viewModel.listSize != -1) {
                    if (dy > 0) {
                        (activity as? INavViewContainer)?.hideNavigationView()
                    } else if (dy < 0) {
                        (activity as? INavViewContainer)?.showNavigationView()
                    }
                }
            }
        })
    }

    private fun loadMore() {
        viewModel.isLoadMore = true
        viewModel.fetchFeedReply()
    }

    private fun initObserve() {

        viewModel.afterFollow.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                feedDataAdapter.notifyItemChanged(0)
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
                    requireContext().getColorFromAttr(rikka.preference.simplemenu.R.attr.colorPrimaryDark),
                    128
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

        viewModel.changeState.observe(viewLifecycleOwner) {
            footerAdapter.setLoadState(it.first, it.second)
            if (it.first != FooterAdapter.LoadState.LOADING) {
                binding.swipeRefresh.isRefreshing = false
                binding.indicator.parent.isIndeterminate = false
                binding.indicator.parent.visibility = View.GONE
                viewModel.isLoadMore = false
                viewModel.isRefreshing = false
            }
        }

        viewModel.feedReplyData.observe(viewLifecycleOwner) {
            viewModel.listSize = it.size
            feedReplyAdapter.submitList(it)
            if (viewModel.isViewReply == true) {
                viewModel.isViewReply = false
                if (viewModel.firstVisibleItemPosition > viewModel.itemCount)
                    scrollToPosition(viewModel.itemCount)
            }
            if (dialog != null) {
                dialog?.dismiss()
                dialog === null
            }
        }

    }

    private fun scrollToPosition(position: Int) {
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
            mLayoutManager.scrollToPositionWithOffset(position, 0)
        else
            sLayoutManager.scrollToPositionWithOffset(position, 0)
    }

    private fun initData() {
        if (viewModel.isInit) {
            viewModel.isInit = false
            viewModel.isTop?.let { feedReplyAdapter.setHaveTop(it, viewModel.topReplyId) }
            binding.titleProfile.visibility = View.GONE
            refresh()
        }/* else {
            if (getScrollYDistance() >= 50.dp) {
                // showTitleProfile()
            } else {
                binding.titleProfile.visibility = View.GONE
            }
        }*/
    }

    private fun refresh() {
        viewModel.firstVisibleItemPosition = 0
        viewModel.lastVisibleItemPosition = 0
        viewModel.firstItem = null
        viewModel.lastItem = null
        viewModel.page = 1
        viewModel.isEnd = false
        viewModel.isRefreshing = true
        viewModel.isLoadMore = false
        viewModel.fetchFeedReply()
    }


    /*private fun getScrollYDistance(): Int {
        val position = mLayoutManager.findFirstVisibleItemPosition()
        val firstVisibleChildView = mLayoutManager.findViewByPosition(position)
        var itemHeight = 0
        var top = 0
        firstVisibleChildView?.let {
            itemHeight = firstVisibleChildView.height
            top = firstVisibleChildView.top
        }
        return position * itemHeight - top
    }*/

    @SuppressLint("SetTextI18n")
    private fun initView() {
        feedDataAdapter = FeedDataAdapter(
            ItemClickListener(),
            viewModel.feedDataList,
            viewModel.articleList
        )
        feedReplyAdapter = FeedReplyAdapter(ItemClickListener())
        feedFixAdapter =
            FeedFixAdapter(viewModel.replyCount.toString(), RefreshReplyListener())

        binding.listener = RefreshReplyListener()
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
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    binding.tabLayout.visibility = View.VISIBLE
                    mLayoutManager = OffsetLinearLayoutManager(requireContext())
                    mLayoutManager
                } else {
                    binding.tabLayout.visibility = View.GONE
                    sLayoutManager =
                        StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                    sLayoutManager
                }
            if (viewModel.isViewReply == true) {
                viewModel.isViewReply = false
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    footerAdapter.setLoadState(FooterAdapter.LoadState.LOADING_REPLY, null)
                    mLayoutManager.scrollToPositionWithOffset(viewModel.itemCount, 0)
                } else {
                    footerAdapter.setLoadState(FooterAdapter.LoadState.LOADING, null)
                }
            } else {
                footerAdapter.setLoadState(FooterAdapter.LoadState.LOADING, null)
            }
            if (itemDecorationCount == 0)
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
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
                    addItemDecoration(StaggerItemDecoration(10.dp, viewModel.itemCount))
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
                binding.titleProfile.visibility = View.GONE
                scrollToPosition(0)
                viewModel.firstVisibleItemPosition = 0
            }
            inflateMenu(R.menu.feed_menu)
            menu.findItem(R.id.showReply).isVisible =
                resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE
            menu.findItem(R.id.report).isVisible = PrefManager.isLogin
            val favorite = menu.findItem(R.id.favorite)
            CoroutineScope(Dispatchers.IO).launch {
                if (feedFavoriteDao.isFavorite(viewModel.id.toString())) {
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
                        if (viewModel.firstVisibleItemPosition <= viewModel.itemCount - 1) {
                            mLayoutManager.scrollToPositionWithOffset(viewModel.itemCount, 0)
                            viewModel.firstVisibleItemPosition = viewModel.itemCount
                        } else {
                            binding.titleProfile.visibility = View.GONE
                            mLayoutManager.scrollToPositionWithOffset(0, 0)
                            viewModel.firstVisibleItemPosition = 0
                        }
                    }

                    R.id.block -> {
                        MaterialAlertDialogBuilder(requireContext()).apply {
                            setTitle("确定将 ${viewModel.funame} 加入黑名单？")
                            setNegativeButton(android.R.string.cancel, null)
                            setPositiveButton(android.R.string.ok) { _, _ ->
                                BlackListUtil.saveUid(viewModel.uid.toString())
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
                        CoroutineScope(Dispatchers.IO).launch {
                            if (feedFavoriteDao.isFavorite(viewModel.id.toString())) {
                                feedFavoriteDao.delete(viewModel.id.toString())
                                withContext(Dispatchers.Main) {
                                    favorite.title = "收藏"
                                    ToastUtil.toast(requireContext(), "已取消收藏")
                                }
                            } else {
                                try {
                                    val fav = FeedFavorite(
                                        viewModel.id.toString(),
                                        viewModel.uid.toString(),
                                        viewModel.funame.toString(),
                                        viewModel.avatar.toString(),
                                        viewModel.device.toString(),
                                        if (viewModel.feedType == "feedArticle") viewModel.articleMsg.toString()
                                        else viewModel.feedDataList!![0].message, // 还未加载完会空指针
                                        if (viewModel.feedType == "feedArticle") viewModel.articleDateLine.toString()
                                        else viewModel.feedDataList!![0].dateline.toString()
                                    )
                                    feedFavoriteDao.insert(fav)
                                    withContext(Dispatchers.Main) {
                                        favorite.title = "取消收藏"
                                        ToastUtil.toast(requireContext(), "已收藏")
                                    }
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
            if (viewModel.firstVisibleItemPosition > 1)
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
            val width = 68.dp + paddingLeft + paddingRight
            val height = 68.dp + paddingTop + paddingBottom
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
        override fun onFollowUser(uid: String, followAuthor: Int, position: Int) {
            if (PrefManager.isLogin)
                if (followAuthor == 1) {
                    viewModel.onPostFollowUnFollow("/v6/user/unfollow", uid, followAuthor)
                } else {
                    viewModel.onPostFollowUnFollow("/v6/user/follow", uid, followAuthor)
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
            if (PrefManager.isLogin) {
                if (PrefManager.SZLMID == "") {
                    Toast.makeText(requireContext(), SZLM_ID, Toast.LENGTH_SHORT)
                        .show()
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

        override fun onLikeClick(type: String, id: String, position: Int, likeData: Like) {
            if (PrefManager.isLogin)
                if (PrefManager.SZLMID.isEmpty())
                    Toast.makeText(requireContext(), SZLM_ID, Toast.LENGTH_SHORT).show()
                else
                    if (type == "feed")
                        viewModel.onPostLikeFeed(id, likeData)
                    else
                        viewModel.onPostLikeReply(id, position, likeData)
        }

        override fun showTotalReply(id: String, uid: String, position: Int, rPosition: Int?) {
            val mBottomSheetDialogFragment =
                Reply2ReplyBottomSheetDialog.newInstance(
                    position,
                    viewModel.uid.toString(),
                    uid,
                    id
                )
            val feedReplyList = viewModel.feedReplyData.value!!
            if (rPosition == null || rPosition == -1)
                mBottomSheetDialogFragment.oriReply.add(feedReplyList[position])
            else
                mBottomSheetDialogFragment.oriReply.add(
                    feedReplyList[position].replyRows!![rPosition]
                )

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
                    BlackListUtil.saveUid(uid)
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

    override fun onDestroy() {
        bottomSheetDialog?.dismiss()
        dialog?.dismiss()
        bottomSheetDialog = null
        dialog = null
        super.onDestroy()
    }

}