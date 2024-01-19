package com.example.c001apk.ui.fragment.feed

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.ThemeUtils
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.R
import com.example.c001apk.adapter.FeedAdapter
import com.example.c001apk.databinding.FragmentFeedBinding
import com.example.c001apk.logic.database.FeedFavoriteDatabase
import com.example.c001apk.logic.model.FeedArticleContentBean
import com.example.c001apk.logic.model.FeedFavorite
import com.example.c001apk.logic.model.TotalReplyResponse
import com.example.c001apk.ui.activity.UserActivity
import com.example.c001apk.ui.activity.WebViewActivity
import com.example.c001apk.ui.fragment.BaseFragment
import com.example.c001apk.ui.fragment.ReplyBottomSheetDialog
import com.example.c001apk.ui.fragment.minterface.AppListener
import com.example.c001apk.ui.fragment.minterface.IOnPublishClickListener
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.ClipboardUtil
import com.example.c001apk.util.DateUtils
import com.example.c001apk.util.DensityTool
import com.example.c001apk.util.ImageUtil
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.ToastUtil
import com.example.c001apk.view.OffsetLinearLayoutManager
import com.example.c001apk.view.StaggerItemDecoration
import com.example.c001apk.view.StickyItemDecorator
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLDecoder


class FeedFragment : BaseFragment<FragmentFeedBinding>(), AppListener, IOnPublishClickListener {

    private val viewModel by lazy { ViewModelProvider(requireActivity())[AppViewModel::class.java] }
    private lateinit var bottomSheetDialog: ReplyBottomSheetDialog
    private lateinit var mAdapter: FeedAdapter
    private lateinit var mLayoutManager: OffsetLinearLayoutManager
    private lateinit var sLayoutManager: StaggeredGridLayoutManager
    private val feedFavoriteDao by lazy {
        FeedFavoriteDatabase.getDatabase(requireContext()).feedFavoriteDao()
    }
    private val fabViewBehavior by lazy { HideBottomViewOnScrollBehavior<FloatingActionButton>() }
    private var dialog: AlertDialog? = null

    @SuppressLint("SetTextI18n", "RestrictedApi", "InflateParams", "NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initBar()
        initView()
        initData()
        initButton()
        initRefresh()
        initScroll()

        viewModel.feedReplyData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                if (viewModel.isRefreshReply) {
                    viewModel.feedReplyList.clear()
                }
                val reply = result.getOrNull()
                if (reply?.message != null) {
                    viewModel.errorMessage = reply.message
                    binding.indicator.parent.isIndeterminate = false
                    binding.indicator.parent.visibility = View.GONE
                    viewModel.isEnd = true
                    viewModel.isLoadMore = false
                    viewModel.isRefreshing = false
                    binding.swipeRefresh.isRefreshing = false
                    viewModel.loadState = mAdapter.LOADING_ERROR
                    mAdapter.setLoadState(viewModel.loadState, viewModel.errorMessage)
                    mAdapter.notifyItemChanged(viewModel.itemCount + viewModel.feedReplyList.size + 1)
                    return@observe
                } else if (!reply?.data.isNullOrEmpty()) {
                    if (viewModel.firstItem == null)
                        viewModel.firstItem = reply?.data?.first()?.id
                    viewModel.lastItem = reply?.data?.last()?.id
                    if (viewModel.isRefreshing) {
                        viewModel.feedReplyList.clear()
                        if (viewModel.listType == "lastupdate_desc" && viewModel.feedTopReplyList.isNotEmpty())
                            viewModel.feedReplyList.addAll(viewModel.feedTopReplyList)
                    }
                    if (viewModel.isRefreshing || viewModel.isLoadMore) {
                        viewModel.listSize = viewModel.feedReplyList.size
                        for (element in reply?.data!!) {
                            if (element.entityType == "feed_reply") {
                                if (viewModel.listType == "lastupdate_desc" && viewModel.topReplyId != null
                                    && element.id == viewModel.topReplyId
                                )
                                    continue
                                if (!BlackListUtil.checkUid(element.uid))
                                    viewModel.feedReplyList.add(element)
                            }
                        }
                    }
                    viewModel.loadState = mAdapter.LOADING_COMPLETE
                    mAdapter.setLoadState(viewModel.loadState, null)
                } else if (reply?.data?.isEmpty() == true) {
                    if (viewModel.isRefreshing)
                        viewModel.feedReplyList.clear()
                    viewModel.isEnd = true
                    viewModel.loadState = mAdapter.LOADING_END
                    mAdapter.setLoadState(viewModel.loadState, null)
                } else {
                    viewModel.errorMessage = getString(R.string.loading_failed)
                    viewModel.isEnd = true
                    viewModel.loadState = mAdapter.LOADING_ERROR
                    mAdapter.setLoadState(
                        viewModel.loadState,
                        viewModel.errorMessage
                    )
                    result.exceptionOrNull()?.printStackTrace()
                }
                if (viewModel.isViewReply == true) {
                    viewModel.isViewReply = false
                    mLayoutManager.scrollToPositionWithOffset(viewModel.itemCount, 0)
                }
                binding.replyCount.text = "共 ${viewModel.replyCount} 回复"
                if (viewModel.isRefreshReply)
                    mAdapter.notifyDataSetChanged()
                else if (viewModel.isLoadMore)
                    if (viewModel.isEnd)
                        mAdapter.notifyItemChanged(viewModel.itemCount + viewModel.feedReplyList.size + 1)
                    else
                        mAdapter.notifyItemRangeChanged(
                            viewModel.itemCount + viewModel.listSize + 1,
                            viewModel.feedReplyList.size - viewModel.listSize + 1
                        )
                else
                    mAdapter.notifyDataSetChanged()
                viewModel.isRefreshReply = false
                viewModel.isRefreshing = false
                binding.indicator.parent.isIndeterminate = false
                binding.indicator.parent.visibility = View.GONE
                binding.reply.visibility =
                    if (PrefManager.isLogin) View.VISIBLE
                    else View.GONE
                viewModel.isLoadMore = false
                viewModel.isRefreshing = false
                binding.swipeRefresh.isRefreshing = false
                if (dialog != null) {
                    dialog?.dismiss()
                    dialog = null
                }
            }
        }

        viewModel.likeReplyData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isPostLikeReply) {
                viewModel.isPostLikeReply = false

                val response = result.getOrNull()
                if (response != null) {
                    if (response.data != null) {
                        viewModel.feedReplyList[viewModel.likeReplyPosition - viewModel.itemCount - 1].likenum =
                            response.data
                        viewModel.feedReplyList[viewModel.likeReplyPosition - viewModel.itemCount - 1].userAction?.like =
                            1
                        mAdapter.notifyItemChanged(viewModel.likeReplyPosition, "like")
                    } else
                        Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT)
                            .show()
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
                        viewModel.feedReplyList[viewModel.likeReplyPosition - viewModel.itemCount - 1].likenum =
                            response.data
                        viewModel.feedReplyList[viewModel.likeReplyPosition - viewModel.itemCount - 1].userAction?.like =
                            0
                        mAdapter.notifyItemChanged(viewModel.likeReplyPosition, "like")
                    } else
                        Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT)
                            .show()
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
                        Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT)
                            .show()
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
                        Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT)
                            .show()
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
                        if (response.data.messageStatus != null) {
                            bottomSheetDialog.editText.text = null
                            if (response.data.messageStatus == 1)
                                Toast.makeText(requireContext(), "回复成功", Toast.LENGTH_SHORT)
                                    .show()
                            bottomSheetDialog.dismiss()
                            if (viewModel.type == "feed") {
                                viewModel.feedReplyList.add(
                                    0, TotalReplyResponse.Data(
                                        null,
                                        "feed_reply",
                                        viewModel.id.toString(),
                                        viewModel.ruid.toString(),
                                        PrefManager.uid,
                                        viewModel.id.toString(),
                                        URLDecoder.decode(PrefManager.username, "UTF-8"),
                                        viewModel.uname.toString(),
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
                                mAdapter.notifyItemInserted(viewModel.itemCount + 1)
                                mLayoutManager.scrollToPositionWithOffset(viewModel.itemCount, 0)
                            } else {
                                viewModel.feedReplyList[viewModel.rPosition!! - viewModel.itemCount - 1].replyRows?.add(
                                    viewModel.feedReplyList[viewModel.rPosition!! - viewModel.itemCount - 1].replyRows?.size!!,
                                    TotalReplyResponse.Data(
                                        null,
                                        "feed_reply",
                                        viewModel.rid.toString(),
                                        viewModel.ruid.toString(),
                                        PrefManager.uid,
                                        viewModel.rid.toString(),
                                        URLDecoder.decode(PrefManager.username, "UTF-8"),
                                        viewModel.uname.toString(),
                                        viewModel.replyData["message"].toString(),
                                        "",
                                        null,
                                        System.currentTimeMillis() / 1000,
                                        "0",
                                        "0",
                                        PrefManager.userAvatar,
                                        null,
                                        0,
                                        null
                                    )
                                )
                                mAdapter.notifyItemChanged(viewModel.rPosition!!)
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT)
                            .show()
                        if (response.messageStatus == "err_request_captcha") {
                            viewModel.isGetCaptcha = true
                            viewModel.timeStamp = System.currentTimeMillis() / 1000
                            viewModel.getValidateCaptcha()
                        }
                    }
                }
            }
        }

        viewModel.validateCaptchaData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isGetCaptcha) {
                viewModel.isGetCaptcha = false

                val response = result.getOrNull()
                response?.let {
                    val responseBody = response.body()
                    val bitmap = BitmapFactory.decodeStream(responseBody!!.byteStream())
                    val captchaView = LayoutInflater.from(requireContext())
                        .inflate(R.layout.item_captcha, null, false)
                    val captchaImg: ImageView = captchaView.findViewById(R.id.captchaImg)
                    captchaImg.setImageBitmap(bitmap)
                    val captchaText: TextInputEditText = captchaView.findViewById(R.id.captchaText)
                    captchaText.highlightColor = ColorUtils.setAlphaComponent(
                        ThemeUtils.getThemeAttrColor(
                            requireContext(),
                            rikka.preference.simplemenu.R.attr.colorPrimaryDark
                        ), 128
                    )
                    MaterialAlertDialogBuilder(requireContext()).apply {
                        setView(captchaView)
                        setTitle("captcha")
                        setNegativeButton(android.R.string.cancel, null)
                        setPositiveButton("验证并继续") { _, _ ->
                            viewModel.requestValidateData["type"] = "err_request_captcha"
                            viewModel.requestValidateData["code"] = captchaText.text.toString()
                            viewModel.requestValidateData["mobile"] = ""
                            viewModel.requestValidateData["idcard"] = ""
                            viewModel.requestValidateData["name"] = ""
                            viewModel.isRequestValidate = true
                            viewModel.postRequestValidate()
                        }
                        show()
                    }
                }
            }
        }

        viewModel.postRequestValidateData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isRequestValidate) {
                viewModel.isRequestValidate = false

                val response = result.getOrNull()
                response?.let {
                    if (response.data != null) {
                        Toast.makeText(requireContext(), response.data, Toast.LENGTH_SHORT).show()
                        if (response.data == "验证通过") {
                            viewModel.isCreateFeed = true
                            bottomSheetDialog.editText.text = null
                            bottomSheetDialog.dismiss()
                            //viewModel.postReply()
                        }
                    } else if (response.message != null) {
                        Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT)
                            .show()
                        if (response.message == "请输入正确的图形验证码") {
                            viewModel.isGetCaptcha = true
                            viewModel.timeStamp = System.currentTimeMillis() / 1000
                            viewModel.getValidateCaptcha()
                        }
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

        viewModel.postDeleteData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val response = result.getOrNull()
                if (response != null) {
                    if (response.data == "删除成功") {
                        Toast.makeText(requireContext(), response.data, Toast.LENGTH_SHORT).show()
                        if (viewModel.rPosition == null) {
                            viewModel.feedReplyList.removeAt(viewModel.position - viewModel.itemCount - 1)
                            mAdapter.notifyItemRemoved(viewModel.position)
                        } else {
                            viewModel.feedReplyList[viewModel.position - viewModel.itemCount - 1].replyRows?.removeAt(
                                viewModel.rPosition!!
                            )
                            mAdapter.notifyItemChanged(viewModel.position)
                        }
                    } else if (!response.message.isNullOrEmpty()) {
                        Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object :
            androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(
                recyclerView: androidx.recyclerview.widget.RecyclerView,
                newState: Int
            ) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE) {

                    if (viewModel.feedContentList.isNotEmpty() && !viewModel.isEnd && isAdded) {
                        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                            viewModel.lastVisibleItemPosition =
                                mLayoutManager.findLastVisibleItemPosition()
                        } else {
                            val last = sLayoutManager.findLastVisibleItemPositions(null)
                            for (pos in last) {
                                if (pos > viewModel.lastVisibleItemPosition) {
                                    viewModel.lastVisibleItemPosition = pos
                                }
                            }
                        }
                    }

                    if (viewModel.lastVisibleItemPosition ==
                        viewModel.itemCount + viewModel.feedReplyList.size + 1
                        && !viewModel.isEnd && !viewModel.isRefreshing && !viewModel.isLoadMore
                    ) {
                        viewModel.page++
                        loadMore()
                    }
                }
            }


            override fun onScrolled(
                recyclerView: androidx.recyclerview.widget.RecyclerView,
                dx: Int,
                dy: Int
            ) {
                super.onScrolled(recyclerView, dx, dy)

                if (viewModel.feedContentList.isNotEmpty()) {
                    if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        viewModel.firstCompletelyVisibleItemPosition =
                            mLayoutManager.findFirstCompletelyVisibleItemPosition()
                        viewModel.firstVisibleItemPosition =
                            mLayoutManager.findFirstVisibleItemPosition()
                        viewModel.lastVisibleItemPosition =
                            mLayoutManager.findLastVisibleItemPosition()

                        if (viewModel.firstCompletelyVisibleItemPosition == 0) {
                            if (binding.titleProfile.visibility != View.GONE)
                                binding.titleProfile.visibility = View.GONE
                        } else if (viewModel.firstVisibleItemPosition >= 1) {
                            if (binding.titleProfile.visibility != View.VISIBLE)
                                showTitleProfile()
                        } else if (getScrollYDistance() >= 50.dp) {
                            if (binding.titleProfile.visibility != View.VISIBLE)
                                showTitleProfile()
                        } else {
                            if (binding.titleProfile.visibility != View.GONE) {
                                binding.titleProfile.visibility = View.GONE
                            }
                        }
                    } else {
                        val last = sLayoutManager.findLastVisibleItemPositions(null)
                        for (pos in last) {
                            if (pos > viewModel.lastVisibleItemPosition) {
                                viewModel.lastVisibleItemPosition = pos
                            }
                        }
                    }
                }

            }
        })
    }

    private fun loadMore() {
        viewModel.loadState = mAdapter.LOADING
        mAdapter.setLoadState(viewModel.loadState, null)
        mAdapter.notifyItemChanged(viewModel.itemCount + viewModel.feedReplyList.size + 1)
        viewModel.isLoadMore = true
        viewModel.isNew = true
        viewModel.getFeedReply()
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
            refreshData()
        }
    }

    @SuppressLint("InflateParams")
    private fun refreshReply(listType: String) {
        viewModel.firstItem = null
        viewModel.lastItem = null
        if (listType == "lastupdate_desc" && viewModel.feedTopReplyList.isNotEmpty())
            mAdapter.setHaveTop(viewModel.isTop)
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
        dialog = MaterialAlertDialogBuilder(
            requireContext(),
            R.style.ThemeOverlay_MaterialAlertDialog_Rounded
        ).apply {
            setView(
                LayoutInflater.from(requireContext()).inflate(R.layout.dialog_refresh, null, false)
            )
            setCancelable(false)
        }.create()
        dialog?.window?.setLayout(150.dp, LinearLayout.LayoutParams.WRAP_CONTENT)
        dialog?.show()
        viewModel.getFeedReply()
    }

    @SuppressLint("SetTextI18n", "InflateParams")
    private fun initButton() {
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
        binding.reply.apply {
            val lp = CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                CoordinatorLayout.LayoutParams.WRAP_CONTENT
            )
            lp.setMargins(
                0,
                0,
                25.dp,
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                    DensityTool.getNavigationBarHeight(requireContext()) + 25.dp
                else 25.dp
            )
            lp.gravity = Gravity.BOTTOM or Gravity.END
            layoutParams = lp
            (layoutParams as CoordinatorLayout.LayoutParams).behavior = fabViewBehavior

            setOnClickListener {
                if (PrefManager.SZLMID == "") {
                    Toast.makeText(requireContext(), "数字联盟ID不能为空", Toast.LENGTH_SHORT)
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
        binding.avatar.setOnClickListener {
            IntentUtil.startActivity<UserActivity>(requireContext()) {
                putExtra("id", viewModel.uid)
            }
        }
        mAdapter.setListType(viewModel.listType)
        binding.replyCount.text = "共 ${viewModel.replyCount} 回复"
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

    private fun refreshData() {
        viewModel.firstVisibleItemPosition = -1
        viewModel.lastVisibleItemPosition = -1
        viewModel.firstItem = null
        viewModel.lastItem = null
        viewModel.page = 1
        viewModel.isEnd = false
        viewModel.isRefreshing = true
        viewModel.isLoadMore = false
        viewModel.isNew = true
        viewModel.getFeedReply()
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

    private fun showTitleProfile() {
        if (binding.name.text.isNullOrEmpty()) {
            binding.name.text = viewModel.funame
            binding.date.text = DateUtils.fromToday(viewModel.dateLine)
            if (!viewModel.device.isNullOrEmpty()) {
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
            ImageUtil.showIMG(binding.avatar, viewModel.avatar)
        }
        binding.titleProfile.visibility = View.VISIBLE
    }

    private fun initData() {
        if (viewModel.isInit) {
            viewModel.isInit = false
            mAdapter.setHaveTop(viewModel.isTop)
            binding.titleProfile.visibility = View.GONE
            viewModel.loadState = mAdapter.LOADING_REPLY
            mAdapter.setLoadState(viewModel.loadState, null)
            mAdapter.notifyItemRangeChanged(
                0,
                viewModel.itemCount + 2
            )
            if (viewModel.isViewReply == true) {
                viewModel.isViewReply = false
                mLayoutManager.scrollToPositionWithOffset(viewModel.itemCount, 0)
            }
            refreshData()
        } else {
            mAdapter.setLoadState(viewModel.loadState, viewModel.errorMessage)
            mAdapter.notifyItemChanged(viewModel.itemCount + viewModel.feedReplyList.size + 1)
            if (getScrollYDistance() >= 50.dp)
                showTitleProfile()
            else
                binding.titleProfile.visibility = View.GONE
            binding.reply.visibility =
                if (PrefManager.isLogin) View.VISIBLE
                else View.GONE
        }
    }

    private fun initView() {
        mAdapter =
            FeedAdapter(requireContext(), viewModel.feedContentList, viewModel.feedReplyList)
        mAdapter.setAppListener(this)
        mLayoutManager = OffsetLinearLayoutManager(requireContext())
        sLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        if (viewModel.feedType == "feedArticle" && viewModel.itemCount == 1) {
            viewModel.itemCount = 0

            if (viewModel.feedContentList[0].data?.messageCover?.isNotEmpty() == true) {
                viewModel.itemCount++
            }

            if (viewModel.feedContentList[0].data?.messageTitle?.isNotEmpty() == true) {
                viewModel.itemCount++
            }

            val feedRaw = """{"data":${viewModel.feedContentList[0].data?.messageRawOutput}}"""
            val feedJson: FeedArticleContentBean = Gson().fromJson(
                feedRaw,
                FeedArticleContentBean::class.java
            )
            for (element in feedJson.data) {
                if (element.type == "text" || element.type == "image" || element.type == "shareUrl")
                    viewModel.itemCount++
            }
        }

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            binding.tabLayout.visibility = View.GONE
        else
            binding.tabLayout.visibility = View.VISIBLE
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager =
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                    mLayoutManager
                else sLayoutManager
            if (itemDecorationCount == 0)
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                    addItemDecoration(
                        StickyItemDecorator(
                            requireContext(),
                            1,
                            viewModel.itemCount,
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
                sLayoutManager.scrollToPositionWithOffset(0, 0)
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
                        } else {
                            binding.titleProfile.visibility = View.GONE
                            mLayoutManager.scrollToPositionWithOffset(0, 0)
                        }
                    }

                    R.id.block -> {
                        MaterialAlertDialogBuilder(requireContext()).apply {
                            setTitle("确定将 ${viewModel.funame} 加入黑名单？")
                            setNegativeButton(android.R.string.cancel, null)
                            setPositiveButton(android.R.string.ok) { _, _ ->
                                BlackListUtil.saveUid(viewModel.uid.toString())
                                //requireActivity().finish()
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
                                    ToastUtil.toast("已取消收藏")
                                }
                            } else {
                                try {
                                    val fav = FeedFavorite(
                                        viewModel.id.toString(),
                                        viewModel.uid.toString(),
                                        viewModel.funame.toString(),
                                        viewModel.avatar.toString(),
                                        viewModel.device.toString(),
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

    override fun onShowTotalReply(position: Int, uid: String, id: String, rPosition: Int?) {
        val mBottomSheetDialogFragment =
            Reply2ReplyBottomSheetDialog.newInstance(position, viewModel.uid.toString(), uid, id)
        if (rPosition == null)
            mBottomSheetDialogFragment.oriReply.add(viewModel.feedReplyList[position - viewModel.itemCount - 1])
        else
            mBottomSheetDialogFragment.oriReply.add(viewModel.feedReplyList[position - viewModel.itemCount - 1].replyRows!![rPosition])

        mBottomSheetDialogFragment.show(childFragmentManager, "Dialog")
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
                Toast.makeText(requireContext(), "数字联盟ID不能为空", Toast.LENGTH_SHORT).show()
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

    private fun initReply() {
        bottomSheetDialog.apply {
            rid = viewModel.rid.toString()
            ruid = viewModel.ruid.toString()
            uname = viewModel.uname.toString()
            setData()
            show()
        }
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

    override fun onRefreshReply(listType: String) {
        when (listType) {
            "lastupdate_desc" -> binding.buttonToggle.check(R.id.lastUpdate)
            "dateline_desc" -> binding.buttonToggle.check(R.id.dateLine)
            "popular" -> binding.buttonToggle.check(R.id.popular)
            "" -> binding.buttonToggle.check(R.id.author)
        }
        refreshReply(listType)
    }

    override fun onDeleteFeedReply(id: String, position: Int, rPosition: Int?) {
        viewModel.rPosition = null
        viewModel.rPosition = rPosition
        viewModel.isNew = true
        viewModel.position = position
        viewModel.url = "/v6/feed/deleteReply"
        viewModel.deleteId = id
        viewModel.postDelete()
    }

    override fun onShowCollection(id: String, title: String) {}

    override fun onReload() {
        viewModel.isEnd = false
        loadMore()
    }

    override fun onPublish(message: String, replyAndForward: String) {
        viewModel.replyData["message"] = message
        viewModel.replyData["replyAndForward"] = replyAndForward
        viewModel.isPostReply = true
        viewModel.postReply()
    }

}