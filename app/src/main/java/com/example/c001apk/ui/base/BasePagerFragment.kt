package com.example.c001apk.ui.base

import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.c001apk.R
import com.example.c001apk.databinding.BaseTablayoutViewpagerBinding
import com.example.c001apk.ui.home.IOnTabClickContainer
import com.example.c001apk.ui.home.IOnTabClickListener
import com.example.c001apk.util.dp
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

// Toolbar + TabLayout + ViewPager2
abstract class BasePagerFragment : Fragment(), IOnTabClickContainer {

    private var _binding: BaseTablayoutViewpagerBinding? = null
    val binding get() = _binding!!
    override var tabController: IOnTabClickListener? = null
    lateinit var tabList: List<String>
    val fabBehavior by lazy { HideBottomViewOnScrollBehavior<FloatingActionButton>() }
    lateinit var fab: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BaseTablayoutViewpagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appBar.setLiftable(true)

        initTabList()
        initBar()
        initView()
    }

    open fun initFab() {
        fab = FloatingActionButton(requireContext()).apply {
            setImageResource(R.drawable.outline_note_alt_24)
            layoutParams = CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                CoordinatorLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                behavior = fabBehavior
            }
            if (SDK_INT >= 26)
                tooltipText = getString(R.string.publishFeed)
        }
        // https://stackoverflow.com/questions/54062834/setonapplywindowinsetslistener-never-called
        ViewCompat.setOnApplyWindowInsetsListener(binding.collapsingToolbar, null)
        ViewCompat.setOnApplyWindowInsetsListener(fab) { _, insets ->
            val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            fab.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                rightMargin = 25.dp
                bottomMargin = navigationBars.bottom + 25.dp
            }
            insets
        }
        binding.root.addView(fab)
    }

    fun initView() {
        binding.viewPager.offscreenPageLimit =
            with(tabList.size) {
                if (this < 1) ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
                else this
            }
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int) = getFragment(position)
            override fun getItemCount() = tabList.size
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabList[position]
        }.attach()
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                iOnTabSelected(tab)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {
                tabController?.onReturnTop(null)
                onTabReselectedExtra()
            }
        })
    }

    open fun onTabReselectedExtra() {}

    open fun iOnTabSelected(tab: TabLayout.Tab?) {}

    abstract fun getFragment(position: Int): Fragment

    abstract fun initTabList()

    open fun initBar() {
        binding.toolBar.apply {
            setNavigationIcon(R.drawable.ic_back)
            setNavigationOnClickListener {
                onBackClick()
            }
        }
    }

    abstract fun onBackClick()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
