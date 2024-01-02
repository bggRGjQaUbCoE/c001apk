package com.example.c001apk.ui.fragment.search

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.c001apk.R
import com.example.c001apk.databinding.FragmentSearchResultBinding
import com.example.c001apk.ui.activity.SearchActivity
import com.example.c001apk.ui.fragment.BaseFragment
import com.example.c001apk.ui.fragment.minterface.IOnSearchMenuClickContainer
import com.example.c001apk.ui.fragment.minterface.IOnSearchMenuClickListener
import com.example.c001apk.ui.fragment.minterface.IOnTabClickContainer
import com.example.c001apk.ui.fragment.minterface.IOnTabClickListener
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class SearchResultFragment : BaseFragment<FragmentSearchResultBinding>(),
    IOnSearchMenuClickContainer, IOnTabClickContainer {

    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    override var controller: IOnSearchMenuClickListener? = null
    override var tabController: IOnTabClickListener? = null

    companion object {
        @JvmStatic
        fun newInstance(keyWord: String, pageType: String, pageParam: String, title: String) =
            SearchResultFragment().apply {
                arguments = Bundle().apply {
                    putString("KEYWORD", keyWord)
                    putString("pageType", pageType)
                    putString("pageParam", pageParam)
                    putString("title", title)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            viewModel.keyWord = it.getString("KEYWORD")!!
            viewModel.pageType = it.getString("pageType")!!
            viewModel.pageParam = it.getString("pageParam")!!
            viewModel.title = it.getString("title")!!
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initBar()
        initData()
        initView()
        initViewPagerMenu()

    }

    private fun initBar() {
        binding.toolBar.apply {
            title = viewModel.keyWord
            setTitleTextAppearance(requireContext(), R.style.Toolbar_TitleText)
            if (viewModel.pageType != "")
                subtitle = when (viewModel.pageType) {
                    "tag" -> "话题: ${viewModel.title}"
                    "product_phone" -> "数码: ${viewModel.title}"
                    "apk" -> "应用: ${viewModel.title}"
                    "user" -> "用户: ${viewModel.title}"
                    else -> ""
                }
            setOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
        (activity as SearchActivity).setSupportActionBar(binding.toolBar)
        (activity as SearchActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initData() {
        if (viewModel.pageType == "") {
            viewModel.searchTabList = arrayOf("动态", "应用", "数码", "用户", "话题")
            viewModel.searchFragmentList.run {
                add(SearchContentFragment.newInstance(viewModel.keyWord, "feed", "", ""))
                add(SearchContentFragment.newInstance(viewModel.keyWord, "apk", "", ""))
                add(SearchContentFragment.newInstance(viewModel.keyWord, "product", "", ""))
                add(SearchContentFragment.newInstance(viewModel.keyWord, "user", "", ""))
                add(SearchContentFragment.newInstance(viewModel.keyWord, "feedTopic", "", ""))
            }
        } else {
            binding.tabLayout.visibility = View.GONE
            viewModel.searchTabList = arrayOf("null")
            viewModel.searchFragmentList
                .add(
                    SearchContentFragment.newInstance(
                        viewModel.keyWord,
                        "feed",
                        viewModel.pageType,
                        viewModel.pageParam
                    )
                )
        }
    }

    private fun initView() {
        binding.viewPager.offscreenPageLimit = viewModel.searchTabList.size
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int) = viewModel.searchFragmentList[position]
            override fun getItemCount() = viewModel.searchTabList.size
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = viewModel.searchTabList[position]
        }.attach()
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {
                tabController?.onReturnTop(null)
            }

        })
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
        inflater.inflate(R.menu.search_menu, menu)
        menu.findItem(R.id.type).isVisible = binding.viewPager.currentItem == 0
        menu.findItem(R.id.order).isVisible = binding.viewPager.currentItem == 0
    }

    override fun onPrepareOptionsMenu(menu: Menu) {

        menu.findItem(
            when (viewModel.sort) {
                "default" -> R.id.feedDefault
                "hot" -> R.id.feedHot
                "reply" -> R.id.feedReply
                else -> throw IllegalArgumentException("type error")
            }
        )?.isChecked = true

        menu.findItem(
            when (viewModel.feedType) {
                "all" -> R.id.typeAll
                "feed" -> R.id.typeFeed
                "feedArticle" -> R.id.typeArticle
                "picture" -> R.id.typePic
                "comment" -> R.id.typeReply
                else -> throw IllegalArgumentException("type error")
            }
        )?.isChecked = true

        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> requireActivity().supportFragmentManager.popBackStack()

            R.id.feedDefault -> {
                viewModel.sort = "default"
                controller?.onSearch("sort", "default", null)
            }

            R.id.feedHot -> {
                viewModel.sort = "hot"
                controller?.onSearch("sort", "hot", null)
            }

            R.id.feedReply -> {
                viewModel.sort = "reply"
                controller?.onSearch("sort", "reply", null)
            }

            R.id.typeAll -> {
                viewModel.feedType = "all"
                controller?.onSearch("feedType", "all", null)
            }

            R.id.typeFeed -> {
                viewModel.feedType = "feed"
                controller?.onSearch("feedType", "feed", null)
            }

            R.id.typeArticle -> {
                viewModel.feedType = "feedArticle"
                controller?.onSearch("feedType", "feedArticle", null)
            }

            R.id.typePic -> {
                viewModel.feedType = "picture"
                controller?.onSearch("feedType", "picture", null)
            }

            R.id.typeReply -> {
                viewModel.feedType = "comment"
                controller?.onSearch("feedType", "comment", null)
            }

        }
        return super.onOptionsItemSelected(item)
    }


}