package com.example.c001apk.ui.fragment.search

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
import com.example.c001apk.databinding.FragmentSearchResultBinding
import com.example.c001apk.ui.activity.SearchActivity
import com.example.c001apk.ui.fragment.minterface.IOnSearchMenuClickContainer
import com.example.c001apk.ui.fragment.minterface.IOnSearchMenuClickListener
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.tabs.TabLayoutMediator

class SearchResultFragment : Fragment(), IOnSearchMenuClickContainer {

    private lateinit var binding: FragmentSearchResultBinding
    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    override var controller: IOnSearchMenuClickListener? = null

    companion object {
        @JvmStatic
        fun newInstance(keyWord: String) =
            SearchResultFragment().apply {
                arguments = Bundle().apply {
                    putString("KEYWORD", keyWord)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            viewModel.keyWord = it.getString("KEYWORD")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchResultBinding.inflate(inflater, container, false)
        return binding.root
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
            setOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
            }
            setNavigationIcon(R.drawable.ic_back)
            binding.toolBar.setNavigationOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
        (activity as SearchActivity).setSupportActionBar(binding.toolBar)
        (activity as SearchActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initData() {
        viewModel.searchFragmentList.run {
            add(SearchContentFragment.newInstance(viewModel.keyWord, "feed"))
            add(SearchContentFragment.newInstance(viewModel.keyWord, "apk"))
            add(SearchContentFragment.newInstance(viewModel.keyWord, "product"))
            add(SearchContentFragment.newInstance(viewModel.keyWord, "user"))
            add(SearchContentFragment.newInstance(viewModel.keyWord, "feedTopic"))
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
            R.id.feedDefault -> {
                viewModel.sort = "default"
                controller?.onSearch("sort", "default")
            }

            R.id.feedHot -> {
                viewModel.sort = "hot"
                controller?.onSearch("sort", "hot")
            }

            R.id.feedReply -> {
                viewModel.sort = "reply"
                controller?.onSearch("sort", "reply")
            }

            R.id.typeAll -> {
                viewModel.feedType = "all"
                controller?.onSearch("feedType", "all")
            }

            R.id.typeFeed -> {
                viewModel.feedType = "feed"
                controller?.onSearch("feedType", "feed")
            }

            R.id.typeArticle -> {
                viewModel.feedType = "feedArticle"
                controller?.onSearch("feedType", "feedArticle")
            }

            R.id.typePic -> {
                viewModel.feedType = "picture"
                controller?.onSearch("feedType", "picture")
            }

            R.id.typeReply -> {
                viewModel.feedType = "comment"
                controller?.onSearch("feedType", "comment")
            }

        }
        return super.onOptionsItemSelected(item)
    }


}