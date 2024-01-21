package com.example.c001apk.ui.fragment.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.ThemeUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.c001apk.databinding.FragmentHomeBinding
import com.example.c001apk.logic.database.HomeMenuDatabase
import com.example.c001apk.logic.model.HomeMenu
import com.example.c001apk.ui.activity.CopyActivity
import com.example.c001apk.ui.activity.SearchActivity
import com.example.c001apk.ui.fragment.BaseFragment
import com.example.c001apk.ui.fragment.home.app.AppListFragment
import com.example.c001apk.ui.fragment.home.topic.TopicFragment
import com.example.c001apk.ui.fragment.minterface.IOnBottomClickContainer
import com.example.c001apk.ui.fragment.minterface.IOnBottomClickListener
import com.example.c001apk.ui.fragment.minterface.IOnTabClickContainer
import com.example.c001apk.ui.fragment.minterface.IOnTabClickListener
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.Tab
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class HomeFragment : BaseFragment<FragmentHomeBinding>(), IOnBottomClickListener,
    IOnTabClickContainer {

    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    override var tabController: IOnTabClickListener? = null
    private val homeMenuDao by lazy {
        HomeMenuDatabase.getDatabase(requireContext()).homeMenuDao()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.tabList.isEmpty())
            initTabMenu()
        else
            initView()
        initMenu()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            @SuppressLint("RestrictedApi")
            override fun onTabSelected(tab: Tab?) {
                viewModel.position = tab!!.position
                val textView = TextView(requireContext())
                val selectedSize = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_PX,
                    17f,
                    resources.displayMetrics
                )
                textView.paint.isFakeBoldText = true
                textView.gravity = Gravity.CENTER_HORIZONTAL
                textView.setTextColor(
                    ThemeUtils.getThemeAttrColor(
                        requireContext(),
                        rikka.preference.simplemenu.R.attr.colorPrimary
                    )
                )
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, selectedSize)
                textView.text = tab.text
                tab.setCustomView(textView)
            }

            @SuppressLint("RestrictedApi")
            override fun onTabUnselected(tab: Tab?) {
                val textView = TextView(requireContext())
                val selectedSize = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_PX,
                    15f,
                    resources.displayMetrics
                )
                textView.paint.isFakeBoldText = false
                textView.gravity = Gravity.CENTER_HORIZONTAL
                textView.setTextColor(
                    ThemeUtils.getThemeAttrColor(
                        requireContext(),
                        rikka.preference.simplemenu.R.attr.colorControlNormal
                    )
                )
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, selectedSize)
                textView.text = tab?.text
                tab?.setCustomView(textView)
            }

            override fun onTabReselected(tab: Tab?) {
                if (tab?.text == "关注")
                    tabController?.onReturnTop(false)
                else
                    tabController?.onReturnTop(true)
            }

        })

    }

    private fun initTabMenu() {
        CoroutineScope(Dispatchers.IO).launch {
            viewModel.menuList.addAll(homeMenuDao.loadAll())
            if (viewModel.menuList.isEmpty()) {
                initMenuList()
            } else {
                for (element in viewModel.menuList) {
                    if (element.isEnable)
                        viewModel.tabList.add(element.title)
                }
                if (viewModel.tabList.isEmpty()) {
                    homeMenuDao.deleteAll()
                    initMenuList()
                }
            }
            withContext(Dispatchers.Main) {
                initView()
            }
        }
    }

    private fun initMenuList() {
        homeMenuDao.apply {
            insert(HomeMenu(0, "关注", true))
            insert(HomeMenu(1, "应用", true))
            insert(HomeMenu(2, "头条", true))
            insert(HomeMenu(3, "热榜", true))
            insert(HomeMenu(4, "话题", true))
            insert(HomeMenu(5, "数码", true))
            insert(HomeMenu(6, "酷图", true))
        }
        viewModel.tabList.apply {
            add("关注")
            add("应用")
            add("头条")
            add("热榜")
            add("话题")
            add("数码")
            add("酷图")
        }
    }

    private fun initMenu() {
        binding.search.setOnClickListener {
            IntentUtil.startActivity<SearchActivity>(requireContext()) {
            }
        }

        binding.menu.setOnClickListener {
            IntentUtil.startActivity<CopyActivity>(requireContext()) {
                putExtra("type", "homeMenu")
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun initView() {
        binding.viewPager.offscreenPageLimit = viewModel.tabList.size
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): Fragment {
                return when (viewModel.tabList[position]) {
                    "关注" -> HomeFeedFragment.newInstance("follow")
                    "应用" -> AppListFragment()
                    "头条" -> HomeFeedFragment.newInstance("feed")
                    "热榜" -> HomeFeedFragment.newInstance("rank")
                    "话题" -> TopicFragment.newInstance("topic")
                    "数码" -> TopicFragment.newInstance("product")
                    "酷图" -> HomeFeedFragment.newInstance("coolPic")
                    else -> throw IllegalArgumentException()
                }
            }

            override fun getItemCount() = viewModel.tabList.size

        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = viewModel.tabList[position]
        }.attach()
        if (viewModel.isInitial) {
            viewModel.isInitial = false
            if (viewModel.tabList.contains("头条"))
                binding.viewPager.setCurrentItem(viewModel.tabList.indexOf("头条"), false)
        } else {
            val textView = TextView(requireContext())
            val selectedSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_PX,
                17f,
                resources.displayMetrics
            )
            textView.paint.isFakeBoldText = true
            textView.gravity = Gravity.CENTER_HORIZONTAL
            textView.setTextColor(
                ThemeUtils.getThemeAttrColor(
                    requireContext(),
                    rikka.preference.simplemenu.R.attr.colorPrimary
                )
            )
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, selectedSize)
            textView.text = binding.tabLayout.getTabAt(viewModel.position)?.text
            binding.tabLayout.getTabAt(viewModel.position)?.setCustomView(textView)
        }
    }

    override fun onReturnTop() {
        tabController?.onReturnTop(true)
    }

    override fun onPause() {
        super.onPause()
        (requireContext() as? IOnBottomClickContainer)?.controller = null
    }

    override fun onResume() {
        super.onResume()
        (requireContext() as? IOnBottomClickContainer)?.controller = this
    }

}