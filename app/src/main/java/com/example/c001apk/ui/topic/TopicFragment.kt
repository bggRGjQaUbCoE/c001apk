package com.example.c001apk.ui.topic

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.c001apk.R
import com.example.c001apk.ui.base.BasePagerFragment
import com.example.c001apk.ui.home.IOnTabClickListener
import com.example.c001apk.ui.search.IOnSearchMenuClickContainer
import com.example.c001apk.ui.search.IOnSearchMenuClickListener
import com.example.c001apk.ui.search.SearchActivity
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.PrefManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.GRAVITY_CENTER
import com.google.android.material.tabs.TabLayout.MODE_SCROLLABLE
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TopicFragment : BasePagerFragment(), IOnSearchMenuClickContainer {

    private val viewModel by viewModels<TopicViewModel>(ownerProducer = { requireActivity() })
    override var tabController: IOnTabClickListener? = null
    private lateinit var subscribe: MenuItem
    private lateinit var order: MenuItem
    private var menuBlock: MenuItem? = null
    override var controller: IOnSearchMenuClickListener? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSelected()
        initObserve()
    }

    private fun initSelected() {
        viewModel.tabSelected?.let {
            binding.viewPager.setCurrentItem(it, false)
            viewModel.tabSelected = null
        }
    }

    private fun initObserve() {
        viewModel.blockState.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandledOrReturnNull()?.let {
                menuBlock?.title = if (it) "移除黑名单"
                else "加入黑名单"
            }
        }

        viewModel.followState.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                subscribe.title = if (it) "取消关注"
                else "关注"
            }
        }

        viewModel.toastText.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun iOnTabSelected(tab: TabLayout.Tab?) {
        order.isVisible = tab?.position == tabList.indexOf("讨论")
    }

    override fun getFragment(position: Int): Fragment =
        TopicContentFragment.newInstance(
            viewModel.topicList[position].url,
            viewModel.topicList[position].title,
        )

    override fun initTabList() {
        binding.tabLayout.apply {
            tabGravity = GRAVITY_CENTER
            tabMode = MODE_SCROLLABLE
        }
        tabList = viewModel.topicList.map { it.title }
    }

    override fun onBackClick() {
        activity?.finish()
    }

    override fun initBar() {
        super.initBar()
        binding.collapsingToolbar.isTitleEnabled = false
        binding.toolBar.apply {
            title = if (viewModel.type == "topic") viewModel.url.replace("/t/", "")
            else viewModel.title
            viewModel.subtitle?.let { subtitle = it }

            inflateMenu(R.menu.topic_product_menu)

            order = menu.findItem(R.id.order)
            order.isVisible = viewModel.type == "product"
                    && binding.viewPager.currentItem == tabList.indexOf("讨论")
            menu.findItem(
                when (viewModel.productTitle) {
                    "最近回复" -> R.id.topicLatestReply
                    "热度排序" -> R.id.topicHot
                    "最新发布" -> R.id.topicLatestPublish
                    else -> throw IllegalArgumentException("type error")
                }
            )?.isChecked = true

            menuBlock = menu.findItem(R.id.block)
            subscribe = menu.findItem(R.id.subscribe)
            subscribe.isVisible = PrefManager.isLogin

            viewModel.checkMenuState()

            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.search -> {
                        if (viewModel.type == "topic") {
                            IntentUtil.startActivity<SearchActivity>(requireContext()) {
                                putExtra("type", "topic")
                                putExtra("pageType", "tag")
                                putExtra("pageParam", viewModel.url.replace("/t/", ""))
                                putExtra("title", viewModel.url.replace("/t/", ""))
                            }
                        } else {
                            IntentUtil.startActivity<SearchActivity>(requireContext()) {
                                putExtra("type", "topic")
                                putExtra("pageType", "product_phone")
                                putExtra("pageParam", viewModel.id)
                                putExtra("title", viewModel.title)
                            }
                        }
                    }

                    R.id.topicLatestReply -> {
                        viewModel.productTitle = "最近回复"
                        controller?.onSearch("title", "最近回复", viewModel.id)
                    }

                    R.id.topicHot -> {
                        viewModel.productTitle = "热度排序"
                        controller?.onSearch("title", "热度排序", viewModel.id)
                    }

                    R.id.topicLatestPublish -> {
                        viewModel.productTitle = "最新发布"
                        controller?.onSearch("title", "最新发布", viewModel.id)
                    }

                    R.id.block -> {
                        val isBlocked = menuBlock?.title.toString() == "移除黑名单"
                        MaterialAlertDialogBuilder(requireContext()).apply {
                            val title =
                                if (viewModel.type == "topic") viewModel.url
                                    .replace("/t/", "")
                                else viewModel.title
                            setTitle("确定将 $title ${menuBlock?.title}？")
                            setNegativeButton(android.R.string.cancel, null)
                            setPositiveButton(android.R.string.ok) { _, _ ->
                                viewModel.title.let { title ->
                                    menuBlock?.title = if (isBlocked) {
                                        viewModel.deleteTopic(title)
                                        "加入黑名单"
                                    } else {
                                        viewModel.saveTopic(title)
                                        "移除黑名单"
                                    }
                                }
                            }
                            show()
                        }
                    }

                    R.id.subscribe -> {
                        when (viewModel.type) {
                            "topic" -> {
                                val followUrl =
                                    if (viewModel.isFollow) "/v6/feed/unFollowTag"
                                    else "/v6/feed/followTag"
                                val tag = viewModel.url.replace("/t/", "")
                                viewModel.onGetFollow(followUrl, tag, null)
                            }

                            "product" -> {
                                if (viewModel.postFollowData.isNullOrEmpty())
                                    viewModel.postFollowData = HashMap()
                                viewModel.postFollowData?.let { map ->
                                    map["id"] = viewModel.id
                                    map["status"] =
                                        if (viewModel.isFollow) "0"
                                        else "1"
                                }
                                viewModel.onPostFollow()
                            }

                            else -> Toast.makeText(
                                requireContext(),
                                "type error: ${viewModel.type}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                }
                menu.findItem(
                    when (viewModel.productTitle) {
                        "最近回复" -> R.id.topicLatestReply
                        "热度排序" -> R.id.topicHot
                        "最新发布" -> R.id.topicLatestPublish
                        else -> throw IllegalArgumentException("type error")
                    }
                )?.isChecked = true
                return@setOnMenuItemClickListener true
            }
        }
    }
}