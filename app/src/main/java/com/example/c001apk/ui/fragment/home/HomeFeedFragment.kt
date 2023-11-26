package com.example.c001apk.ui.fragment.home

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.ThemeUtils
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.adapter.AppAdapter
import com.example.c001apk.databinding.FragmentHomeFeedBinding
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.ui.fragment.ReplyBottomSheetDialog
import com.example.c001apk.ui.fragment.minterface.IOnLikeClickListener
import com.example.c001apk.ui.fragment.minterface.IOnPublishClickListener
import com.example.c001apk.ui.fragment.minterface.IOnTabClickContainer
import com.example.c001apk.ui.fragment.minterface.IOnTabClickListener
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.ImageUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.TokenDeviceUtils
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.view.ninegridimageview.NineGridImageView
import com.example.c001apk.view.ninegridimageview.OnImageItemClickListener
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText


class HomeFeedFragment : Fragment(), IOnLikeClickListener,
    OnImageItemClickListener, IOnTabClickListener, IOnPublishClickListener {

    private lateinit var binding: FragmentHomeFeedBinding
    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var mAdapter: AppAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var bottomSheetDialog: ReplyBottomSheetDialog

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
            viewModel.type = it.getString("TYPE")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged", "RestrictedApi")
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

                val newList = ArrayList<HomeFeedResponse.Data>()
                val feed = result.getOrNull()
                if (!feed.isNullOrEmpty()) {
                    if (viewModel.isRefreshing) {

                        if (feed.size <= 4 && feed.last().entityTemplate == "refreshCard") {
                            if (viewModel.homeFeedList.size >= 4) {
                                if (viewModel.homeFeedList[3].entityTemplate != "refreshCard") {
                                    viewModel.homeFeedList.add(3, feed.last())
                                    mAdapter.notifyItemInserted(3)
                                }
                            }
                            mAdapter.setLoadState(mAdapter.LOADING_COMPLETE, null)
                            viewModel.isLoadMore = false
                            viewModel.isRefreshing = false
                            binding.swipeRefresh.isRefreshing = false
                            binding.indicator.isIndeterminate = false
                            binding.indicator.visibility = View.GONE
                            return@observe
                        } else {
                            if (PrefManager.isKeepFeed)
                                if (viewModel.homeFeedList.size in 3..50) {
                                    repeat(3) {
                                        viewModel.homeFeedList.removeAt(0)
                                    }
                                    newList.addAll(viewModel.homeFeedList)
                                }
                            viewModel.homeFeedList.clear()
                        }

                    }
                    if (viewModel.isRefreshing || viewModel.isLoadMore) {
                        for (element in feed) {
                            if (element.entityType == "feed"
                                || element.entityTemplate == "iconMiniScrollCard"
                                || element.entityTemplate == "iconLinkGridCard"
                                || element.entityTemplate == "imageCarouselCard_1"
                                || element.entityTemplate == "imageTextScrollCard"
                                || element.entityTemplate == "refreshCard"
                            ) {
                                if (element.entityType == "feed" && viewModel.changeFirstItem) {
                                    viewModel.changeFirstItem = false
                                    viewModel.firstItem = element.id
                                }
                                if (!BlackListUtil.checkUid(element.userInfo?.uid.toString()))
                                    viewModel.homeFeedList.add(element)
                            }

                        }
                        if (PrefManager.isKeepFeed)
                            if (viewModel.isRefreshing && newList.isNotEmpty())
                                viewModel.homeFeedList.addAll(newList)
                        if (viewModel.homeFeedList.last().entityTemplate == "refreshCard") {
                            viewModel.lastItem =
                                viewModel.homeFeedList[viewModel.homeFeedList.size - 2].entityId
                        } else {
                            viewModel.lastItem =
                                viewModel.homeFeedList.last().entityId
                        }
                    }
                    mAdapter.setLoadState(mAdapter.LOADING_COMPLETE, null)
                } else {
                    mAdapter.setLoadState(mAdapter.LOADING_END, null)
                    viewModel.isEnd = true
                    result.exceptionOrNull()?.printStackTrace()
                }
                mAdapter.notifyDataSetChanged()
                viewModel.isLoadMore = false
                viewModel.isRefreshing = false
                binding.swipeRefresh.isRefreshing = false
                binding.indicator.isIndeterminate = false
                binding.indicator.visibility = View.GONE
            }
        }

        viewModel.homeRankingData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val feed = result.getOrNull()
                if (!feed.isNullOrEmpty()) {
                    if (viewModel.isRefreshing)
                        viewModel.homeFeedList.clear()
                    if (viewModel.isRefreshing || viewModel.isLoadMore) {
                        for (element in feed) {
                            if (element.entityType == "feed"
                                || element.entityTemplate == "iconMiniGridCard"
                                || element.entityTemplate == "iconLinkGridCard"
                            ) {
                                if (!BlackListUtil.checkUid(element.userInfo?.uid.toString()))
                                    viewModel.homeFeedList.add(element)
                            }
                        }
                        viewModel.lastItem =
                            viewModel.homeFeedList[viewModel.homeFeedList.size - 1].entityId
                    }
                    mAdapter.setLoadState(mAdapter.LOADING_COMPLETE, null)
                } else {
                    mAdapter.setLoadState(mAdapter.LOADING_END, null)
                    viewModel.isEnd = true
                    result.exceptionOrNull()?.printStackTrace()
                }
                mAdapter.notifyDataSetChanged()
                viewModel.isLoadMore = false
                viewModel.isRefreshing = false
                binding.swipeRefresh.isRefreshing = false
                binding.indicator.isIndeterminate = false
                binding.indicator.visibility = View.GONE
            }
        }

        viewModel.followFeedData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val feed = result.getOrNull()
                if (!feed.isNullOrEmpty()) {
                    if (viewModel.isRefreshing)
                        viewModel.homeFeedList.clear()
                    if (viewModel.isRefreshing || viewModel.isLoadMore) {
                        for (element in feed) {
                            if (element.entityType == "feed")
                                if (!BlackListUtil.checkUid(element.userInfo?.uid.toString()))
                                    viewModel.homeFeedList.add(element)
                            //viewModel.lastItem = feed[feed.size - 1].entityId
                        }
                    }
                    mAdapter.setLoadState(mAdapter.LOADING_COMPLETE, null)
                } else {
                    mAdapter.setLoadState(mAdapter.LOADING_END, null)
                    viewModel.isEnd = true
                    result.exceptionOrNull()?.printStackTrace()
                }
                mAdapter.notifyDataSetChanged()
                viewModel.isLoadMore = false
                viewModel.isRefreshing = false
                binding.swipeRefresh.isRefreshing = false
                binding.indicator.isIndeterminate = false
                binding.indicator.visibility = View.GONE
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
                        mAdapter.notifyItemChanged(viewModel.likePosition)
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
                        viewModel.homeFeedList[viewModel.likePosition].likenum =
                            response.data.count
                        viewModel.homeFeedList[viewModel.likePosition].userAction?.like = 0
                        mAdapter.notifyItemChanged(viewModel.likePosition)
                    } else
                        Toast.makeText(activity, response.message, Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(activity, "发布成功", Toast.LENGTH_SHORT).show()
                        bottomSheetDialog.editText.text = null
                        bottomSheetDialog.dismiss()
                    } else {
                        response.message?.let {
                            Toast.makeText(activity, it, Toast.LENGTH_SHORT).show()
                        }
                        if (response.messageStatus == "err_request_captcha") {
                            viewModel.isGetCaptcha = true
                            viewModel.timeStamp = System.currentTimeMillis() / 1000
                            viewModel.getValidateCaptcha()
                        }
                    }
                } else {
                    Toast.makeText(activity, "response is null", Toast.LENGTH_SHORT).show()
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
                            viewModel.postCreateFeed()
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

    }

    private fun initPublish() {
        if (viewModel.type == "feed" && PrefManager.isLogin) {
            val view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_reply_bottom_sheet, null, false)
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
                    Toast.makeText(activity, "数字联盟ID不能为空", Toast.LENGTH_SHORT).show()
                } else {
                    bottomSheetDialog.show()
                }
            }
        } else
            binding.fab.visibility = View.GONE
    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            @SuppressLint("NotifyDataSetChanged")
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (viewModel.lastVisibleItemPosition == viewModel.homeFeedList.size
                        && !viewModel.isEnd && !viewModel.isRefreshing && !viewModel.isLoadMore
                    ) {
                        mAdapter.setLoadState(mAdapter.LOADING, null)
                        mAdapter.notifyDataSetChanged()
                        viewModel.isLoadMore = true
                        //viewModel.firstItem = null
                        viewModel.page++
                        viewModel.firstLaunch = 0
                        viewModel.isNew = true
                        when (viewModel.type) {
                            "feed" -> viewModel.getHomeFeed()
                            "rank" -> viewModel.getHomeRanking()
                            "follow" -> viewModel.getFollowFeed()
                        }

                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (viewModel.homeFeedList.isNotEmpty()) {
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

    private fun initData() {
        if (viewModel.homeFeedList.isEmpty()) {
            binding.indicator.isIndeterminate = true
            binding.indicator.visibility = View.VISIBLE
            refreshData()
        }
    }

    private fun refreshData() {
        //viewModel.lastItem = null
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
            "rank" -> viewModel.getHomeRanking()
            "follow" -> viewModel.getFollowFeed()
        }
    }

    private fun initView() {
        val space = resources.getDimensionPixelSize(R.dimen.normal_space)
        mAdapter = AppAdapter(
            requireContext(), viewModel.homeFeedList
        )
        mLayoutManager = LinearLayoutManager(activity)
        mAdapter.setIOnLikeReplyListener(this)
        mAdapter.setOnImageItemClickListener(this)
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
            itemAnimator = null
            if (itemDecorationCount == 0)
                addItemDecoration(LinearItemDecoration(space))
        }
    }

    override fun onResume() {
        super.onResume()

        (requireParentFragment() as IOnTabClickContainer).controller = this

        if (viewModel.isInit) {
            viewModel.isInit = false
            initPublish()
            initView()
            initData()
            initRefresh()
            initScroll()
        }
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

    override fun onReturnTop() {
        if (viewModel.firstCompletelyVisibleItemPosition == 0) {
            binding.swipeRefresh.isRefreshing = true
            refreshData()
        } else {
            binding.recyclerView.scrollToPosition(0)
            binding.swipeRefresh.isRefreshing = true
            refreshData()
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

}