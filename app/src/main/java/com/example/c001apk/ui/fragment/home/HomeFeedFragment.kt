package com.example.c001apk.ui.fragment.home

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.ThemeUtils
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.R
import com.example.c001apk.adapter.AppAdapter
import com.example.c001apk.databinding.FragmentHomeFeedBinding
import com.example.c001apk.ui.fragment.BaseFragment
import com.example.c001apk.ui.fragment.ReplyBottomSheetDialog
import com.example.c001apk.ui.fragment.minterface.AppListener
import com.example.c001apk.ui.fragment.minterface.INavViewContainer
import com.example.c001apk.ui.fragment.minterface.IOnPublishClickListener
import com.example.c001apk.ui.fragment.minterface.IOnTabClickContainer
import com.example.c001apk.ui.fragment.minterface.IOnTabClickListener
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.DensityTool
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.TokenDeviceUtils
import com.example.c001apk.util.TopicBlackListUtil
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.view.StaggerItemDecoration
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText


class HomeFeedFragment : BaseFragment<FragmentHomeFeedBinding>(), AppListener, IOnTabClickListener,
    IOnPublishClickListener {

    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var mAdapter: AppAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var sLayoutManager: StaggeredGridLayoutManager
    private lateinit var bottomSheetDialog: ReplyBottomSheetDialog
    private val fabViewBehavior by lazy { HideBottomViewOnScrollBehavior<FloatingActionButton>() }

    companion object {
        @JvmStatic
        fun newInstance(type: String) =
            HomeFeedFragment().apply {
                arguments = Bundle().apply {
                    putString("TYPE", type)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.type = it.getString("TYPE")
        }
    }

    @SuppressLint("NotifyDataSetChanged", "RestrictedApi", "InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!viewModel.isInit) {
            initPublish()
            initView()
            initData()
            initRefresh()
            initScroll()
        }

        viewModel.homeFeedData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val feed = result.getOrNull()
                if (!feed?.message.isNullOrEmpty()) {
                    viewModel.loadState = mAdapter.LOADING_ERROR
                    viewModel.errorMessage = feed?.message
                    mAdapter.setLoadState(viewModel.loadState, viewModel.errorMessage)
                    mAdapter.notifyItemChanged(viewModel.homeFeedList.size)
                    binding.indicator.parent.isIndeterminate = false
                    binding.indicator.parent.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    return@observe
                } else if (!feed?.data.isNullOrEmpty()) {
                    if (viewModel.isRefreshing) {
                        if (feed?.data!!.size <= 4 && feed.data.last().entityTemplate == "refreshCard") {
                            val index = if (PrefManager.isIconMiniCard) 4
                            else 3
                            if (viewModel.homeFeedList.size >= index) {
                                if (viewModel.homeFeedList[index - 1].entityTemplate != "refreshCard") {
                                    viewModel.homeFeedList.add(index - 1, feed.data.last())
                                    mAdapter.notifyItemInserted(index - 1)
                                }
                            }
                            viewModel.loadState = mAdapter.LOADING_COMPLETE
                            mAdapter.setLoadState(viewModel.loadState, null)
                            viewModel.isLoadMore = false
                            viewModel.isRefreshing = false
                            binding.swipeRefresh.isRefreshing = false
                            binding.indicator.parent.isIndeterminate = false
                            binding.indicator.parent.visibility = View.GONE
                            return@observe
                        } else {
                            viewModel.homeFeedList.clear()
                        }
                    }
                    if (viewModel.isRefreshing || viewModel.isLoadMore) {
                        viewModel.listSize = viewModel.homeFeedList.size
                        for (element in feed?.data!!) {
                            if (!PrefManager.isIconMiniCard && element.entityTemplate == "iconMiniScrollCard")
                                continue
                            else if (element.entityType == "feed"
                                || element.entityTemplate == "iconMiniScrollCard"
                                || element.entityTemplate == "iconLinkGridCard"
                                || element.entityTemplate == "imageCarouselCard_1"
                                || element.entityTemplate == "imageTextScrollCard"
                            ) {
                                if (element.entityType == "feed" && viewModel.changeFirstItem) {
                                    viewModel.changeFirstItem = false
                                    viewModel.firstItem = element.id
                                }
                                if (!BlackListUtil.checkUid(element.userInfo?.uid.toString())
                                    && !TopicBlackListUtil.checkTopic(
                                        element.tags + element.ttitle
                                    )
                                )
                                    viewModel.homeFeedList.add(element)
                            }
                        }
                        viewModel.lastItem = if (feed.data.last().entityTemplate != "refreshCard")
                            feed.data.last().entityId
                        else if (feed.data[feed.data.size - 2].entityTemplate != "refreshCard")
                            feed.data[feed.data.size - 2].entityId
                        else ""
                    }
                    viewModel.loadState = mAdapter.LOADING_COMPLETE
                    mAdapter.setLoadState(viewModel.loadState, null)
                } else if (feed?.data?.isEmpty() == true) {
                    if (viewModel.isRefreshing)
                        viewModel.homeFeedList.clear()
                    viewModel.isEnd = true
                    viewModel.loadState = mAdapter.LOADING_END
                    mAdapter.setLoadState(viewModel.loadState, null)
                } else {
                    viewModel.isEnd = true
                    viewModel.loadState = mAdapter.LOADING_ERROR
                    viewModel.errorMessage = getString(R.string.loading_failed)
                    mAdapter.setLoadState(
                        viewModel.loadState,
                        viewModel.errorMessage
                    )
                    result.exceptionOrNull()?.printStackTrace()
                }
                if (viewModel.isLoadMore)
                    if (viewModel.isEnd)
                        mAdapter.notifyItemChanged(viewModel.homeFeedList.size)
                    else
                        mAdapter.notifyItemRangeChanged(
                            viewModel.listSize,
                            viewModel.homeFeedList.size - viewModel.listSize + 1
                        )
                else
                    mAdapter.notifyDataSetChanged()
                viewModel.isLoadMore = false
                viewModel.isRefreshing = false
                binding.swipeRefresh.isRefreshing = false
                binding.indicator.parent.isIndeterminate = false
                binding.indicator.parent.visibility = View.GONE
            }
        }

        viewModel.dataListData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val feed = result.getOrNull()
                if (!feed?.message.isNullOrEmpty()) {
                    viewModel.loadState = mAdapter.LOADING_ERROR
                    viewModel.errorMessage = feed?.message
                    mAdapter.setLoadState(viewModel.loadState, viewModel.errorMessage)
                    mAdapter.notifyItemChanged(viewModel.homeFeedList.size)
                    binding.indicator.parent.isIndeterminate = false
                    binding.indicator.parent.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    return@observe
                } else if (!feed?.data.isNullOrEmpty()) {
                    if (viewModel.isRefreshing)
                        viewModel.homeFeedList.clear()
                    if (viewModel.isRefreshing || viewModel.isLoadMore) {
                        viewModel.listSize = viewModel.homeFeedList.size
                        for (element in feed?.data!!) {
                            if (!PrefManager.isIconMiniCard
                                && element.entityTemplate == "iconMiniGridCard"
                            )
                                continue
                            else if (element.entityType == "feed"
                                || element.entityTemplate == "iconMiniGridCard"
                                || element.entityTemplate == "iconLinkGridCard"
                                || element.entityTemplate == "imageSquareScrollCard"
                            ) {
                                if (!BlackListUtil.checkUid(element.userInfo?.uid.toString())
                                    && !TopicBlackListUtil.checkTopic(
                                        element.tags + element.ttitle
                                    )
                                )
                                    viewModel.homeFeedList.add(element)
                            }
                        }
                    }
                    viewModel.loadState = mAdapter.LOADING_COMPLETE
                    mAdapter.setLoadState(viewModel.loadState, null)
                } else if (feed?.data?.isEmpty() == true) {
                    if (viewModel.isRefreshing)
                        viewModel.homeFeedList.clear()
                    viewModel.isEnd = true
                    viewModel.loadState = mAdapter.LOADING_END
                    mAdapter.setLoadState(viewModel.loadState, null)
                } else {
                    viewModel.isEnd = true
                    viewModel.loadState = mAdapter.LOADING_ERROR
                    viewModel.errorMessage = getString(R.string.loading_failed)
                    mAdapter.setLoadState(
                        viewModel.loadState,
                        viewModel.errorMessage
                    )
                    result.exceptionOrNull()?.printStackTrace()
                }
                if (viewModel.isLoadMore)
                    if (viewModel.isEnd)
                        mAdapter.notifyItemChanged(viewModel.homeFeedList.size)
                    else
                        mAdapter.notifyItemRangeChanged(
                            viewModel.listSize,
                            viewModel.homeFeedList.size - viewModel.listSize + 1
                        )
                else
                    mAdapter.notifyDataSetChanged()
                viewModel.isLoadMore = false
                viewModel.isRefreshing = false
                binding.swipeRefresh.isRefreshing = false
                binding.indicator.parent.isIndeterminate = false
                binding.indicator.parent.visibility = View.GONE
            }
        }

        viewModel.likeFeedData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isPostLikeFeed) {
                viewModel.isPostLikeFeed = false

                val response = result.getOrNull()
                if (response != null) {
                    if (response.data != null) {
                        viewModel.homeFeedList[viewModel.likePosition].likenum =
                            response.data.count
                        viewModel.homeFeedList[viewModel.likePosition].userAction?.like = 1
                        mAdapter.notifyItemChanged(viewModel.likePosition, "like")
                    } else
                        Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
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
                        viewModel.homeFeedList[viewModel.likePosition].likenum =
                            response.data.count
                        viewModel.homeFeedList[viewModel.likePosition].userAction?.like = 0
                        mAdapter.notifyItemChanged(viewModel.likePosition, "like")
                    } else
                        Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

        viewModel.postCreateFeedData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isCreateFeed) {
                viewModel.isCreateFeed = false

                val response = result.getOrNull()
                if (response != null) {
                    if (response.id != null) {
                        Toast.makeText(requireContext(), "发布成功", Toast.LENGTH_SHORT).show()
                        bottomSheetDialog.editText.text = null
                        bottomSheetDialog.dismiss()
                    } else {
                        response.message?.let {
                            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                        }
                        if (response.messageStatus == "err_request_captcha") {
                            viewModel.isGetCaptcha = true
                            viewModel.timeStamp = System.currentTimeMillis() / 1000
                            viewModel.getValidateCaptcha()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "response is null", Toast.LENGTH_SHORT).show()
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
                            //viewModel.postCreateFeed()
                        }
                    } else if (response.message != null) {
                        Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                        if (response.message == "请输入正确的图形验证码") {
                            viewModel.isGetCaptcha = true
                            viewModel.timeStamp = System.currentTimeMillis() / 1000
                            viewModel.getValidateCaptcha()
                        }
                    }
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
                        viewModel.homeFeedList.removeAt(viewModel.position)
                        mAdapter.notifyItemRemoved(viewModel.position)
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

    @SuppressLint("InflateParams")
    private fun initPublish() {
        if (viewModel.type == "feed" && PrefManager.isLogin) {
            val view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_reply_bottom_sheet, null, false)
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
                            + 105.dp
                else
                    25.dp
            )
            lp.gravity = Gravity.BOTTOM or Gravity.END
            binding.fab.layoutParams = lp
            (binding.fab.layoutParams as CoordinatorLayout.LayoutParams).behavior = fabViewBehavior
            binding.fab.visibility = View.VISIBLE
            bottomSheetDialog = ReplyBottomSheetDialog(requireContext(), view)
            bottomSheetDialog.setIOnPublishClickListener(this)
            bottomSheetDialog.apply {
                setContentView(view)
                setCancelable(false)
                setCanceledOnTouchOutside(true)
                window?.apply {
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
                type = "publish"
            }
            binding.fab.setOnClickListener {
                if (PrefManager.SZLMID == "") {
                    Toast.makeText(requireContext(), "数字联盟ID不能为空", Toast.LENGTH_SHORT).show()
                } else {
                    bottomSheetDialog.show()
                }
            }
        } else
            binding.fab.visibility = View.GONE
    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                    if (viewModel.homeFeedList.isNotEmpty() && isAdded) {
                        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                            viewModel.lastVisibleItemPosition =
                                mLayoutManager.findLastVisibleItemPosition()
                            viewModel.firstCompletelyVisibleItemPosition =
                                mLayoutManager.findFirstCompletelyVisibleItemPosition()
                        } else {
                            val positions = sLayoutManager.findLastVisibleItemPositions(null)
                            for (pos in positions) {
                                if (pos > viewModel.lastVisibleItemPosition) {
                                    viewModel.lastVisibleItemPosition = pos
                                }
                            }
                        }
                    }

                    if (viewModel.lastVisibleItemPosition == viewModel.homeFeedList.size
                        && !viewModel.isEnd && !viewModel.isRefreshing && !viewModel.isLoadMore
                    ) {
                        viewModel.page++
                        loadMore()
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (viewModel.homeFeedList.isNotEmpty()) {
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
        viewModel.loadState = mAdapter.LOADING
        mAdapter.setLoadState(viewModel.loadState, null)
        mAdapter.notifyItemChanged(viewModel.homeFeedList.size)
        viewModel.isLoadMore = true
        viewModel.firstLaunch = 0
        viewModel.isNew = true
        when (viewModel.type) {
            "feed" -> viewModel.getHomeFeed()
            "rank" -> viewModel.getDataList()
            "follow" -> viewModel.getDataList()
            "coolPic" -> viewModel.getDataList()
        }
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
            binding.indicator.parent.isIndeterminate = false
            binding.indicator.parent.visibility = View.GONE
            refreshData()
        }
    }

    private fun initData() {
        if (viewModel.homeFeedList.isEmpty()) {
            binding.indicator.parent.isIndeterminate = true
            binding.indicator.parent.visibility = View.VISIBLE
            refreshData()
        } else {
            mAdapter.setLoadState(viewModel.loadState, viewModel.errorMessage)
            mAdapter.notifyItemChanged(viewModel.homeFeedList.size)
        }
    }

    private fun refreshData() {
        //viewModel.lastItem = null
        viewModel.firstVisibleItemPosition = -1
        viewModel.lastVisibleItemPosition = -1
        viewModel.changeFirstItem = true
        viewModel.isEnd = false
        viewModel.page = 1
        viewModel.firstLaunch = 0
        viewModel.isRefreshing = true
        viewModel.isLoadMore = false
        viewModel.installTime = TokenDeviceUtils.getLastingInstallTime(requireContext())
        viewModel.isNew = true
        when (viewModel.type) {
            "feed" -> viewModel.getHomeFeed()
            "rank" -> {
                viewModel.url = "/page?url=V9_HOME_TAB_RANKING"
                viewModel.title = "热榜"
                viewModel.getDataList()
            }

            "follow" -> {
                if (viewModel.url.isNullOrEmpty()) {
                    when (PrefManager.FOLLOWTYPE) {
                        "all" -> {
                            viewModel.url = "/page?url=V9_HOME_TAB_FOLLOW"
                            viewModel.title = "全部关注"
                        }

                        "circle" -> {
                            viewModel.url = "/page?url=V9_HOME_TAB_FOLLOW&type=circle"
                            viewModel.title = "好友关注"
                        }

                        "topic" -> {
                            viewModel.url = "/page?url=V9_HOME_TAB_FOLLOW&type=topic"
                            viewModel.title = "话题关注"
                        }

                        else -> {
                            viewModel.url = "/page?url=V9_HOME_TAB_FOLLOW&type=product"
                            viewModel.title = "数码关注"
                        }
                    }


                }
                viewModel.getDataList()
            }

            "coolPic" -> {
                viewModel.url = "/page?url=V11_FIND_COOLPIC"
                viewModel.title = "酷图"
                viewModel.getDataList()
            }
        }
    }

    private fun initView() {
        mAdapter = AppAdapter(requireContext(), viewModel.homeFeedList)
        mLayoutManager = LinearLayoutManager(requireContext())
        sLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        mAdapter.setAppListener(this)
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager =
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                    mLayoutManager
                else sLayoutManager
            if (itemDecorationCount == 0) {
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                    addItemDecoration(LinearItemDecoration(10.dp))
                else
                    addItemDecoration(StaggerItemDecoration(10.dp))
            }
        }
    }

    override fun onPause() {
        super.onPause()
        (requireParentFragment() as? IOnTabClickContainer)?.tabController = null
    }

    override fun onResume() {
        super.onResume()

        (requireParentFragment() as? IOnTabClickContainer)?.tabController = this

        if (viewModel.isInit) {
            viewModel.isInit = false
            initPublish()
            initView()
            initData()
            initRefresh()
            initScroll()
        }
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

    override fun onDeleteFeedReply(id: String, position: Int, rPosition: Int?) {
        viewModel.isNew = true
        viewModel.position = position
        viewModel.url = "/v6/feed/deleteFeed"
        viewModel.deleteId = id
        viewModel.postDelete()
    }

    override fun onShowCollection(id: String, title: String) {}

    @SuppressLint("NotifyDataSetChanged")
    override fun onReturnTop(isRefresh: Boolean?) {
        binding.recyclerView.stopScroll()
        if (isRefresh!!) {
            if (viewModel.firstCompletelyVisibleItemPosition == 0) {
                binding.swipeRefresh.isRefreshing = true
                refreshData()
            } else {
                viewModel.firstCompletelyVisibleItemPosition = 0
                binding.recyclerView.scrollToPosition(0)
                binding.swipeRefresh.isRefreshing = true
                refreshData()
            }
        } else if (viewModel.type == "follow") {
            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle("关注分组")
                val items = arrayOf("全部关注", "好友关注", "话题关注", "数码关注", "应用关注")
                viewModel.position = when (PrefManager.FOLLOWTYPE) {
                    "all" -> 0
                    "circle" -> 1
                    "topic" -> 2
                    else -> 3
                }
                setSingleChoiceItems(
                    items,
                    viewModel.position
                ) { dialog: DialogInterface, position: Int ->
                    when (position) {
                        0 -> {
                            viewModel.url = "/page?url=V9_HOME_TAB_FOLLOW"
                            viewModel.title = "全部关注"
                            PrefManager.FOLLOWTYPE = "all"
                        }

                        1 -> {
                            viewModel.url = "/page?url=V9_HOME_TAB_FOLLOW&type=circle"
                            viewModel.title = "好友关注"
                            PrefManager.FOLLOWTYPE = "circle"
                        }

                        2 -> {
                            viewModel.url = "/page?url=V9_HOME_TAB_FOLLOW&type=topic"
                            viewModel.title = "话题关注"
                            PrefManager.FOLLOWTYPE = "topic"
                        }

                        3 -> {
                            viewModel.url = "/page?url=V9_HOME_TAB_FOLLOW&type=product"
                            viewModel.title = "数码关注"
                            PrefManager.FOLLOWTYPE = "product"
                        }

                        4 -> {
                            viewModel.url = "/page?url=V9_HOME_TAB_FOLLOW&type=apk"
                            viewModel.title = "应用关注"
                            PrefManager.FOLLOWTYPE = "apk"
                        }
                    }
                    viewModel.homeFeedList.clear()
                    mAdapter.notifyDataSetChanged()
                    binding.indicator.parent.visibility = View.VISIBLE
                    binding.indicator.parent.isIndeterminate = true
                    refreshData()
                    dialog.dismiss()
                }
                show()
            }
        }
    }

    override fun onPublish(message: String, replyAndForward: String) {
        viewModel.createFeedData["id"] = ""
        viewModel.createFeedData["message"] = message
        viewModel.createFeedData["type"] = "feed"
        viewModel.createFeedData["is_html_artical"] = "0"
        viewModel.createFeedData["pic"] = ""
        viewModel.createFeedData["status"] = "-1"
        viewModel.createFeedData["location"] = ""
        viewModel.createFeedData["long_location"] = ""
        viewModel.createFeedData["latitude"] = "0.0"
        viewModel.createFeedData["longitude"] = "0.0"
        viewModel.createFeedData["media_url"] = ""
        viewModel.createFeedData["media_type"] = "0"
        viewModel.createFeedData["media_pic"] = ""
        viewModel.createFeedData["message_title"] = ""
        viewModel.createFeedData["message_brief"] = ""
        viewModel.createFeedData["extra_title"] = ""
        viewModel.createFeedData["extra_url"] = ""
        viewModel.createFeedData["extra_key"] = ""
        viewModel.createFeedData["extra_pic"] = ""
        viewModel.createFeedData["extra_info"] = ""
        viewModel.createFeedData["message_cover"] = ""
        viewModel.createFeedData["disallow_repost"] = "0"
        viewModel.createFeedData["is_anonymous"] = "0"
        viewModel.createFeedData["is_editInDyh"] = "0"
        viewModel.createFeedData["forwardid"] = ""
        viewModel.createFeedData["fid"] = ""
        viewModel.createFeedData["dyhId"] = ""
        viewModel.createFeedData["targetType"] = ""
        viewModel.createFeedData["productId"] = ""
        viewModel.createFeedData["province"] = ""
        viewModel.createFeedData["city_code"] = ""
        viewModel.createFeedData["province"] = ""
        viewModel.createFeedData["city_code"] = ""
        viewModel.createFeedData["targetId"] = ""
        viewModel.createFeedData["location_city"] = ""
        viewModel.createFeedData["location_country"] = ""
        viewModel.createFeedData["disallow_reply"] = "0"
        viewModel.createFeedData["vote_score"] = "0"
        viewModel.createFeedData["replyWithForward"] = "0"
        viewModel.createFeedData["media_info"] = ""
        viewModel.createFeedData["insert_product_media"] = "0"
        viewModel.createFeedData["is_ks_doc"] = "0"
        viewModel.createFeedData["goods_list_id"] = ""
        viewModel.isCreateFeed = true
        viewModel.postCreateFeed()
    }

    override fun onReload() {
        viewModel.isEnd = false
        loadMore()
    }

}