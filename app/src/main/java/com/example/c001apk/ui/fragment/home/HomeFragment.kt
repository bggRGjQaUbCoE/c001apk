package com.example.c001apk.ui.fragment.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.c001apk.databinding.FragmentHomeBinding
import com.example.c001apk.logic.database.HomeMenuDatabase
import com.example.c001apk.logic.model.HomeMenu
import com.example.c001apk.ui.activity.CopyActivity
import com.example.c001apk.ui.activity.SearchActivity
import com.example.c001apk.ui.fragment.home.topic.TopicFragment
import com.example.c001apk.ui.fragment.minterface.IOnBottomClickContainer
import com.example.c001apk.ui.fragment.minterface.IOnBottomClickListener
import com.example.c001apk.ui.fragment.minterface.IOnTabClickContainer
import com.example.c001apk.ui.fragment.minterface.IOnTabClickListener
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.Tab
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class HomeFragment : Fragment(), IOnBottomClickListener, IOnTabClickContainer {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private var tabList = ArrayList<String>()
    private var fragmentList = ArrayList<Fragment>()
    override var tabController: IOnTabClickListener? = null
    private var menuList: ArrayList<HomeMenu> = ArrayList()
    private val homeMenuDao by lazy {
        HomeMenuDatabase.getDatabase(requireContext()).homeMenuDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initTabMenu()
        initMenu()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: Tab?) {}

            override fun onTabUnselected(tab: Tab?) {
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
            menuList.addAll(homeMenuDao.loadAll())
            if (menuList.isEmpty()) {
                initMenuList()
            } else {
                for (element in menuList) {
                    if (element.isEnable)
                        tabList.add(element.title)
                }
                if (tabList.isEmpty()) {
                    homeMenuDao.deleteAll()
                    initMenuList()
                }
            }
            fragmentList.apply {
                for (element in tabList) {
                    when (element) {
                        "关注" -> add(HomeFeedFragment.newInstance("follow"))
                        "应用" -> add(AppListFragment())
                        "头条" -> add(HomeFeedFragment.newInstance("feed"))
                        "热榜" -> add(HomeFeedFragment.newInstance("rank"))
                        "话题" -> add(TopicFragment.newInstance("topic"))
                        "数码" -> add(TopicFragment.newInstance("product"))
                    }
                }
            }
            withContext(Dispatchers.Main) {
                initView()
            }
        }
    }

    private fun initMenuList() {
        homeMenuDao.apply {
            insert(HomeMenu("关注", true))
            insert(HomeMenu("应用", true))
            insert(HomeMenu("头条", true))
            insert(HomeMenu("热榜", true))
            insert(HomeMenu("话题", true))
            insert(HomeMenu("数码", true))
        }
        tabList.apply {
            add("关注")
            add("应用")
            add("头条")
            add("热榜")
            add("话题")
            add("数码")
        }
    }

    private fun initMenu() {
        binding.search.setOnClickListener {
            val intent = Intent(activity, SearchActivity::class.java)
            intent.putExtra("pageType", "")
            intent.putExtra("pageParam", "")
            intent.putExtra("title", "")
            requireActivity().startActivity(intent)
        }

        binding.menu.setOnClickListener {
            val intent = Intent(activity, CopyActivity::class.java)
            intent.putExtra("type", "homeMenu")
            requireActivity().startActivity(intent)
        }
    }

    /*private fun initMenu() {
        binding.toolBar.inflateMenu(R.menu.home_feed_menu)
        binding.toolBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.search -> startActivity(Intent(activity, SearchActivity::class.java))
            }
            return@setOnMenuItemClickListener true
        }
    }*/

    private fun initView() {
        binding.viewPager.offscreenPageLimit = tabList.size
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): Fragment {
                return fragmentList[position]
            }

            override fun getItemCount() = tabList.size

        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabList[position]
        }.attach()
        if (viewModel.isInitial) {
            viewModel.isInitial = false
            if (menuList.contains(HomeMenu("头条", true)))
                binding.viewPager.setCurrentItem(tabList.indexOf("头条"), false)
        }
    }

    override fun onReturnTop() {
        tabController?.onReturnTop(true)
    }

    override fun onResume() {
        super.onResume()
        (requireContext() as IOnBottomClickContainer).controller = this
    }

}