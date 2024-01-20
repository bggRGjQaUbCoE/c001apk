package com.example.c001apk.ui.fragment.search

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.c001apk.R
import com.example.c001apk.databinding.FragmentSearchResultBinding
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
    private lateinit var type: MenuItem
    private lateinit var order: MenuItem

    companion object {
        @JvmStatic
        fun newInstance(keyWord: String, pageType: String?, pageParam: String?, title: String?) =
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
            viewModel.keyWord = it.getString("KEYWORD")
            viewModel.pageType = it.getString("pageType")
            viewModel.pageParam = it.getString("pageParam")
            viewModel.title = it.getString("title")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initBar()
        initData()

    }

    private fun initBar() {
        binding.toolBar.apply {
            title = viewModel.keyWord
            setTitleTextAppearance(requireContext(), R.style.Toolbar_TitleText)
            if (!viewModel.pageType.isNullOrEmpty())
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
            setNavigationIcon(R.drawable.ic_back)
            setNavigationOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
            }
            inflateMenu(R.menu.search_menu)
            type = menu.findItem(R.id.type)
            order = menu.findItem(R.id.order)
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
            setOnMenuItemClickListener {
                when (it.itemId) {
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
                return@setOnMenuItemClickListener true
            }
        }
    }

    private fun initData() {
        viewModel.tabList =
            if (viewModel.pageType.isNullOrEmpty())
                arrayListOf("动态", "应用", "数码", "用户", "话题")
            else {
                binding.tabLayout.visibility = View.GONE
                arrayListOf("null")
            }
        initView()
    }

    private fun initView() {
        binding.viewPager.offscreenPageLimit = viewModel.tabList.size
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int) =
                if (viewModel.pageType.isNullOrEmpty()) {
                    when (position) {
                        0 -> SearchContentFragment.newInstance(
                            viewModel.keyWord.toString(),
                            "feed",
                            null,
                            null
                        )

                        1 -> SearchContentFragment.newInstance(
                            viewModel.keyWord.toString(),
                            "apk",
                            null,
                            null
                        )

                        2 -> SearchContentFragment.newInstance(
                            viewModel.keyWord.toString(),
                            "product",
                            null,
                            null
                        )

                        3 -> SearchContentFragment.newInstance(
                            viewModel.keyWord.toString(),
                            "user",
                            null,
                            null
                        )

                        4 -> SearchContentFragment.newInstance(
                            viewModel.keyWord.toString(),
                            "feedTopic",
                            null,
                            null
                        )

                        else -> throw IllegalArgumentException()
                    }
                } else {
                    SearchContentFragment.newInstance(
                        viewModel.keyWord.toString(),
                        "feed",
                        viewModel.pageType.toString(),
                        viewModel.pageParam.toString()
                    )

                }

            override fun getItemCount() = viewModel.tabList.size
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = viewModel.tabList[position]
        }.attach()
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        type.isVisible = true
                        order.isVisible = true
                    }

                    else -> {
                        type.isVisible = false
                        order.isVisible = false
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {
                tabController?.onReturnTop(null)
            }

        })
    }

}