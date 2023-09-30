package com.example.coolapk.ui.fragment.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.coolapk.R
import com.example.coolapk.databinding.FragmentHomeBinding
import com.example.coolapk.ui.activity.search.SearchActivity
import com.example.coolapk.ui.fragment.BlankFragment
import com.example.coolapk.ui.fragment.home.feed.HomeFeedFragment
import com.google.android.material.tabs.TabLayoutMediator


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel by lazy { ViewModelProvider(this)[HomeViewModel::class.java] }
    private val tabList = arrayOf("好友关注", "头条", "热榜")
    private var fragmentList = ArrayList<Fragment>()

    init {
        fragmentList.run {
            add(BlankFragment())
            add(HomeFeedFragment())
            add(BlankFragment())
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

        initView()
        initMenu()

    }

    private fun initMenu() {
        binding.toolBar.inflateMenu(R.menu.home_feed_menu)
        binding.toolBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.search -> startActivity(Intent(activity, SearchActivity::class.java))
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun initView() {
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int) = fragmentList[position]
            override fun getItemCount() = tabList.size
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabList[position]
        }.attach()
        if (viewModel.isInitial) {
            binding.viewPager.setCurrentItem(1, false)
            viewModel.isInitial = false
        }
    }

}