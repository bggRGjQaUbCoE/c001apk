package com.example.c001apk.ui.topic

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.c001apk.R
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.databinding.FragmentTopicBinding
import com.example.c001apk.ui.base.BaseFragment
import com.example.c001apk.ui.home.IOnTabClickContainer
import com.example.c001apk.ui.home.IOnTabClickListener
import com.example.c001apk.ui.search.IOnSearchMenuClickContainer
import com.example.c001apk.ui.search.IOnSearchMenuClickListener
import com.example.c001apk.ui.search.SearchActivity
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.PrefManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TopicFragment : BaseFragment<FragmentTopicBinding>(), IOnSearchMenuClickContainer,
    IOnTabClickContainer {

    private val viewModel by viewModels<TopicViewModel>()
    override var controller: IOnSearchMenuClickListener? = null
    override var tabController: IOnTabClickListener? = null
    private lateinit var subscribe: MenuItem
    private lateinit var order: MenuItem
    private var menuBlock: MenuItem? = null

    companion object {
        @JvmStatic
        fun newInstance(type: String?, title: String?, url: String?, id: String?) =
            TopicFragment().apply {
                arguments = Bundle().apply {
                    putString("url", url)
                    putString("title", title)
                    putString("id", id)
                    putString("type", type)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.url = it.getString("url")
            viewModel.title = it.getString("title")
            viewModel.id = it.getString("id")
            viewModel.type = it.getString("type")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

    private fun initData() {
        if (viewModel.initData) {
            viewModel.initData = false
            viewModel.loadingState.value = LoadingState.Loading
        }
    }

    private fun initObserve() {
        viewModel.loadingState.observe(viewLifecycleOwner) {
            when (it) {
                LoadingState.Loading -> {
                    binding.indicator.parent.isIndeterminate = true
                    binding.indicator.parent.isVisible = true
                    if (viewModel.type == "topic") {
                        viewModel.url = viewModel.url.toString().replace("/t/", "")
                        viewModel.fetchTopicLayout()
                    } else if (viewModel.type == "product") {
                        viewModel.fetchProductLayout()
                    }
                }

                LoadingState.LoadingDone -> {
                    initView(
                        if (viewModel.isInit) viewModel.tabSelected
                        else null
                    )
                    initBar()
                }

                is LoadingState.LoadingError -> {
                    binding.errorMessage.errMsg.text = it.errMsg
                    binding.errorMessage.errMsg.isVisible = true
                }

                is LoadingState.LoadingFailed -> {
                    binding.errorLayout.msg.text = it.msg
                    binding.errorLayout.parent.isVisible = true
                }
            }
            if (it !is LoadingState.Loading) {
                binding.indicator.parent.isIndeterminate = false
                binding.indicator.parent.isVisible = false
            }
        }

        viewModel.blockState.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandledOrReturnNull()?.let {
                menuBlock?.title = if (it) "移除黑名单"
                else "加入黑名单"
            }
        }

        viewModel.followState.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                Toast.makeText(requireContext(), it.second, Toast.LENGTH_SHORT).show()
                if (it.first) {
                    updateFollowMenu()
                }
            }
        }

    }

    private fun initBar() {
        binding.toolBar.apply {
            title = if (viewModel.type == "topic") viewModel.url.toString().replace("/t/", "")
            else viewModel.title
            viewModel.subtitle?.let { subtitle = viewModel.subtitle }
            setNavigationIcon(R.drawable.ic_back)
            setNavigationOnClickListener {
                requireActivity().finish()
            }

            inflateMenu(R.menu.topic_product_menu)

            order = menu.findItem(R.id.order)
            order.isVisible = viewModel.type == "product"
                    && binding.viewPager.currentItem == viewModel.tabList.indexOf("讨论")
            menu.findItem(
                when (viewModel.productTitle) {
                    "最近回复" -> R.id.topicLatestReply
                    "热度排序" -> R.id.topicHot
                    "最新发布" -> R.id.topicLatestPublish
                    else -> throw IllegalArgumentException("type error")
                }
            )?.isChecked = true

            menuBlock = menu.findItem(R.id.block)
            viewModel.title?.let {
                viewModel.checkTopic(it)
            }

            subscribe = menu.findItem(R.id.subscribe)
            subscribe.isVisible = PrefManager.isLogin
            updateFollowMenu()

            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.search -> {
                        if (viewModel.type == "topic") {
                            IntentUtil.startActivity<SearchActivity>(requireContext()) {
                                putExtra("type", "topic")
                                putExtra("pageType", "tag")
                                putExtra("pageParam", viewModel.url.toString().replace("/t/", ""))
                                putExtra("title", viewModel.url.toString().replace("/t/", ""))
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
                                if (viewModel.type == "topic") viewModel.url.toString()
                                    .replace("/t/", "")
                                else viewModel.title
                            setTitle("确定将 $title ${menuBlock?.title}？")
                            setNegativeButton(android.R.string.cancel, null)
                            setPositiveButton(android.R.string.ok) { _, _ ->
                                viewModel.title?.let { title ->
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
                                val tag = viewModel.url.toString().replace("/t/", "")
                                viewModel.onGetFollow(followUrl, tag)
                            }

                            "product" -> {
                                if (viewModel.postFollowData.isNullOrEmpty())
                                    viewModel.postFollowData = HashMap()
                                viewModel.postFollowData?.let { map ->
                                    map["id"] = viewModel.id.toString()
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

    private fun updateFollowMenu() {
        subscribe.title = if (viewModel.isFollow) "取消关注"
        else "关注"
    }

    private fun initView(tabSelected: Int?) {
        binding.viewPager.offscreenPageLimit = viewModel.tabList.size
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int) =
                TopicContentFragment.newInstance(
                    viewModel.topicList[position].url,
                    viewModel.topicList[position].title,
                )

            override fun getItemCount() = viewModel.tabList.size
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = viewModel.tabList[position]
        }.attach()
        if (viewModel.isInit && tabSelected != null) {
            binding.viewPager.setCurrentItem(tabSelected, false)
            viewModel.isInit = false
        }
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                order.isVisible = tab?.position == viewModel.tabList.indexOf("讨论")
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {
                tabController?.onReturnTop(null)
            }

        })
    }

}