package com.example.c001apk.ui.coolpic

import android.os.Bundle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityCoolPicBinding
import com.example.c001apk.ui.base.BaseActivity
import com.example.c001apk.ui.collection.CollectionFragment
import com.example.c001apk.ui.home.IOnTabClickContainer
import com.example.c001apk.ui.home.IOnTabClickListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CoolPicActivity : BaseActivity<ActivityCoolPicBinding>(), IOnTabClickContainer {

    private val titleText by lazy { intent.getStringExtra("title").orEmpty() }
    private val tabList = listOf("精选", "热门", "最新")
    override var tabController: IOnTabClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.appBar.setLiftable(true)

        initBar()
        initView()

    }

    private fun initBar() {
        binding.toolBar.apply {
            title = titleText
            setNavigationIcon(R.drawable.ic_back)
            setNavigationOnClickListener {
                finish()
            }
        }
    }

    private fun initView() {
        binding.viewPager.offscreenPageLimit = tabList.size
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int) =
                when (position) {
                    0 -> CollectionFragment.newInstance("recommend", titleText)
                    1 -> CollectionFragment.newInstance("hot", titleText)
                    2 -> CollectionFragment.newInstance("newest", titleText)
                    else -> throw IllegalArgumentException()
                }

            override fun getItemCount() = tabList.size
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabList[position]
        }.attach()
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {
                tabController?.onReturnTop(null)
            }
        })
    }

}