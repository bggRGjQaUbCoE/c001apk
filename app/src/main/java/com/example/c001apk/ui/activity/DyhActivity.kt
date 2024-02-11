package com.example.c001apk.ui.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.c001apk.databinding.ActivityDyhDetailBinding
import com.example.c001apk.ui.fragment.DyhDetailFragment
import com.google.android.material.tabs.TabLayoutMediator

class DyhActivity : BaseActivity<ActivityDyhDetailBinding>() {

    private var id: String? = null
    private var title: String? = null
    private val tabList = listOf("精选", "广场")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.id = intent.getStringExtra("id")
        this.title = intent.getStringExtra("title")

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