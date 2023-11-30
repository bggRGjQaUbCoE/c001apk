package com.example.c001apk.ui.fragment.topic

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.c001apk.R
import com.example.c001apk.databinding.FragmentTopicBinding
import com.example.c001apk.logic.database.TopicBlackListDatabase
import com.example.c001apk.logic.model.SearchHistory
import com.example.c001apk.ui.activity.SearchActivity
import com.example.c001apk.ui.activity.TopicActivity
import com.example.c001apk.ui.fragment.minterface.IOnSearchMenuClickContainer
import com.example.c001apk.ui.fragment.minterface.IOnSearchMenuClickListener
import com.example.c001apk.ui.fragment.minterface.IOnTabClickContainer
import com.example.c001apk.ui.fragment.minterface.IOnTabClickListener
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TopicFragment : Fragment(), IOnSearchMenuClickContainer, IOnTabClickContainer {

    private lateinit var binding: FragmentTopicBinding
    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    override var controller: IOnSearchMenuClickListener? = null
    override var tabController: IOnTabClickListener? = null
    private val topicBlackListDao by lazy {
        TopicBlackListDatabase.getDatabase(requireContext()).blackListDao()
    }

    companion object {
        @JvmStatic
        fun newInstance(type: String, title: String, url: String, id: String) =
            TopicFragment().apply {
                arguments = Bundle().apply {
                    putString("url", url)
                    putString("title", title)
                    putString("id", id)
                    putString("type", type)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            viewModel.url = it.getString("url")!!
            viewModel.title = it.getString("title")!!
            viewModel.id = it.getString("id")!!
            viewModel.type = it.getString("type")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTopicBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.tabList.isEmpty())
            getViewData()
        else {
            initView(null)
            initBar()
            initViewPagerMenu()
        }

        viewModel.topicLayoutLiveData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val data = result.getOrNull()
                if (data != null) {
                    if (viewModel.tabList.isEmpty()) {
                        viewModel.type = data.entityType
                        viewModel.subtitle = data.intro
                        initBar()

                        for (element in data.tabList) {
                            viewModel.tabList.add(element.title)
                            viewModel.fragmentList.add(
                                TopicContentFragment.newInstance(
                                    element.url,
                                    element.title,
                                )
                            )
                        }
                        var tabSelected = 0
                        for (element in data.tabList) {
                            if (data.selectedTab == element.pageName) break
                            else tabSelected++
                        }
                        initView(tabSelected)
                    }
                    binding.indicator.isIndeterminate = false
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

        viewModel.productLayoutLiveData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val data = result.getOrNull()
                if (data != null) {
                    if (viewModel.tabList.isEmpty()) {
                        viewModel.subtitle = data.intro
                        initBar()

                        for (element in data.tabList) {
                            viewModel.tabList.add(element.title)
                            viewModel.fragmentList.add(
                                TopicContentFragment.newInstance(
                                    element.url,
                                    element.title,
                                )
                            )
                        }
                        var tabSelected = 0
                        for (element in data.tabList) {
                            if (data.selectedTab == element.pageName) break
                            else tabSelected++
                        }
                        initView(tabSelected)
                    }
                    binding.indicator.isIndeterminate = false
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

    }

    private fun initBar() {
        binding.toolBar.apply {
            title = if (viewModel.type == "topic") viewModel.url.replace("/t/", "")
            else viewModel.title
            viewModel.subtitle?.let { subtitle = viewModel.subtitle }
        }
        (activity as TopicActivity).setSupportActionBar(binding.toolBar)
        (activity as TopicActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initView(tabSelected: Int?) {
        binding.viewPager.offscreenPageLimit = viewModel.tabList.size
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int) = viewModel.fragmentList[position]
            override fun getItemCount() = viewModel.tabList.size
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = viewModel.tabList[position]
        }.attach()
        if (viewModel.isInit && tabSelected != null) {
            binding.viewPager.setCurrentItem(tabSelected, false)
            viewModel.isInit = false
        }
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {
                tabController?.onReturnTop()
            }

        })
    }

    private fun getViewData() {
        binding.indicator.visibility = View.VISIBLE
        binding.indicator.isIndeterminate = true
        viewModel.isNew = true
        initViewPagerMenu()
        if (viewModel.type == "topic") {
            viewModel.url = viewModel.url.replace("/t/", "")
            viewModel.getTopicLayout()
        } else if (viewModel.type == "product") {
            viewModel.getProductLayout()
        }
    }

    private fun initViewPagerMenu() {
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageSelected(position: Int) {}
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                requireActivity().invalidateOptionsMenu()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.topic_product_menu, menu)
        if (viewModel.type == "product")
            menu.findItem(R.id.order).isVisible = binding.viewPager.currentItem == 1
        else
            menu.findItem(R.id.order).isVisible = false
    }

    override fun onPrepareOptionsMenu(menu: Menu) {

        //if (viewModel.type == "product") {
        menu.findItem(
            when (viewModel.productTitle) {
                "最近回复" -> R.id.topicLatestReply
                "热度排序" -> R.id.topicHot
                "最新发布" -> R.id.topicLatestPublish
                else -> throw IllegalArgumentException("type error")
            }
        )?.isChecked = true
        // }

        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> requireActivity().finish()

            R.id.search -> {
                val intent = Intent(activity, SearchActivity::class.java)
                if (viewModel.type == "topic") {
                    intent.putExtra("pageType", "tag")
                    intent.putExtra("pageParam", viewModel.title)
                    intent.putExtra("title", viewModel.title)
                } else {
                    intent.putExtra("pageType", "product_phone")
                    intent.putExtra("pageParam", viewModel.id)
                    intent.putExtra("title", viewModel.title)
                }
                requireActivity().startActivity(intent)
            }

            R.id.topicLatestReply -> {
                viewModel.productTitle = "最近回复"
                controller?.onSearch("title", "最近回复", viewModel.id)
            }

            R.id.topicHot -> {
                viewModel.productTitle = "热度排序"
                controller?.onSearch("title", "热度排序", viewModel.id)
            }

            R.id.topicLatestPublish -> {
                viewModel.productTitle = "最新发布"
                controller?.onSearch("title", "最新发布", viewModel.id)
            }

            R.id.block -> {
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setTitle("确定将 ${viewModel.title} 加入黑名单？")
                    setNegativeButton(android.R.string.cancel, null)
                    setPositiveButton(android.R.string.ok) { _, _ ->
                        CoroutineScope(Dispatchers.IO).launch {
                            if (!topicBlackListDao.isExist(viewModel.title)) {
                                topicBlackListDao.insert(SearchHistory(viewModel.title))
                            }
                        }
                    }
                    show()
                }
            }

        }
        return super.onOptionsItemSelected(item)
    }

}