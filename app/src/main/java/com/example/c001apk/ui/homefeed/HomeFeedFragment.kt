package com.example.c001apk.ui.homefeed

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.R
import com.example.c001apk.adapter.AppAdapter
import com.example.c001apk.adapter.FooterAdapter
import com.example.c001apk.adapter.HeaderAdapter
import com.example.c001apk.adapter.ItemListener
import com.example.c001apk.constant.Constants.SZLM_ID
import com.example.c001apk.databinding.FragmentHomeFeedBinding
import com.example.c001apk.databinding.ItemCaptchaBinding
import com.example.c001apk.logic.model.Like
import com.example.c001apk.ui.base.BaseFragment
import com.example.c001apk.ui.feed.reply.IOnPublishClickListener
import com.example.c001apk.ui.feed.reply.ReplyBottomSheetDialog
import com.example.c001apk.ui.home.IOnTabClickContainer
import com.example.c001apk.ui.home.IOnTabClickListener
import com.example.c001apk.ui.main.INavViewContainer
import com.example.c001apk.util.DensityTool
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.Utils.getColorFromAttr
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.view.StaggerItemDecoration
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton


class HomeFeedFragment : BaseFragment<FragmentHomeFeedBinding>(), IOnTabClickListener,
    IOnPublishClickListener {

    private val viewModel by viewModels<HomeFeedViewModel> {
        FlowersListViewModelFactory(requireContext())
    }
    private lateinit var mAdapter: AppAdapter
    private lateinit var footerAdapter: FooterAdapter
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!viewModel.isInit) {
            initView()
            initRefresh()
            initScroll()
            initPublish()
            initObserve()
        }

    }

    private fun initObserve() {

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

        viewModel.closeSheet.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandledOrReturnNull()?.let {
                if (it && ::bottomSheetDialog.isInitialized && bottomSheetDialog.isShowing) {
                    bottomSheetDialog.editText.text = null
                    bottomSheetDialog.dismiss()
                }
            }
        }

        viewModel.toastText.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandledOrReturnNull()?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.changeState.observe(viewLifecycleOwner) {
            when (it.first) {
                "error" -> {
                    footerAdapter.setLoadState(
                        FooterAdapter.LoadState.LOADING_ERROR, it.second
                    )
                    footerAdapter.notifyItemChanged(0)
                }

                "done" -> {
                    footerAdapter.setLoadState(FooterAdapter.LoadState.LOADING_COMPLETE, null)
                    footerAdapter.notifyItemChanged(0)
                }

                "end" -> {
                    footerAdapter.setLoadState(FooterAdapter.LoadState.LOADING_END, null)
                    footerAdapter.notifyItemChanged(0)
                }
            }
            binding.swipeRefresh.isRefreshing = false
            binding.indicator.parent.isIndeterminate = false
            binding.indicator.parent.visibility = View.GONE
            viewModel.isLoadMore = false
            viewModel.isRefreshing = false
        }


        viewModel.homeFeedData.observe(viewLifecycleOwner) {
            viewModel.listSize = it.size
            mAdapter.submitList(it)
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
                0, 0, 25.dp,
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                    DensityTool.getNavigationBarHeight(requireContext()) + 105.dp
                else 25.dp
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
                    Toast.makeText(requireContext(), SZLM_ID, Toast.LENGTH_SHORT).show()
                } else {
                    bottomSheetDialog.show()
                }
            }
        } else
            binding.fab.visibility = View.GONE
    }

    private fun initRefresh() {
        binding.swipeRefresh.setColorSchemeColors(
            requireContext().getColorFromAttr(rikka.preference.simplemenu.R.attr.colorPrimary)
        )
        binding.swipeRefresh.setOnRefreshListener {
            binding.indicator.parent.isIndeterminate = false
            binding.indicator.parent.visibility = View.GONE
            binding.swipeRefresh.isRefreshing = true
            refresh()
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

                    if (viewModel.lastVisibleItemPosition == viewModel.listSize + 1
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
        footerAdapter.setLoadState(FooterAdapter.LoadState.LOADING, null)
        footerAdapter.notifyItemChanged(0)
        viewModel.isLoadMore = true
        when (viewModel.type) {
            "feed" -> viewModel.fetchHomeFeed()
            else -> viewModel.fetchDataList()
        }
    }

    private fun initView() {
        mAdapter = AppAdapter(ItemClickListener())
        footerAdapter = FooterAdapter(FooterListener())
        binding.recyclerView.apply {
            adapter = ConcatAdapter(HeaderAdapter(), mAdapter, footerAdapter)
            layoutManager =
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    mLayoutManager = LinearLayoutManager(requireContext())
                    mLayoutManager
                } else {
                    sLayoutManager =
                        StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                    sLayoutManager
                }
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
            initView()
            initRefresh()
            initScroll()
            initPublish()
            initObserve()
            binding.indicator.parent.isIndeterminate = true
            binding.indicator.parent.visibility = View.VISIBLE
            refresh()
        }
    }

    private fun refresh() {
        viewModel.page = 1
        viewModel.lastVisibleItemPosition = 0
        viewModel.isEnd = false
        viewModel.isRefreshing = true
        viewModel.isLoadMore = false
        viewModel.changeFirstItem = true

        when (viewModel.type) {
            "feed" -> viewModel.fetchHomeFeed()
            "rank" -> {
                viewModel.dataListUrl = "/page?url=V9_HOME_TAB_RANKING"
                viewModel.dataListTitle = "热榜"
                viewModel.fetchDataList()
            }

            "follow" -> {
                if (viewModel.dataListUrl.isNullOrEmpty()) {
                    when (PrefManager.FOLLOWTYPE) {
                        "all" -> {
                            viewModel.dataListUrl = "/page?url=V9_HOME_TAB_FOLLOW"
                            viewModel.dataListTitle = "全部关注"
                        }

                        "circle" -> {
                            viewModel.dataListUrl = "/page?url=V9_HOME_TAB_FOLLOW&type=circle"
                            viewModel.dataListTitle = "好友关注"
                        }

                        "topic" -> {
                            viewModel.dataListUrl = "/page?url=V9_HOME_TAB_FOLLOW&type=topic"
                            viewModel.dataListTitle = "话题关注"
                        }

                        else -> {
                            viewModel.dataListUrl = "/page?url=V9_HOME_TAB_FOLLOW&type=product"
                            viewModel.dataListTitle = "数码关注"
                        }
                    }


                }
                viewModel.fetchDataList()
            }

            "coolPic" -> {
                viewModel.dataListUrl = "/page?url=V11_FIND_COOLPIC"
                viewModel.dataListTitle = "酷图"
                viewModel.fetchDataList()
            }
        }


    }

    inner class FooterListener : FooterAdapter.FooterListener {
        override fun onReLoad() {
            loadMore()
        }
    }

    override fun onReturnTop(isRefresh: Boolean?) {
        binding.recyclerView.stopScroll()
        if (isRefresh == true) {
            binding.recyclerView.scrollToPosition(0)
            binding.swipeRefresh.isRefreshing = true
            refresh()
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
                    viewModel.position!!
                ) { dialog: DialogInterface, position: Int ->
                    when (position) {
                        0 -> {
                            viewModel.dataListUrl = "/page?url=V9_HOME_TAB_FOLLOW"
                            viewModel.dataListTitle = "全部关注"
                            PrefManager.FOLLOWTYPE = "all"
                        }

                        1 -> {
                            viewModel.dataListUrl = "/page?url=V9_HOME_TAB_FOLLOW&type=circle"
                            viewModel.dataListTitle = "好友关注"
                            PrefManager.FOLLOWTYPE = "circle"
                        }

                        2 -> {
                            viewModel.dataListUrl = "/page?url=V9_HOME_TAB_FOLLOW&type=topic"
                            viewModel.dataListTitle = "话题关注"
                            PrefManager.FOLLOWTYPE = "topic"
                        }

                        3 -> {
                            viewModel.dataListUrl = "/page?url=V9_HOME_TAB_FOLLOW&type=product"
                            viewModel.dataListTitle = "数码关注"
                            PrefManager.FOLLOWTYPE = "product"
                        }

                        4 -> {
                            viewModel.dataListUrl = "/page?url=V9_HOME_TAB_FOLLOW&type=apk"
                            viewModel.dataListTitle = "应用关注"
                            PrefManager.FOLLOWTYPE = "apk"
                        }
                    }
                    viewModel.homeFeedData.postValue(emptyList())
                    binding.indicator.parent.visibility = View.VISIBLE
                    binding.indicator.parent.isIndeterminate = true
                    refresh()
                    dialog.dismiss()
                }
                show()
            }
        }
    }

    override fun onPublish(message: String, replyAndForward: String) {
        viewModel.createFeedData = HashMap()
        viewModel.createFeedData["id"] = ""
        viewModel.createFeedData["message"] = message
        viewModel.createFeedData["type"] = "feed"
        viewModel.createFeedData["pic"] = ""
        viewModel.createFeedData["status"] = "-1"
        viewModel.onPostCreateFeed()
    }

    inner class ItemClickListener : ItemListener {
        override fun onBlockUser(id: String, uid: String, position: Int) {
            super.onBlockUser(id, uid, position)
            val currentList = viewModel.homeFeedData.value!!.toMutableList()
            currentList.removeAt(position)
            viewModel.homeFeedData.postValue(currentList)
        }

        override fun onDeleteClicked(entityType: String, id: String, position: Int) {
            viewModel.onDeleteFeed("/v6/feed/deleteFeed", id, position)
        }

        override fun onLikeClick(type: String, id: String, position: Int, likeData: Like) {
            if (PrefManager.isLogin) {
                if (PrefManager.SZLMID.isEmpty())
                    Toast.makeText(requireContext(), SZLM_ID, Toast.LENGTH_SHORT).show()
                else viewModel.onPostLikeFeed(id, position, likeData)
            }
        }
    }

}