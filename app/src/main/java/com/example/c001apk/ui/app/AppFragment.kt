package com.example.c001apk.ui.app

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.c001apk.R
import com.example.c001apk.databinding.BaseViewAppBinding
import com.example.c001apk.ui.base.BasePagerFragment
import com.example.c001apk.ui.search.SearchActivity
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.ReplaceViewHelper
import com.example.c001apk.view.AppBarLayoutStateChangeListener
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout

class AppFragment : BasePagerFragment() {

    private val viewModel by viewModels<AppViewModel>(ownerProducer = { requireActivity() })
    private val typeList = listOf("reply", "pub", "hot")
    private lateinit var appBinding: BaseViewAppBinding
    private var menuSubscribe: MenuItem? = null
    private var menuBlock: MenuItem? = null
    private var menuSearch: MenuItem? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initApp()
        initAppBar()

    }

    override fun initBar() {
        super.initBar()
        binding.collapsingToolbar.title = viewModel.appData?.title

        // menu
        binding.toolBar.apply {
            inflateMenu(R.menu.topic_product_menu)

            menuSubscribe = menu?.findItem(R.id.subscribe)
            menuBlock = menu?.findItem(R.id.block)
            menuSearch = menu?.findItem(R.id.search)

            updateFollow(viewModel.appData?.userAction?.follow)
            menuBlock?.isVisible = viewModel.appData?.title != null
            viewModel.appData?.title?.let {
                viewModel.checkApp(it)
            }
            menuSearch?.isVisible = !viewModel.tabList.isNullOrEmpty()

            setOnMenuItemClickListener {
                when (it.itemId) {
                    android.R.id.home -> activity?.finish()

                    R.id.search -> {
                        IntentUtil.startActivity<SearchActivity>(requireContext()) {
                            putExtra("pageType", "apk")
                            putExtra("pageParam", viewModel.appId)
                            putExtra("title", viewModel.appData?.title)
                        }
                    }

                    R.id.subscribe -> {
                        /*viewModel.onGetFollow(
                            if (viewModel.appData?.userAction?.follow == 1) "/v6/apk/unFollow"
                            else "/v6/apk/follow"
                        )*/
                    }

                    R.id.block -> {
                        viewModel.appData?.title?.let {
                            val isBlocked = menuBlock?.title.toString() == "移除黑名单"
                            MaterialAlertDialogBuilder(requireContext()).apply {
                                setTitle("确定将 $it ${menuBlock?.title}？")
                                setNegativeButton(android.R.string.cancel, null)
                                setPositiveButton(android.R.string.ok) { _, _ ->
                                    menuBlock?.title = if (isBlocked) {
                                        viewModel.deleteTopic(it)
                                        "加入黑名单"
                                    } else {
                                        viewModel.saveTopic(it)
                                        "移除黑名单"
                                    }
                                }
                                show()
                            }
                        }
                    }
                }
                return@setOnMenuItemClickListener true
            }
        }
    }

    private fun updateFollow(follow: Int?) {
        menuSubscribe?.isVisible = PrefManager.isLogin
                && viewModel.appData?.entityType == "apk"
        menuSubscribe?.title = if (follow == 1) "取消关注"
        else "关注"
    }

    private fun initAppBar() {
        binding.appBar.addOnOffsetChangedListener(object : AppBarLayoutStateChangeListener() {
            override fun onStateChanged(appBarLayout: AppBarLayout?, state: State?) {
                when (state) {
                    State.COLLAPSED -> appBinding.appLayout.visibility = View.INVISIBLE
                    State.EXPANDED, State.INTERMEDIATE ->
                        appBinding.appLayout.isVisible = true

                    else -> appBinding.appLayout.visibility = View.INVISIBLE
                }
            }

        })
    }

    private fun initApp() {
        val replaceViewHelper = ReplaceViewHelper(requireContext())
        appBinding = BaseViewAppBinding.inflate(layoutInflater, null, false)
        replaceViewHelper.toReplaceView(binding.view, appBinding.root)
        appBinding.appData = viewModel.appData
    }

    override fun iOnTabSelected(tab: TabLayout.Tab?) {}

    override fun getFragment(
        position: Int,
    ): Fragment =
        AppContentFragment.newInstance(
            typeList[position],
            viewModel.appId ?: "4599"
        )

    override fun initTabList() {
        tabList = viewModel.tabList ?: emptyList()
    }

    override fun onBackClick() {
        activity?.finish()
    }

}