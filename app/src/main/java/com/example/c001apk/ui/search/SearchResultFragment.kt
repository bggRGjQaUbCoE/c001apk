package com.example.c001apk.ui.search

import android.os.Bundle
import android.view.MenuItem
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.c001apk.R
import com.example.c001apk.ui.base.BasePagerFragment
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchResultFragment : BasePagerFragment(), IOnSearchMenuClickContainer {

    @Inject
    lateinit var viewModelAssistedFactory: SearchResultViewModel.Factory
    private val viewModel by viewModels<SearchResultViewModel> {
        SearchResultViewModel.provideFactory(
            viewModelAssistedFactory,
            arguments?.getString("keyWord").orEmpty(),
            arguments?.getString("pageType").orEmpty(),
            arguments?.getString("pageParam").orEmpty(),
            arguments?.getString("title").orEmpty(),
        )
    }
    private lateinit var type: MenuItem
    private lateinit var order: MenuItem
    override var controller: IOnSearchMenuClickListener? = null

    companion object {
        @JvmStatic
        fun newInstance(keyWord: String, pageType: String?, pageParam: String?, title: String?) =
            SearchResultFragment().apply {
                arguments = Bundle().apply {
                    putString("keyWord", keyWord)
                    putString("pageType", pageType)
                    putString("pageParam", pageParam)
                    putString("title", title)
                }
            }
    }

    override fun iOnTabSelected(tab: TabLayout.Tab?) {
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

    override fun getFragment(position: Int): Fragment =
        if (viewModel.pageType.isEmpty()) {
            when (position) {
                0 -> SearchContentFragment.newInstance(
                    viewModel.keyWord,
                    "feed",
                    null,
                    null
                )

                1 -> SearchContentFragment.newInstance(
                    viewModel.keyWord,
                    "apk",
                    null,
                    null
                )

                2 -> SearchContentFragment.newInstance(
                    viewModel.keyWord,
                    "game",
                    null,
                    null
                )

                3 -> SearchContentFragment.newInstance(
                    viewModel.keyWord,
                    "product",
                    null,
                    null
                )

                4 -> SearchContentFragment.newInstance(
                    viewModel.keyWord,
                    "user",
                    null,
                    null
                )

                5 -> SearchContentFragment.newInstance(
                    viewModel.keyWord,
                    "feedTopic",
                    null,
                    null
                )

                else -> throw IllegalArgumentException()
            }
        } else {
            SearchContentFragment.newInstance(
                viewModel.keyWord,
                "feed",
                viewModel.pageType,
                viewModel.pageParam
            )

        }

    override fun initTabList() {
        tabList =
            if (viewModel.pageType.isEmpty())
                listOf("动态", "应用", "游戏", "数码", "用户", "话题")
            else {
                binding.tabLayout.isVisible = false
                listOf("")
            }
    }

    override fun onBackClick() {
        activity?.supportFragmentManager?.popBackStack()
    }

    override fun initBar() {
        super.initBar()
        binding.collapsingToolbar.isTitleEnabled = false
        binding.toolBar.apply {
            title = viewModel.keyWord
            setOnClickListener {
                activity?.supportFragmentManager?.popBackStack()
            }
            setTitleTextAppearance(requireContext(), R.style.Toolbar_TitleText)
            if (viewModel.pageType.isNotEmpty())
                subtitle = when (viewModel.pageType) {
                    "tag" -> "话题: ${viewModel.title}"
                    "product_phone" -> "数码: ${viewModel.title}"
                    "apk" -> "应用: ${viewModel.title}"
                    "user" -> "用户: ${viewModel.title}"
                    else -> ""
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

                    R.id.typeRating -> {
                        viewModel.feedType = "rating"
                        controller?.onSearch("feedType", "rating", null)
                    }

                    R.id.typeQuestion -> {
                        viewModel.feedType = "question"
                        controller?.onSearch("feedType", "question", null)
                    }

                    R.id.typeAnswer -> {
                        viewModel.feedType = "answer"
                        controller?.onSearch("feedType", "answer", null)
                    }

                    R.id.typeVote -> {
                        viewModel.feedType = "vote"
                        controller?.onSearch("feedType", "vote", null)
                    }
                }

                menu.findItem(
                    when (viewModel.sort) {
                        "default" -> R.id.feedDefault
                        "hot" -> R.id.feedHot
                        "reply" -> R.id.feedReply
                        else -> throw IllegalArgumentException("sort type error: ${viewModel.sort}")
                    }
                )?.isChecked = true

                menu.findItem(
                    when (viewModel.feedType) {
                        "all" -> R.id.typeAll
                        "feed" -> R.id.typeFeed
                        "feedArticle" -> R.id.typeArticle
                        "picture" -> R.id.typePic
                        "comment" -> R.id.typeReply
                        "rating" -> R.id.typeRating
                        "question" -> R.id.typeQuestion
                        "answer" -> R.id.typeAnswer
                        "vote" -> R.id.typeVote
                        else -> throw IllegalArgumentException("feed type error: ${viewModel.feedType}")
                    }
                )?.isChecked = true
                return@setOnMenuItemClickListener true
            }
        }
    }
}