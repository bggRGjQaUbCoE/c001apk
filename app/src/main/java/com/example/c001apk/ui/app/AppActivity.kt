package com.example.c001apk.ui.app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.c001apk.R
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.databinding.ActivityAppBinding
import com.example.c001apk.ui.base.BaseActivity
import com.example.c001apk.ui.home.IOnTabClickContainer
import com.example.c001apk.ui.home.IOnTabClickListener
import com.example.c001apk.ui.search.SearchActivity
import com.example.c001apk.util.ClipboardUtil
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.Utils.downloadApk
import com.example.c001apk.view.AppBarLayoutStateChangeListener
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback

@AndroidEntryPoint
class AppActivity : BaseActivity<ActivityAppBinding>(), IOnTabClickContainer {

    private val viewModel by viewModels<AppActivityViewModel>(
        extrasProducer = {
            defaultViewModelCreationExtras.withCreationCallback<AppActivityViewModel.Factory> { factory ->
                factory.create(id = intent.getStringExtra("id") ?: "com.coolapk.market")
            }
        }
    )
    override var tabController: IOnTabClickListener? = null
    private var menuSubscribe: MenuItem? = null
    private var menuBlock: MenuItem? = null
    private var menuSearch: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.appBar.setLiftable(true)

        initData()
        initObserve()
        initError()

    }

    private fun initError() {
        binding.errorLayout.retry.setOnClickListener {
            binding.errorLayout.parent.isVisible = false
            viewModel.loadingState.value = LoadingState.Loading
        }
    }

    private fun initObserve() {
        viewModel.searchState.observe(this) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                menuSearch?.isVisible = true
            }
        }

        viewModel.toastText.observe(this) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                Toast.makeText(
                    this,
                    if (it == 1) "关注成功" else "取消关注成功",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        viewModel.followState.observe(this) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                updateFollow(it)
            }
        }

        viewModel.blockState.observe(this) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                menuBlock?.title = if (it) "移除黑名单"
                else "加入黑名单"
                menuBlock?.isVisible = true
            }
        }

        viewModel.loadingState.observe(this) {
            when (it) {
                LoadingState.Loading -> {
                    binding.indicator.parent.isIndeterminate = true
                    binding.indicator.parent.isVisible = true
                    viewModel.fetchAppInfo()
                }

                LoadingState.LoadingDone -> {
                    binding.appData = viewModel.appData
                    binding.appLayout.isVisible = true
                    initAppBar()
                    initDownBtn()
                    binding.tabLayout.isVisible =
                        if (!viewModel.tabList.isNullOrEmpty()) {
                            initView()
                            true
                        } else {
                            binding.errorMessage.errMsg.text = viewModel.errMsg
                            binding.errorMessage.errMsg.isVisible = true
                            false
                        }
                }

                is LoadingState.LoadingError -> {
                    binding.appLayout.isVisible = false
                    binding.tabLayout.isVisible = false
                    binding.errorMessage.errMsg.text = it.errMsg
                    binding.errorMessage.errMsg.isVisible = true
                }

                is LoadingState.LoadingFailed -> {
                    binding.appLayout.isVisible = false
                    binding.tabLayout.isVisible = false
                    binding.errorLayout.msg.text = it.msg
                    binding.errorLayout.parent.isVisible = true
                }
            }
            if (it !is LoadingState.Loading) {
                binding.indicator.parent.isIndeterminate = false
                binding.indicator.parent.isVisible = false
            }
        }

        viewModel.download.observe(this) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                if (it)
                    downloadApp()
            }
        }

    }

    private fun updateFollow(it: Int?) {
        menuSubscribe?.isVisible = PrefManager.isLogin
                && viewModel.appData?.entityType == "apk"
        menuSubscribe?.title = if (it == 1) "取消关注"
        else "关注"
    }

    private fun initAppBar() {
        binding.appBar.addOnOffsetChangedListener(object : AppBarLayoutStateChangeListener() {
            override fun onStateChanged(appBarLayout: AppBarLayout?, state: State?) {
                when (state) {
                    State.COLLAPSED -> binding.appLayout.visibility = View.INVISIBLE
                    State.EXPANDED, State.INTERMEDIATE ->
                        binding.appLayout.isVisible = true

                    else -> binding.appLayout.visibility = View.INVISIBLE
                }
            }

        })
    }

    private fun initDownBtn() {
        if (viewModel.appData?.entityType == "apk") {
            binding.btnDownload.apply {
                isVisible = true
                setOnClickListener {
                    if (viewModel.downloadUrl.isNullOrEmpty()) {
                        viewModel.onGetDownloadLink()
                    } else {
                        downloadApp()
                    }
                }
            }
        }
    }

    private fun initView() {
        binding.viewPager.offscreenPageLimit = viewModel.tabList?.size ?: 0
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int) =
                when (position) {
                    0 -> AppFragment.newInstance("reply", viewModel.appId.toString())
                    1 -> AppFragment.newInstance("pub", viewModel.appId.toString())
                    2 -> AppFragment.newInstance("hot", viewModel.appId.toString())
                    else -> throw IllegalArgumentException()
                }

            override fun getItemCount() = viewModel.tabList?.size ?: 0
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = viewModel.tabList?.getOrElse(position) { "null" }
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {
                tabController?.onReturnTop(null)
            }

        })
    }

    private fun downloadApp() {
        try {
            downloadApk(
                this,
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
                Toast.makeText(this@AppActivity, "下载失败", Toast.LENGTH_SHORT).show()
                ClipboardUtil.copyText(this, viewModel.downloadUrl.toString())
                e.printStackTrace()
            }
        }
    }

    private fun initData() {
        if (viewModel.isInit) {
            viewModel.isInit = false
            viewModel.loadingState.value = LoadingState.Loading
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.topic_product_menu, menu)
        menu?.findItem(R.id.order)?.isVisible = false

        menuSubscribe = menu?.findItem(R.id.subscribe)
        menuBlock = menu?.findItem(R.id.block)
        menuSearch = menu?.findItem(R.id.search)

        updateFollow(viewModel.appData?.userAction?.follow)
        menuBlock?.isVisible = viewModel.appData?.title != null
        viewModel.appData?.title?.let {
            viewModel.checkApp(it)
        }
        menuSearch?.isVisible = !viewModel.tabList.isNullOrEmpty()

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()

            R.id.search -> {
                IntentUtil.startActivity<SearchActivity>(this) {
                    putExtra("pageType", "apk")
                    putExtra("pageParam", viewModel.appId)
                    putExtra("title", viewModel.appData?.title)
                }
            }

            R.id.subscribe -> {
                viewModel.onGetFollow(
                    if (viewModel.appData?.userAction?.follow == 1) "/v6/apk/unFollow"
                    else "/v6/apk/follow"
                )
            }

            R.id.block -> {
                viewModel.appData?.title?.let {
                    val isBlocked = menuBlock?.title.toString() == "移除黑名单"
                    MaterialAlertDialogBuilder(this).apply {
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
        return true
    }

}