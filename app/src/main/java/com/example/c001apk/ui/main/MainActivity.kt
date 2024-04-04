package com.example.c001apk.ui.main

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityMainBinding
import com.example.c001apk.ui.base.BaseActivity
import com.example.c001apk.ui.home.HomeFragment
import com.example.c001apk.ui.message.MessageFragment
import com.example.c001apk.ui.settings.SettingsFragment
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.color.MaterialColors
import com.google.android.material.navigation.NavigationBarView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(), IOnBottomClickContainer,
    INavViewContainer {

    private val viewModel by viewModels<MainViewModel>()
    private val navViewBehavior by lazy { HideBottomViewOnScrollBehavior<BottomNavigationView>() }
    override var controller: IOnBottomClickListener? = null
    private lateinit var navView: NavigationBarView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navView = binding.bottomNav as NavigationBarView

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        if (viewModel.isInit) {
            viewModel.isInit = false
            genData()
            initObserve()
        } else if (viewModel.badge != 0) {
            setBadge()
        }

        binding.viewPager.apply {
            offscreenPageLimit = 2
            adapter = object : FragmentStateAdapter(this@MainActivity) {
                override fun getItemCount() = 3
                override fun createFragment(position: Int): Fragment {
                    return when (position) {
                        0 -> HomeFragment()
                        1 -> MessageFragment()
                        else -> SettingsFragment()
                    }
                }
            }

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    when (position) {
                        0 -> onBackPressedCallback.isEnabled = false
                        1 -> onBackPressedCallback.isEnabled = true
                        2 -> onBackPressedCallback.isEnabled = true
                    }
                }
            })
            isUserInputEnabled = false
            fixViewPager2Insets(this)
        }

        navView.apply {
            if (this is BottomNavigationView) {
                (layoutParams as CoordinatorLayout.LayoutParams).behavior = navViewBehavior
            }

            setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.navigation_home -> {
                        if (binding.viewPager.currentItem == 0)
                            controller?.onReturnTop()
                        else
                            binding.viewPager.setCurrentItem(0, true)
                    }

                    R.id.navigation_message -> {
                        binding.viewPager.setCurrentItem(1, true)
                        if (viewModel.badge != 0) {
                            navView.removeBadge(R.id.navigation_message)
                            viewModel.badge = 0
                        }
                    }

                    R.id.navigation_setting -> {
                        binding.viewPager.setCurrentItem(2, true)
                    }

                }
                true
            }
            setOnClickListener { /*Do nothing*/ }
            if (this is BottomNavigationView) {
                fixBottomNavigationViewInsets(this)
            }
        }

    }

    private fun initObserve() {
        viewModel.setBadge.observe(this) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                if (it)
                    setBadge()
            }
        }
    }

    private fun genData() {
        viewModel.fetchAppInfo("com.coolapk.market")
    }

    private fun setBadge() {
        val badge = navView.getOrCreateBadge(R.id.navigation_message)
        badge.number = viewModel.badge
        badge.backgroundColor =
            MaterialColors.getColor(
                this,
                com.google.android.material.R.attr.colorPrimary,
                0
            )
        //badge.badgeTextColor = ContextCompat.getColor(this,R.color.design_default_color_error)
        badge.badgeGravity = BadgeDrawable.TOP_END
        badge.verticalOffset = 5
        badge.horizontalOffset = 5
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            if (binding.viewPager.currentItem != 0) {
                this.isEnabled = false
                showNavigationView()
                navView.selectedItemId = navView.menu.getItem(0).itemId
            }
        }
    }

    override fun showNavigationView() {
        if (binding.bottomNav is BottomNavigationView) {
            if (navViewBehavior.isScrolledDown)
                navViewBehavior.slideUp(binding.bottomNav as BottomNavigationView, true)
        }
    }

    override fun hideNavigationView() {
        if (binding.bottomNav is BottomNavigationView) {
            if (navViewBehavior.isScrolledUp)
                navViewBehavior.slideDown(binding.bottomNav as BottomNavigationView, true)
        }
    }

    // from LibChecker
    /**
     * 覆盖掉 BottomNavigationView 内部的 OnApplyWindowInsetsListener 并避免其被软键盘顶起来
     * @see BottomNavigationView.applyWindowInsets
     */
    private fun fixBottomNavigationViewInsets(view: BottomNavigationView) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
            // 这里不直接使用 windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            // 因为它的结果可能受到 insets 传播链上层某环节的影响，出现了错误的 navigationBarsInsets
            val navigationBarsInsets =
                ViewCompat.getRootWindowInsets(view)
                    ?.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = navigationBarsInsets?.bottom ?: 0)
            windowInsets
        }
    }

    private fun fixViewPager2Insets(view: ViewPager2) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
            /* Do nothing */
            windowInsets
        }
    }

}