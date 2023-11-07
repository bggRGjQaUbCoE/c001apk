package com.example.c001apk.ui.fragment.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.c001apk.databinding.FragmentHomeBinding
import com.example.c001apk.ui.activity.SearchActivity
import com.example.c001apk.ui.fragment.home.topic.TopicFragment
import com.example.c001apk.ui.fragment.minterface.IOnBottomClickContainer
import com.example.c001apk.ui.fragment.minterface.IOnBottomClickListener
import com.example.c001apk.ui.fragment.minterface.IOnTabClickContainer
import com.example.c001apk.ui.fragment.minterface.IOnTabClickListener
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.Tab
import com.google.android.material.tabs.TabLayoutMediator


class HomeFragment : Fragment(), IOnBottomClickListener, IOnTabClickContainer {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private val tabList = arrayOf("关注", "应用", "头条", "热榜", "话题")
    private var fragmentList = ArrayList<Fragment>()
    override var controller: IOnTabClickListener? = null


    init {
        fragmentList.run {
            add(HomeFeedFragment.newInstance("follow"))
            add(AppListFragment())
            add(HomeFeedFragment.newInstance("feed"))
            add(HomeFeedFragment.newInstance("rank"))
            add(TopicFragment())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: Tab?) {}

            override fun onTabUnselected(tab: Tab?) {
            }

            override fun onTabReselected(tab: Tab?) {
                controller?.onReturnTop(tab!!.position)
            }

        })

        initView()
        initMenu()

    }

    private fun initMenu() {
        binding.search.setOnClickListener {
            startActivity(Intent(activity, SearchActivity::class.java))
        }
    }

    /*private fun initMenu() {
        binding.toolBar.inflateMenu(R.menu.home_feed_menu)
        binding.toolBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.search -> startActivity(Intent(activity, SearchActivity::class.java))
            }
            return@setOnMenuItemClickListener true
        }
    }*/

    private fun initView() {
        binding.viewPager.offscreenPageLimit = tabList.size
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int) = fragmentList[position]
            override fun getItemCount() = tabList.size
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabList[position]
        }.attach()
        if (viewModel.isInitial) {
            binding.viewPager.setCurrentItem(2, false)
            viewModel.isInitial = false
        }
    }

    override fun onReturnTop() {
        controller?.onReturnTop(binding.viewPager.currentItem)
    }

    override fun onResume() {
        super.onResume()
        (requireContext() as IOnBottomClickContainer).controller = this
    }

}