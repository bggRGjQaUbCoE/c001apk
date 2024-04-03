package com.example.c001apk.ui.app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.c001apk.R
import com.example.c001apk.databinding.BaseViewAppBinding
import com.example.c001apk.ui.base.BasePagerFragment
import com.example.c001apk.ui.search.SearchActivity
import com.example.c001apk.util.ClipboardUtil
import com.example.c001apk.util.ImageUtil
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.ReplaceViewHelper
import com.example.c001apk.util.Utils.downloadApk
import com.example.c001apk.view.AppBarLayoutStateChangeListener
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AppFragment : BasePagerFragment() {

    private val viewModel by viewModels<AppViewModel>(ownerProducer = { requireActivity() })
    private val typeList = listOf("reply", "pub", "hot")
    private lateinit var appBinding: BaseViewAppBinding
    private var menuSubscribe: MenuItem? = null
    private var menuBlock: MenuItem? = null
    private var menuSearch: MenuItem? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.appBar.setLiftable(true)
        initBar()
        initApp()
        initAppBar()
        if (!viewModel.tabList.isNullOrEmpty()) {
            initTabList()
            initView()
        } else if (!viewModel.errMsg.isNullOrEmpty()) {
            binding.tabLayout.isVisible = false
            binding.errorMessage.errMsg.apply {
                text = viewModel.errMsg
                isVisible = true
            }
        }
        initObserve()
    }

    private fun initObserve() {
        viewModel.download.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                if (it)
                    onDownload()
            }
        }

        viewModel.searchState.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                menuSearch?.isVisible = !viewModel.tabList.isNullOrEmpty()
            }
        }

        viewModel.followState.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                menuSubscribe?.isVisible = PrefManager.isLogin
                        && viewModel.appData?.entityType == "apk"
                menuSubscribe?.title = if (it == 1) "取消关注"
                else "关注"
            }
        }

        viewModel.blockState.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                menuBlock?.title = if (it) "移除黑名单"
                else "加入黑名单"
                menuBlock?.isVisible = true
            }
        }

        viewModel.toastText.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let { text ->
                Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun initBar() {
        super.initBar()
        binding.collapsingToolbar.title = viewModel.appData?.title

        binding.toolBar.apply {
            inflateMenu(R.menu.topic_product_menu)
            menu?.findItem(R.id.order)?.isVisible = false

            menuSubscribe = menu?.findItem(R.id.subscribe)
            menuBlock = menu?.findItem(R.id.block)
            menuSearch = menu?.findItem(R.id.search)

            menuSearch?.isVisible = false
            menuSubscribe?.isVisible = false
            menuBlock?.isVisible = false
            viewModel.checkMenuState()

            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.search -> {
                        IntentUtil.startActivity<SearchActivity>(requireContext()) {
                            putExtra("pageType", "apk")
                            putExtra("pageParam", viewModel.appId)
                            putExtra("title", viewModel.appData?.title)
                        }
                    }

                    R.id.subscribe -> {
                        viewModel.onGetFollowApk(
                            if (viewModel.appData?.userAction?.follow == 1) "/v6/apk/unFollow"
                            else "/v6/apk/follow",
                            null,
                            viewModel.appId
                        )
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
        if (viewModel.appData?.entityType == "apk") {
            appBinding.btnDownload.apply {
                isVisible = true
                setOnClickListener {
                    if (viewModel.downloadUrl.isNullOrEmpty())
                        viewModel.onGetDownloadLink()
                    else
                        onDownload()
                }
            }
        }
        viewModel.appData?.changelog?.let { changelog ->
            appBinding.version.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setTitle("更新日志")
                    setMessage(changelog)
                    setPositiveButton(android.R.string.ok, null)
                }.show()
            }
        }
        viewModel.appData?.logo?.let { logo ->
            appBinding.logo.setOnClickListener {
                ImageUtil.startBigImgViewSimple(it as ImageView, logo)
            }
        }
    }

    private fun onDownload() {
        try {
            downloadApk(
                requireContext(),
                viewModel.downloadUrl.toString(),
                "${viewModel.appData?.title}-${viewModel.appData?.apkversionname}-${viewModel.appData?.apkversioncode}.apk"
            )
        } catch (e: Exception) {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(viewModel.downloadUrl.toString())
                    )
                )
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(requireContext(), "下载失败", Toast.LENGTH_SHORT).show()
                ClipboardUtil.copyText(requireContext(), viewModel.downloadUrl.toString())
                e.printStackTrace()
            }
        }
    }

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