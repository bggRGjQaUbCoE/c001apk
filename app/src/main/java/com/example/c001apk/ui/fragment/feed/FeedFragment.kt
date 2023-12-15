package com.example.c001apk.ui.fragment.feed

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.ThemeUtils
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.R
import com.example.c001apk.adapter.FeedContentAdapter
import com.example.c001apk.constant.RecyclerView.checkForGaps
import com.example.c001apk.constant.RecyclerView.markItemDecorInsetsDirty
import com.example.c001apk.databinding.FragmentFeedBinding
import com.example.c001apk.logic.database.FeedFavoriteDatabase
import com.example.c001apk.logic.model.FeedFavorite
import com.example.c001apk.logic.model.TotalReplyResponse
import com.example.c001apk.ui.activity.UserActivity
import com.example.c001apk.ui.activity.WebViewActivity
import com.example.c001apk.ui.fragment.ReplyBottomSheetDialog
import com.example.c001apk.ui.fragment.minterface.AppListener
import com.example.c001apk.ui.fragment.minterface.IOnPublishClickListener
import com.example.c001apk.ui.fragment.minterface.IOnShowMoreReplyContainer
import com.example.c001apk.ui.fragment.minterface.IOnShowMoreReplyListener
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.reflect.Method
import java.net.URLDecoder


class FeedFragment : Fragment(), AppListener, IOnShowMoreReplyListener, IOnPublishClickListener {

    private lateinit var binding: FragmentFeedBinding
    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var bottomSheetDialog: ReplyBottomSheetDialog
    private lateinit var mAdapter: FeedContentAdapter
    private lateinit var mLayoutManager: OffsetLinearLayoutManager
    private lateinit var sLayoutManager: StaggeredGridLayoutManager
    private val feedFavoriteDao by lazy {
        FeedFavoriteDatabase.getDatabase(this@FeedFragment.requireContext()).feedFavoriteDao()
    }
    private val fabViewBehavior by lazy { HideBottomViewOnScrollBehavior<FloatingActionButton>() }
    private lateinit var mCheckForGapMethod: Method
    private lateinit var mMarkItemDecorInsetsDirtyMethod: Method

    companion object {
        @JvmStatic
        fun newInstance(
            type: String?,
            id: String,
            rid: String?,
            uid: String?,
            uname: String?,
            viewReply: Boolean?
        ) =
            FeedFragment().apply {
                arguments = Bundle().apply {
                    putString("TYPE", type)
                    putString("ID", id)
                    putString("RID", rid)
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
            viewModel.frid = it.getString("RID")
            if (viewModel.uid == "")
                viewModel.uid = it.getString("UID", "")
            if (viewModel.funame == "")
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

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n", "InflateParams", "RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                    DensityTool.getNavigationBarHeight(requireContext())
                            + 25.dp
                else
                    25.dp
            )
            lp.gravity = Gravity.BOTTOM or Gravity.END
            layoutParams = lp
            (layoutParams as CoordinatorLayout.LayoutParams).behavior = fabViewBehavior

            setOnClickListener {
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
                    viewModel.device = feed.data.deviceTitle.toString()
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
                        if (!feed.data.topReplyRows.isNullOrEmpty()) {
                            viewModel.isTop = true
                            mAdapter.setHaveTop(viewModel.isTop)
                            viewModel.topReplyId = feed.data.topReplyRows[0].id
                            viewModel.feedTopReplyList.clear()
                            viewModel.feedTopReplyList.addAll(feed.data.topReplyRows)
                        } else if (!feed.data.replyMeRows.isNullOrEmpty()) {
                            viewModel.isTop = false
                            mAdapter.setHaveTop(viewModel.isTop)
                            viewModel.topReplyId = feed.data.replyMeRows[0].id
                            viewModel.feedTopReplyList.clear()
                            viewModel.feedTopReplyList.addAll(feed.data.replyMeRows)
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
                    if (viewModel.firstItem == null)
                        viewModel.firstItem =
                            if (reply?.data?.size!! >= 7)
                                reply.data[6].id
                            else reply.data.first().id
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
                                if (viewModel.listType == "lastupdate_desc" && viewModel.topReplyId != null && element.id == viewModel.topReplyId)
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
                        if (response.data.messageStatus != null) {
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
                                viewModel.feedReplyList[viewModel.rPosition!! - 2].replyRows?.add(
                                    viewModel.feedReplyList[viewModel.rPosition!! - 2].replyRows?.size!!,
                                    TotalReplyResponse.Data(
                                        null,
                                        "feed_reply",
                                        viewModel.rid,
                                        viewModel.ruid,
                                        PrefManager.uid,
                                        viewModel.rid,
                                        URLDecoder.decode(PrefManager.username, "UTF-8"),
                                        viewModel.uname,
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
                        Toast.makeText(activity, response.message, Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(activity, response.data, Toast.LENGTH_SHORT).show()
                        if (response.data == "验证通过") {
                            viewModel.isCreateFeed = true
                            bottomSheetDialog.editText.text = null
                            bottomSheetDialog.dismiss()
                            //viewModel.postReply()
                        }
                    } else if (response.message != null) {
                        Toast.makeText(activity, response.message, Toast.LENGTH_SHORT).show()
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
                            viewModel.feedReplyList.removeAt(viewModel.position - 2)
                            mAdapter.notifyItemRemoved(viewModel.position)
                        } else {
                            viewModel.feedReplyList[viewModel.position - 2].replyRows?.removeAt(
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

    private fun showErrorMessage() {
        binding.errorMessage.visibility = View.VISIBLE
        binding.errorMessage.text = viewModel.errorMessage
    }

    @SuppressLint("SetTextI18n")
    private fun initButton() {
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
                        } else if (getScrollYDistance() >= DensityTool.dp2px(
                                requireContext(),
                                50f
                            )
                        ) {
                            if (binding.titleProfile.visibility != View.VISIBLE)
                                showTitleProfile()
                        } else {
                            if (binding.titleProfile.visibility != View.GONE) {
                                binding.titleProfile.visibility = View.GONE
                            }
                        }
                    } else {
                        val result =
                            mCheckForGapMethod.invoke(binding.recyclerView.layoutManager) as Boolean
                        if (result)
                            mMarkItemDecorInsetsDirtyMethod.invoke(binding.recyclerView)

                        val last = sLayoutManager.findLastVisibleItemPositions(null)
                        for (pos in last) {
                            if (pos > viewModel.lastVisibleItemPosition) {
                                viewModel.lastVisibleItemPosition = pos
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
        if (binding.name1.text.isNullOrEmpty()) {
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
            binding.titleProfile.visibility = View.GONE
            binding.indicator.visibility = View.VISIBLE
            binding.indicator.isIndeterminate = true
            refreshData()
        } else {
            binding.contentLayout.visibility = View.VISIBLE
            if (viewModel.errorMessage != null) {
                mAdapter.setLoadState(mAdapter.LOADING_ERROR, viewModel.errorMessage)
                mAdapter.notifyItemChanged(2)
            } else if (viewModel.isEnd) {
                mAdapter.setLoadState(mAdapter.LOADING_END, null)
                mAdapter.notifyItemChanged(viewModel.feedReplyList.size + 2)
            }
            if (getScrollYDistance() >= DensityTool.dp2px(requireContext(), 50f)) {
                showTitleProfile()
            } else {
                if (binding.titleProfile.visibility != View.GONE)
                    binding.titleProfile.visibility = View.GONE
            }
            if (PrefManager.isLogin)
                binding.reply.visibility = View.VISIBLE
            else
                binding.reply.visibility = View.GONE
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
        viewModel.getFeed()
    }

    private fun initView() {
        val space = resources.getDimensionPixelSize(R.dimen.normal_space)
        mAdapter =
            FeedContentAdapter(requireContext(), viewModel.feedContentList, viewModel.feedReplyList)
        mAdapter.setAppListener(this)
        mLayoutManager = OffsetLinearLayoutManager(activity)
        sLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.tabLayout.visibility = View.GONE
            // https://codeantenna.com/a/2NDTnG37Vg
            mCheckForGapMethod = checkForGaps
            mCheckForGapMethod.isAccessible = true
            mMarkItemDecorInsetsDirtyMethod = markItemDecorInsetsDirty
            mMarkItemDecorInsetsDirtyMethod.isAccessible = true
        } else
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
                            space,
                            object : StickyItemDecorator.SortShowListener {
                                override fun showSort(show: Boolean) {
                                    binding.tabLayout.visibility =
                                        if (show) View.VISIBLE else View.GONE
                                }
                            })
                    )
                else
                    addItemDecoration(StaggerItemDecoration(space))
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

    override fun onShowTotalReply(position: Int, uid: String, id: String, rPosition: Int?) {
        val mBottomSheetDialogFragment =
            Reply2ReplyBottomSheetDialog.newInstance(position, viewModel.uid, uid, id)
        if (rPosition == null)
            mBottomSheetDialogFragment.oriReply.add(viewModel.feedReplyList[position - 2])
        else
            mBottomSheetDialogFragment.oriReply.add(viewModel.feedReplyList[position - 2].replyRows!![rPosition])

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
        var index = 0
        for (element in viewModel.feedReplyList) {
            if (element.id == id)
                break
            else
                index++
        }
        val mBottomSheetDialogFragment =
            Reply2ReplyBottomSheetDialog.newInstance(position, viewModel.uid, uid, id)
        mBottomSheetDialogFragment.oriReply.add(viewModel.feedReplyList[index])
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

}