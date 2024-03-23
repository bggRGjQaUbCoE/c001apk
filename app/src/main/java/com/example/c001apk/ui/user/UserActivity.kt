package com.example.c001apk.ui.user

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.example.c001apk.R
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.constant.Constants
import com.example.c001apk.databinding.ActivityUserBinding
import com.example.c001apk.ui.base.BaseActivity
import com.example.c001apk.ui.others.WebViewActivity
import com.example.c001apk.ui.search.SearchActivity
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.PrefManager
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback


@AndroidEntryPoint
class UserActivity : BaseActivity<ActivityUserBinding>() {

    private val viewModel by viewModels<UserViewModel>(
        extrasProducer = {
            defaultViewModelCreationExtras.withCreationCallback<UserViewModel.Factory> { factory ->
                factory.create(uid = intent.getStringExtra("id").orEmpty())
            }
        }
    )
    private var menuBlock: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        initData()
        initFollowBtn()
        initObserve()

        binding.errorLayout.retry.setOnClickListener {
            binding.errorLayout.parent.isVisible = false
            viewModel.activityState.value = LoadingState.Loading
        }

    }

    private fun initData() {
        if (viewModel.isAInit) {
            viewModel.isAInit = false
            viewModel.activityState.value = LoadingState.Loading
        }
    }

    private fun initFollowBtn() {
        binding.followBtn.apply {
            isVisible = PrefManager.isLogin
            setOnClickListener {
                if (PrefManager.isLogin)
                    viewModel.onPostFollowUnFollow(
                        if (viewModel.userData?.isFollow == 1)
                            "/v6/user/unfollow"
                        else
                            "/v6/user/follow"
                    )
            }
        }
    }

    private fun initObserve() {
        viewModel.blockState.observe(this) { event ->
            event?.getContentIfNotHandledOrReturnNull()?.let {
                if (it)
                    menuBlock?.title = getMenuTitle("移除黑名单")
            }
        }

        viewModel.toastText.observe(this) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        /*viewModel.followState.observe(this) {
            binding.followBtn.text = if (it == 1) "取消关注"
            else "关注"
        }*/

        viewModel.activityState.observe(this) {
            when (it) {
                LoadingState.Loading -> {
                    binding.indicator.parent.isIndeterminate = true
                    binding.indicator.parent.isVisible = true
                    viewModel.fetchUser()
                }

                LoadingState.LoadingDone -> {
                    binding.userData = viewModel.userData
                    binding.listener = viewModel.ItemClickListener()
                    binding.infoLayout.isVisible = true
                    beginTransaction()
                }

                is LoadingState.LoadingError -> {
                    binding.errorMessage.errMsg.apply {
                        text = it.errMsg
                        isVisible = true
                    }
                }

                is LoadingState.LoadingFailed -> {
                    binding.errorLayout.apply {
                        msg.text = it.msg
                        retry.text =
                            if (it.msg == Constants.LOADING_EMPTY) getString(R.string.refresh)
                            else getString(R.string.retry)
                        parent.isVisible = true
                    }
                }
            }
            if (it !is LoadingState.Loading) {
                binding.indicator.parent.isIndeterminate = false
                binding.indicator.parent.isVisible = false
            }
        }

    }

    @SuppressLint("CommitTransaction")
    private fun beginTransaction() {
        if (supportFragmentManager.findFragmentById(R.id.fragmentContainer) == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragmentContainer, UserFragment()
                )
                .commit()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.user_menu, menu)

        menuBlock = menu?.findItem(R.id.block)
        menuBlock?.title = getMenuTitle(menuBlock?.title)
        viewModel.checkUid(viewModel.uid)

        val menuShare = menu?.findItem(R.id.share)
        menuShare?.title = getMenuTitle(menuShare?.title)

        val menuReport = menu?.findItem(R.id.report)
        menuReport?.title = getMenuTitle(menuReport?.title)
        menuReport?.isVisible = PrefManager.isLogin

        return true
    }

    private fun getMenuTitle(title: CharSequence?): SpannableString {
        return SpannableString(title).also {
            it.setSpan(
                ForegroundColorSpan(
                    MaterialColors.getColor(
                        this,
                        com.google.android.material.R.attr.colorControlNormal,
                        0
                    )
                ),
                0, title?.length ?: 0, 0
            )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()

            R.id.search -> {
                if (viewModel.userData == null)
                    Toast.makeText(this, "加载中...", Toast.LENGTH_SHORT).show()
                else
                    IntentUtil.startActivity<SearchActivity>(this) {
                        putExtra("pageType", "user")
                        putExtra("pageParam", viewModel.uid)
                        putExtra("title", binding.name.text)
                    }
            }

            R.id.block -> {
                val isBlocked = menuBlock?.title.toString() == "移除黑名单"
                MaterialAlertDialogBuilder(this).apply {
                    setTitle("确定将 ${viewModel.userData?.username} ${menuBlock?.title}？")
                    setNegativeButton(android.R.string.cancel, null)
                    setPositiveButton(android.R.string.ok) { _, _ ->
                        viewModel.uid.let {
                            menuBlock?.title = if (isBlocked) {
                                viewModel.deleteUid(it)
                                getMenuTitle("加入黑名单")
                            } else {
                                viewModel.saveUid(it)
                                getMenuTitle("移除黑名单")
                            }
                        }
                    }
                    show()
                }
            }

            R.id.share -> {
                IntentUtil.shareText(this, "https://www.coolapk.com/u/${viewModel.uid}")
            }

            R.id.report -> {
                IntentUtil.startActivity<WebViewActivity>(this) {
                    putExtra(
                        "url",
                        "https://m.coolapk.com/mp/do?c=user&m=report&id=${viewModel.uid}"
                    )
                }
            }

        }
        return true
    }


}