package com.example.c001apk.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityMainBinding
import com.example.c001apk.ui.fragment.BlankFragment
import com.example.c001apk.ui.fragment.home.HomeFragment
import com.example.c001apk.ui.fragment.minterface.IOnBottomClickContainer
import com.example.c001apk.ui.fragment.minterface.IOnBottomClickListener

class MainActivity : AppCompatActivity(), IOnBottomClickContainer {

    private lateinit var binding: ActivityMainBinding

    override var controller: IOnBottomClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewPager.apply {
            adapter = object : FragmentStateAdapter(this@MainActivity) {
                override fun getItemCount() = 3
                override fun createFragment(position: Int): Fragment {
                    return when (position) {
                        0 -> HomeFragment()
                        1 -> BlankFragment()
                        2 -> BlankFragment()
                        else -> HomeFragment()
                    }
                }
            }
            isUserInputEnabled = false
        }

        binding.viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.bottomNav.menu.getItem(position)?.isChecked = true
            }
        })

        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    if (binding.viewPager.currentItem == 0)
                        controller?.onReturnTop()
                    else
                        binding.viewPager.setCurrentItem(0, true)
                }

                R.id.navigation_message -> binding.viewPager.setCurrentItem(1, true)
                R.id.navigation_setting -> binding.viewPager.setCurrentItem(2, true)
            }
            true
        }

    }

}