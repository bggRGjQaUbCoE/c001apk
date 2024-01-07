package com.example.c001apk.ui.fragment.topic

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.c001apk.R
import com.example.c001apk.databinding.FragmentTopicBinding
import com.example.c001apk.logic.database.TopicBlackListDatabase
import com.example.c001apk.logic.model.SearchHistory
import com.example.c001apk.ui.activity.SearchActivity
import com.example.c001apk.ui.activity.TopicActivity
import com.example.c001apk.ui.fragment.BaseFragment
import com.example.c001apk.ui.fragment.minterface.IOnSearchMenuClickContainer
import com.example.c001apk.ui.fragment.minterface.IOnSearchMenuClickListener
import com.example.c001apk.ui.fragment.minterface.IOnTabClickContainer
import com.example.c001apk.ui.fragment.minterface.IOnTabClickListener
import com.example.c001apk.util.PrefManager
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TopicFragment : BaseFragment<FragmentTopicBinding>(), IOnSearchMenuClickContainer,
    IOnTabClickContainer {

    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    override var controller: IOnSearchMenuClickListener? = null
    override var tabController: IOnTabClickListener? = null
    private val topicBlackListDao by lazy {
        TopicBlackListDatabase.getDatabase(requireContext()).blackListDao()
    }
    private lateinit var subscribe: MenuItem

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
        setHasOptionsMenu(true)
        arguments?.let {
            viewModel.url = it.getString("url")
            viewModel.title = it.getString("title")
            viewModel.id = it.getString("id")
            viewModel.type = it.getString("type")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.isResume) {
            viewModel.isResume = false
            getViewData()
        } else if (!viewModel.isResume && viewModel.tabList.isEmpty()) {
            binding.errorLayout.parent.visibility = View.VISIBLE
        } else {
            initView(null)
            initBar()
            initViewPagerMenu()
        }

        binding.errorLayout.retry.setOnClickListener {
            binding.errorLayout.parent.visibility = View.GONE
            getViewData()
        }

        viewModel.topicLayoutLiveData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val data = result.getOrNull()
                if (data?.data != null) {
                    viewModel.isFollow = data.data.userAction?.follow == 1
                    if (viewModel.tabList.isEmpty()) {
                        viewModel.id = data.data.id
                        viewModel.type = data.data.entityType
                        viewModel.subtitle = data.data.intro
                        initBar()

                        for (element in data.data.tabList) {
                            viewModel.tabList.add(element.title)
                            viewModel.fragmentList.add(
                                TopicContentFragment.newInstance(
                                    element.url,
                                    element.title,
                                    true
                                )
                            )
                        }
                        var tabSelected = 0
                        for (element in data.data.tabList) {
                            if (data.data.selectedTab == element.pageName) break
                            else tabSelected++
                        }
                        initView(tabSelected)
                    }
                    binding.indicator.parent.isIndeterminate = false
                    binding.indicator.parent.visibility = View.GONE
                } else {
                    binding.indicator.parent.isIndeterminate = false
                    binding.indicator.parent.visibility = View.GONE
                    binding.errorLayout.parent.visibility = View.VISIBLE
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

        viewModel.productLayoutLiveData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val data = result.getOrNull()
                if (data?.data != null) {
                    viewModel.isFollow = data.data.userAction?.follow == 1
                    if (viewModel.tabList.isEmpty()) {
                        viewModel.subtitle = data.data.intro
                        initBar()

                        for (element in data.data.tabList) {
                            viewModel.tabList.add(element.title)
                            viewModel.fragmentList.add(
                                TopicContentFragment.newInstance(
                                    element.url,
                                    element.title,
                                    true
                                )
                            )
                        }
                        var tabSelected = 0
                        for (element in data.data.tabList) {
                            if (data.data.selectedTab == element.pageName) break
                            else tabSelected++
                        }
                        initView(tabSelected)
                    }
                    binding.indicator.parent.isIndeterminate = false
                    binding.indicator.parent.visibility = View.GONE
                } else {
                    binding.indicator.parent.isIndeterminate = false
                    binding.indicator.parent.visibility = View.GONE
                    binding.errorLayout.parent.visibility = View.VISIBLE
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

        viewModel.getFollowData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val response = result.getOrNull()
                if (response != null) {
                    if (!response.message.isNullOrEmpty()) {
                        if (response.message.contains("关注成功")) {
                            Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT)
                                .show()
                            viewModel.isFollow = !viewModel.isFollow
                            initSub()
                        } else
                            Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT)
                                .show()
                    }
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

        viewModel.postFollowData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val response = result.getOrNull()
                if (response != null) {
                    if (!response.message.isNullOrEmpty()) {
                        if (response.message.contains("手机吧成功")) {
                            Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT)
                                .show()
                            viewModel.isFollow = !viewModel.isFollow
                            initSub()
                        } else
                            Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT)
                                .show()
                    }
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

    }

    private fun initSub() {
        subscribe.title = if (viewModel.isFollow) "取消关注"
        else "关注"
    }

    private fun initBar() {
        binding.toolBar.apply {
            title = if (viewModel.type == "topic") viewModel.url.toString().replace("/t/", "")
            else viewModel.title
            viewModel.subtitle?.let { subtitle = viewModel.subtitle }
        }
        (activity as TopicActivity).setSupportActionBar(binding.toolBar)
        (activity as TopicActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initView(tabSelected: Int?) {
        binding.viewPager.offscreenPageLimit = viewModel.tabList.size
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int) = viewModel.fragmentList[position]
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
            override fun onTabSelected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {
                tabController?.onReturnTop(null)
            }

        })
    }

    private fun getViewData() {
        binding.indicator.parent.visibility = View.VISIBLE
        binding.indicator.parent.isIndeterminate = true
        viewModel.isNew = true
        initViewPagerMenu()
        if (viewModel.type == "topic") {
            viewModel.url = viewModel.url.toString().replace("/t/", "")
            viewModel.getTopicLayout()
        } else if (viewModel.type == "product") {
            viewModel.getProductLayout()
        }
    }

    private fun initViewPagerMenu() {
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageSelected(position: Int) {}
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                requireActivity().invalidateOptionsMenu()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.topic_product_menu, menu)
        if (viewModel.type == "product")
            menu.findItem(R.id.order).isVisible = binding.viewPager.currentItem == 1
        else
            menu.findItem(R.id.order).isVisible = false
        subscribe = menu.findItem(R.id.subscribe)
        subscribe.isVisible = PrefManager.isLogin
    }

    override fun onPrepareOptionsMenu(menu: Menu) {

        initSub()

        //if (viewModel.type == "product") {
        menu.findItem(
            when (viewModel.productTitle) {
                "最近回复" -> R.id.topicLatestReply
                "热度排序" -> R.id.topicHot
                "最新发布" -> R.id.topicLatestPublish
                else -> throw IllegalArgumentException("type error")
            }
        )?.isChecked = true
        // }

        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> requireActivity().finish()

            R.id.search -> {
                val intent = Intent(activity, SearchActivity::class.java)
                if (viewModel.type == "topic") {
                    intent.putExtra("pageType", "tag")
                    intent.putExtra("pageParam", viewModel.url.toString().replace("/t/", ""))
                    intent.putExtra("title", viewModel.url.toString().replace("/t/", ""))
                } else {
                    intent.putExtra("pageType", "product_phone")
                    intent.putExtra("pageParam", viewModel.id)
                    intent.putExtra("title", viewModel.title)
                }
                requireActivity().startActivity(intent)
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
                MaterialAlertDialogBuilder(requireContext()).apply {
                    val title =
                        if (viewModel.type == "topic") viewModel.url.toString().replace("/t/", "")
                        else viewModel.title
                    setTitle("确定将 $title 加入黑名单？")
                    setNegativeButton(android.R.string.cancel, null)
                    setPositiveButton(android.R.string.ok) { _, _ ->
                        CoroutineScope(Dispatchers.IO).launch {
                            if (!topicBlackListDao.isExist(viewModel.title.toString())) {
                                topicBlackListDao.insert(SearchHistory(viewModel.title.toString()))
                            }
                        }
                    }
                    show()
                }
            }

            R.id.subscribe -> {
                when (viewModel.type) {
                    "topic" -> {
                        viewModel.isNew = true
                        viewModel.followUrl = if (viewModel.isFollow) "/v6/feed/unFollowTag"
                        else "/v6/feed/followTag"
                        viewModel.tag = viewModel.url.toString().replace("/t/", "")
                        viewModel.getFollow()
                    }

                    "product" -> {
                        viewModel.isNew = true
                        viewModel.postFollow["id"] = viewModel.id.toString()
                        viewModel.postFollow["status"] = if (viewModel.isFollow) "0"
                        else "1"
                        viewModel.postFollow()
                    }

                    else -> Toast.makeText(
                        requireContext(),
                        "type error: ${viewModel.type}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }
        return super.onOptionsItemSelected(item)
    }

}