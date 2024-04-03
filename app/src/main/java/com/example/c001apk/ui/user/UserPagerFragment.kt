package com.example.c001apk.ui.user

import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.c001apk.R
import com.example.c001apk.databinding.BaseUserPageBinding
import com.example.c001apk.databinding.BaseViewUserBinding
import com.example.c001apk.ui.base.BaseFragment
import com.example.c001apk.ui.others.WebViewActivity
import com.example.c001apk.ui.search.SearchActivity
import com.example.c001apk.util.DateUtils
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.ReplaceViewHelper
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserPagerFragment : BaseFragment<BaseUserPageBinding>() {

    private val viewModel by viewModels<UserViewModel>(ownerProducer = { requireActivity() })
    private lateinit var userBinding: BaseViewUserBinding
    private var menuBlock: MenuItem? = null
    private var menuFollow: MenuItem? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBar()
        initPage()
        initUser()
        initObserve()
    }

    private fun initPage() {
        if (childFragmentManager.findFragmentById(R.id.fragmentContainer) == null) {
            childFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, UserFragment())
                .commit()
        }
    }

    private fun initObserve() {
        viewModel.blockState.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                menuBlock?.title = getMenuTitle(
                    if (it) "移除黑名单"
                    else "加入黑名单"
                )

            }
        }

        viewModel.followState.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                menuFollow?.title = getMenuTitle(
                    if (it == 1) "取消关注"
                    else "关注"
                )
            }
        }

        viewModel.toastText.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initUser() {
        val replaceViewHelper = ReplaceViewHelper(requireContext())
        userBinding = BaseViewUserBinding.inflate(layoutInflater, null, false)
        replaceViewHelper.toReplaceView(binding.view, userBinding.root)
        userBinding.userData = viewModel.userData
        userBinding.listener = viewModel.ItemClickListener()
    }

    private fun getMenuTitle(title: CharSequence?): SpannableString {
        return SpannableString(title).also {
            it.setSpan(
                ForegroundColorSpan(
                    MaterialColors.getColor(
                        requireContext(),
                        com.google.android.material.R.attr.colorControlNormal,
                        0
                    )
                ),
                0, title?.length ?: 0, 0
            )
        }
    }

    private fun initBar() {
        binding.collapsingToolbar.title = viewModel.userData?.username
        binding.toolBar.apply {
            setNavigationIcon(R.drawable.ic_back)
            setNavigationOnClickListener {
                activity?.finish()
            }

            inflateMenu(R.menu.user_menu)
            menuBlock = menu?.findItem(R.id.block)
            menuBlock?.title = getMenuTitle(menuBlock?.title)

            menuFollow = menu?.findItem(R.id.subscribe)
            menuFollow?.title = getMenuTitle(menuFollow?.title)
            menuFollow?.isVisible = PrefManager.isLogin

            val menuShare = menu?.findItem(R.id.share)
            menuShare?.title = getMenuTitle(menuShare?.title)

            val menuReport = menu?.findItem(R.id.report)
            menuReport?.title = getMenuTitle(menuReport?.title)
            menuReport?.isVisible = PrefManager.isLogin

            menu?.findItem(R.id.check)?.title = getMenuTitle(menu?.findItem(R.id.check)?.title)

            viewModel.checkMenuState()

            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.check -> {
                        val data = viewModel.userData
                        MaterialAlertDialogBuilder(requireContext()).apply {
                            setTitle(data?.username)
                            setMessage(
                                """
                                uid: ${data?.uid}
                                
                                等级: Lv.${data?.level}
                                
                                性别: ${if (data?.gender == 0) "女" else if (data?.gender == 1) "男" else "未知"}
                                
                                注册时长: ${((System.currentTimeMillis() / 1000 - (data?.regdate ?: 0)) / 24 / 3600)} 天
                                
                                注册时间: ${DateUtils.timeStamp2Date(data?.regdate ?: 0)}
                            """.trimIndent()
                            )
                            show()
                        }
                    }

                    R.id.subscribe -> {
                        viewModel.onPostFollowUnFollow(
                            if (viewModel.userData?.isFollow == 1)
                                "/v6/user/unfollow"
                            else
                                "/v6/user/follow"
                        )
                    }

                    R.id.search -> {
                        IntentUtil.startActivity<SearchActivity>(requireContext()) {
                            putExtra("pageType", "user")
                            putExtra("pageParam", viewModel.uid)
                            putExtra("title", viewModel.userData?.username)
                        }
                    }

                    R.id.block -> {
                        val isBlocked = menuBlock?.title.toString() == "移除黑名单"
                        MaterialAlertDialogBuilder(requireContext()).apply {
                            setTitle("确定将 ${viewModel.userData?.username} ${menuBlock?.title}？")
                            setNegativeButton(android.R.string.cancel, null)
                            setPositiveButton(android.R.string.ok) { _, _ ->
                                viewModel.uid.let { uid ->
                                    menuBlock?.title = if (isBlocked) {
                                        viewModel.deleteUid(uid)
                                        getMenuTitle("加入黑名单")
                                    } else {
                                        viewModel.saveUid(uid)
                                        getMenuTitle("移除黑名单")
                                    }
                                }
                            }
                            show()
                        }
                    }

                    R.id.share -> {
                        IntentUtil.shareText(
                            requireContext(),
                            "https://www.coolapk1s.com/u/${viewModel.uid}"
                        )
                    }

                    R.id.report -> {
                        IntentUtil.startActivity<WebViewActivity>(requireContext()) {
                            putExtra(
                                "url",
                                "https://m.coolapk.com/mp/do?c=user&m=report&id=${viewModel.uid}"
                            )
                        }
                    }
                }
                return@setOnMenuItemClickListener true
            }
        }
    }
}