package com.example.c001apk.ui.homefeed

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.viewModels
import com.example.c001apk.R
import com.example.c001apk.adapter.FooterState
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.adapter.PlaceHolderAdapter
import com.example.c001apk.constant.Constants.SZLM_ID
import com.example.c001apk.databinding.BaseRefreshRecyclerviewBinding
import com.example.c001apk.ui.base.BaseAppFragment
import com.example.c001apk.ui.feed.reply.ReplyActivity
import com.example.c001apk.ui.home.IOnTabClickContainer
import com.example.c001apk.ui.home.IOnTabClickListener
import com.example.c001apk.ui.main.IOnBottomClickContainer
import com.example.c001apk.ui.main.IOnBottomClickListener
import com.example.c001apk.ui.main.MainActivity
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.TokenDeviceUtils.getLastingInstallTime
import com.example.c001apk.util.dp
import com.example.c001apk.util.setSpaceFooterView
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFeedFragment : BaseAppFragment<HomeFeedViewModel>(), IOnTabClickListener,
    IOnBottomClickListener {

    @Inject
    lateinit var viewModelAssistedFactory: HomeFeedViewModel.Factory
    override val viewModel by viewModels<HomeFeedViewModel> {
        HomeFeedViewModel.provideFactory(
            viewModelAssistedFactory,
            getLastingInstallTime(requireContext())
        )
    }
    private lateinit var fab: FloatingActionButton
    private val fabViewBehavior by lazy { HideBottomViewOnScrollBehavior<FloatingActionButton>() }
    private val placeHolderAdapter by lazy { PlaceHolderAdapter() }

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

    override fun initView() {
        super.initView()

        binding.vfContainer.setOnDisplayedChildChangedListener {
            binding.recyclerView.setSpaceFooterView(placeHolderAdapter)
        }
    }

    override fun initObserve() {
        super.initObserve()

        viewModel.toastText.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun initPublish() {
        fab.apply {
            setImageResource(R.drawable.ic_add1)
            layoutParams = CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                CoordinatorLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                behavior = fabViewBehavior
            }
            if (SDK_INT >= 26)
                tooltipText = getString(R.string.publishFeed)
            setOnClickListener {
                if (PrefManager.SZLMID == "") {
                    Toast.makeText(requireContext(), SZLM_ID, Toast.LENGTH_SHORT).show()
                } else {
                    val intent = Intent(requireContext(), ReplyActivity::class.java)
                    intent.putExtra("type", "createFeed")
                    val options = ActivityOptionsCompat.makeCustomAnimation(
                        requireContext(),
                        R.anim.anim_bottom_sheet_slide_up,
                        R.anim.anim_bottom_sheet_slide_down
                    )
                    requireContext().startActivity(intent, options.toBundle())
                }
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(fab) { _, insets ->
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            fab.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                rightMargin = 25.dp
                bottomMargin = navigationBars.bottom + if (isPortrait) 105.dp else 25.dp
            }
            insets
        }
    }

    override fun onScrolled(dy: Int) {
        if (dy > 0) {
            (activity as? MainActivity)?.hideNavigationView()
        } else if (dy < 0) {
            (activity as? MainActivity)?.showNavigationView()
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
                if (viewModel.type == "feed" && fabViewBehavior.isScrolledDown)
                    fabViewBehavior.slideUp(fab, true)
                binding.recyclerView.scrollToPosition(0)
                binding.swipeRefresh.isRefreshing = true
                refreshData()
            } else if (viewModel.type == "follow") {
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setTitle("关注分组")
                    val items = arrayOf("全部关注", "好友关注", "话题关注", "数码关注", "应用关注")
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

    override fun onReturnTop() {
        onReturnTop(true)
    }

    override fun fetchData() {
        viewModel.fetchData()
        viewModel.changeFirstItem = true
    }

}