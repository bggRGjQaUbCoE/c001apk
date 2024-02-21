package com.example.c001apk.ui.dyh

import android.os.Bundle
import android.view.MenuItem
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.c001apk.databinding.ActivityDyhDetailBinding
import com.example.c001apk.ui.base.BaseActivity
import com.example.c001apk.ui.home.IOnTabClickContainer
import com.example.c001apk.ui.home.IOnTabClickListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class DyhActivity : BaseActivity<ActivityDyhDetailBinding>(), IOnTabClickContainer {

    private var id: String? = null
    private var title: String? = null
    private val tabList = listOf("精选", "广场")
    override var tabController: IOnTabClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.id = intent.getStringExtra("id")
        this.title = intent.getStringExtra("title")

        binding.appBar.setLiftable(true)

        initBar()
        initView()

    }


    private fun initView() {
        binding.viewPager.offscreenPageLimit = tabList.size
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int) =
                when (position) {
                    0 -> DyhDetailFragment.newInstance(id.toString(), "all")
                    1 -> DyhDetailFragment.newInstance(id.toString(), "square")
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

    private fun initBar() {
        binding.toolBar.title = title
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }
}