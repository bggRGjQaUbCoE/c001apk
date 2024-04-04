package com.example.c001apk.ui.homefeed

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.viewModels
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.R
import com.example.c001apk.adapter.FooterState
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.constant.Constants.SZLM_ID
import com.example.c001apk.databinding.BaseRefreshRecyclerviewBinding
import com.example.c001apk.databinding.ItemCaptchaBinding
import com.example.c001apk.ui.base.BaseAppFragment
import com.example.c001apk.ui.feed.reply.IOnPublishClickListener
import com.example.c001apk.ui.feed.reply.ReplyBottomSheetDialog
import com.example.c001apk.ui.home.IOnTabClickContainer
import com.example.c001apk.ui.home.IOnTabClickListener
import com.example.c001apk.ui.main.INavViewContainer
import com.example.c001apk.ui.main.IOnBottomClickContainer
import com.example.c001apk.ui.main.IOnBottomClickListener
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.TokenDeviceUtils.getLastingInstallTime
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFeedFragment : BaseAppFragment<HomeFeedViewModel>(), IOnTabClickListener,
    IOnBottomClickListener, IOnPublishClickListener {

    @Inject
    lateinit var viewModelAssistedFactory: HomeFeedViewModel.Factory
    override val viewModel by viewModels<HomeFeedViewModel> {
        HomeFeedViewModel.provideFactory(
            viewModelAssistedFactory,
            getLastingInstallTime(requireContext())
        )
    }
    private lateinit var bottomSheetDialog: ReplyBottomSheetDialog
    private lateinit var fab: FloatingActionButton
    private val fabViewBehavior by lazy { HideBottomViewOnScrollBehavior<FloatingActionButton>() }

    companion object {
        @JvmStatic
        fun newInstance(type: String) =
            HomeFeedFragment().apply {
                arguments = Bundle().apply {
                    putString("type", type)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BaseRefreshRecyclerviewBinding.inflate(inflater, container, false)
        if (viewModel.type == "feed" && PrefManager.isLogin) {
            fab = FloatingActionButton(requireContext())
            initPublish()
            _binding?.root?.addView(fab)
        }
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.type = it.getString("type")
            when (viewModel.type) {
                "rank" -> {
                    viewModel.dataListUrl = "/page?url=V9_HOME_TAB_RANKING"
                    viewModel.dataListTitle = "热榜"
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
                }

                "coolPic" -> {
                    viewModel.dataListUrl = "/page?url=V11_FIND_COOLPIC"
                    viewModel.dataListTitle = "酷图"
                }
            }
        }
    }

    override fun initObserve() {
        super.initObserve()

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

        viewModel.closeSheet.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandledOrReturnNull()?.let {
                if (it && ::bottomSheetDialog.isInitialized && bottomSheetDialog.isShowing) {
                    bottomSheetDialog.editText.text = null
                    bottomSheetDialog.dismiss()
                }
            }
        }

    }

    @SuppressLint("InflateParams")
    private fun initPublish() {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.dialog_reply_bottom_sheet, null, false)
        bottomSheetDialog = ReplyBottomSheetDialog(requireContext(), view)
        bottomSheetDialog.apply {
            setIOnPublishClickListener(this@HomeFeedFragment)
            setContentView(view)
            setCancelable(false)
            setCanceledOnTouchOutside(true)
            window?.apply {
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            type = "publish"
        }

        fab.apply {
            val lp = CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                CoordinatorLayout.LayoutParams.WRAP_CONTENT
            )
            lp.gravity = Gravity.BOTTOM or Gravity.END
            setImageResource(R.drawable.ic_add1)
            layoutParams = lp
            (this.layoutParams as CoordinatorLayout.LayoutParams).behavior = fabViewBehavior
            if (SDK_INT >= 26)
                tooltipText = getString(R.string.publishFeed)
            setOnClickListener {
                if (PrefManager.SZLMID == "") {
                    Toast.makeText(requireContext(), SZLM_ID, Toast.LENGTH_SHORT).show()
                } else {
                    bottomSheetDialog.show()
                }
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(fab) { _, insets ->
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            fab.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                rightMargin = 25.dp
                bottomMargin =
                    if (isPortrait)
                        navigationBars.bottom + 105.dp
                    else 25.dp
            }
            insets
        }
    }

    override fun onScrolled(dy: Int) {
        if (dy > 0) {
            (activity as? INavViewContainer)?.hideNavigationView()
        } else if (dy < 0) {
            (activity as? INavViewContainer)?.showNavigationView()
        }
    }

    override fun onPause() {
        super.onPause()
        (parentFragment as? IOnTabClickContainer)?.tabController = null
        (activity as? IOnBottomClickContainer)?.controller = null
    }

    override fun onResume() {
        super.onResume()
        (parentFragment as? IOnTabClickContainer)?.tabController = this
        (activity as? IOnBottomClickContainer)?.controller = this
    }

    override fun onReturnTop(isRefresh: Boolean?) {
        if (binding.swipeRefresh.isEnabled) {
            binding.recyclerView.stopScroll()
            if (isRefresh == true) {
                binding.recyclerView.scrollToPosition(0)
                binding.swipeRefresh.isRefreshing = true
                refreshData()
            } else if (viewModel.type == "follow") {
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setTitle("关注分组")
                    val items =
                        arrayOf("全部关注", "好友关注", "话题关注", "数码关注", "应用关注")
                    viewModel.position = when (PrefManager.FOLLOWTYPE) {
                        "all" -> 0
                        "circle" -> 1
                        "topic" -> 2
                        "product" -> 3
                        "apk" -> 4
                        else -> 0
                    }
                    setSingleChoiceItems(
                        items,
                        viewModel.position ?: 0
                    ) { dialog: DialogInterface, position: Int ->
                        when (position) {
                            0 -> {
                                viewModel.dataListUrl = "/page?url=V9_HOME_TAB_FOLLOW"
                                viewModel.dataListTitle = "全部关注"
                                PrefManager.FOLLOWTYPE = "all"
                            }

                            1 -> {
                                viewModel.dataListUrl =
                                    "/page?url=V9_HOME_TAB_FOLLOW&type=circle"
                                viewModel.dataListTitle = "好友关注"
                                PrefManager.FOLLOWTYPE = "circle"
                            }

                            2 -> {
                                viewModel.dataListUrl =
                                    "/page?url=V9_HOME_TAB_FOLLOW&type=topic"
                                viewModel.dataListTitle = "话题关注"
                                PrefManager.FOLLOWTYPE = "topic"
                            }

                            3 -> {
                                viewModel.dataListUrl =
                                    "/page?url=V9_HOME_TAB_FOLLOW&type=product"
                                viewModel.dataListTitle = "数码关注"
                                PrefManager.FOLLOWTYPE = "product"
                            }

                            4 -> {
                                viewModel.dataListUrl = "/page?url=V9_HOME_TAB_FOLLOW&type=apk"
                                viewModel.dataListTitle = "应用关注"
                                PrefManager.FOLLOWTYPE = "apk"
                            }
                        }
                        viewModel.dataList.value = emptyList()
                        viewModel.footerState.value = FooterState.LoadingDone
                        viewModel.loadingState.value = LoadingState.Loading
                        dialog.dismiss()
                    }
                    show()
                }
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

    override fun onReturnTop() {
        onReturnTop(true)
    }

    override fun fetchData() {
        viewModel.fetchData()
        viewModel.changeFirstItem = true
    }

}