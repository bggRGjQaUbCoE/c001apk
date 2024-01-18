package com.example.c001apk.ui.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.c001apk.R
import com.example.c001apk.databinding.FragmentTopicBinding
import com.example.c001apk.ui.fragment.CollectionFragment
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.tabs.TabLayoutMediator

class CoolPicActivity : BaseActivity<FragmentTopicBinding>() {

    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private val fragmentList: MutableList<Fragment> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.title = intent.getStringExtra("title")

        initBar()
        initTabList()
        initView()
    }

    private fun initBar() {
        binding.toolBar.apply {
            title = viewModel.title
            setNavigationIcon(R.drawable.ic_back)
            setNavigationOnClickListener {
                finish()
            }
        }
    }

    private fun initTabList() {
        if (viewModel.tabList.isEmpty()) {
            viewModel.tabList.apply {
                add("精选")
                add("热门")
                add("最新")
            }
        }
    }

    private fun initView() {
        initFragmentList()
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int) = fragmentList[position]
            override fun getItemCount() = viewModel.tabList.size
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = viewModel.tabList[position]
        }.attach()
    }

    private fun initFragmentList() {
        fragmentList.apply {
            add(CollectionFragment.newInstance("recommend", viewModel.title.toString()))
            add(CollectionFragment.newInstance("hot", viewModel.title.toString()))
            add(CollectionFragment.newInstance("newest", viewModel.title.toString()))
        }
    }

}