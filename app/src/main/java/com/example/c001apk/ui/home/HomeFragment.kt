package com.example.c001apk.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.c001apk.databinding.FragmentHomeBinding
import com.example.c001apk.ui.applist.AppListFragment
import com.example.c001apk.ui.base.BaseFragment
import com.example.c001apk.ui.homefeed.HomeFeedFragment
import com.example.c001apk.ui.hometopic.HomeTopicFragment
import com.example.c001apk.ui.main.INavViewContainer
import com.example.c001apk.ui.others.CopyActivity
import com.example.c001apk.ui.search.SearchActivity
import com.example.c001apk.util.IntentUtil
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.Tab
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(), IOnTabClickContainer {

    private val viewModel by viewModels<HomeViewModel>()
    override var tabController: IOnTabClickListener? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initButton()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: Tab) {
                viewModel.position = tab.position
            }

            override fun onTabUnselected(tab: Tab?) {}

            override fun onTabReselected(tab: Tab?) {
                if (tab?.text == "关注")
                    tabController?.onReturnTop(false)
                else {
                    tabController?.onReturnTop(true)
                    (activity as? INavViewContainer)?.showNavigationView()
                }
            }
        })

        viewModel.tabListLiveData.observe(viewLifecycleOwner) { tabList ->
            if (tabList.isEmpty()) {
                viewModel.initTab()
            } else {
                val enableList = tabList.filter {
                    it.isEnable
                }.map {
                    it.title
                }
                if (enableList.isEmpty()) {
                    viewModel.updateTab(viewModel.defaultList)
                } else {
                    initView(enableList)
                }
            }

        }

    }

    private fun initButton() {
        binding.search.setOnClickListener {
            IntentUtil.startActivity<SearchActivity>(requireContext()) {
            }
        }

        binding.menu.setOnClickListener {
            IntentUtil.startActivity<CopyActivity>(requireContext()) {
                putExtra("type", "homeMenu")
            }
        }
    }

    private fun initView(enableList: List<String>) {
        binding.viewPager.offscreenPageLimit = enableList.size
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): Fragment {
                return when (enableList[position]) {
                    "关注" -> HomeFeedFragment.newInstance("follow")
                    "应用" -> AppListFragment()
                    "头条" -> HomeFeedFragment.newInstance("feed")
                    "热榜" -> HomeFeedFragment.newInstance("rank")
                    "话题" -> HomeTopicFragment.newInstance("topic")
                    "数码" -> HomeTopicFragment.newInstance("product")
                    "酷图" -> HomeFeedFragment.newInstance("coolPic")
                    else -> throw IllegalArgumentException()
                }
            }

            override fun getItemCount() = enableList.size

        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = enableList[position]
        }.attach()
        if (viewModel.isInit) {
            viewModel.isInit = false
            if (enableList.contains("头条"))
                binding.viewPager.setCurrentItem(enableList.indexOf("头条"), false)
        }
    }

}