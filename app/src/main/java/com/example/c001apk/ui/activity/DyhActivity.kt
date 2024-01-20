package com.example.c001apk.ui.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.c001apk.databinding.ActivityDyhDetailBinding
import com.example.c001apk.ui.fragment.DyhDetailFragment
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.tabs.TabLayoutMediator

class DyhActivity : BaseActivity<ActivityDyhDetailBinding>() {

    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.id = intent.getStringExtra("id")
        viewModel.title = intent.getStringExtra("title")

        initBar()
        if (viewModel.tabList.isEmpty())
            initData()
        initView()

    }

    private fun initData() {
        viewModel.tabList.add("精选")
        viewModel.tabList.add("广场")
    }

    private fun initView() {
        binding.viewPager.offscreenPageLimit = viewModel.tabList.size
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int) =
                when (position) {
                    0 -> DyhDetailFragment.newInstance(viewModel.id.toString(), "all")
                    1 -> DyhDetailFragment.newInstance(viewModel.id.toString(), "square")
                    else -> throw IllegalArgumentException()
                }

            override fun getItemCount() = viewModel.tabList.size
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = viewModel.tabList[position]
        }.attach()
    }

    private fun initBar() {
        binding.toolBar.title = viewModel.title
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