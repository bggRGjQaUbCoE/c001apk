package com.example.c001apk.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.c001apk.R
import com.example.c001apk.databinding.BaseTablayoutViewpagerBinding
import com.example.c001apk.ui.home.IOnTabClickContainer
import com.example.c001apk.ui.home.IOnTabClickListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

// Toolbar + TabLayout + ViewPager2
abstract class BasePagerFragment : Fragment(), IOnTabClickContainer {

    private var _binding: BaseTablayoutViewpagerBinding? = null
    val binding get() = _binding!!
    override var tabController: IOnTabClickListener? = null
    lateinit var tabList: List<String>

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
            }
        })
    }

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
