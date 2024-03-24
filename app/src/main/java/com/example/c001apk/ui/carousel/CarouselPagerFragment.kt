package com.example.c001apk.ui.carousel

import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.c001apk.ui.base.BasePagerFragment
import com.example.c001apk.ui.home.IOnTabClickListener
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CarouselPagerFragment : BasePagerFragment() {

    private val viewModel by viewModels<CarouselViewModel>(ownerProducer = { requireActivity() })
    override var tabController: IOnTabClickListener? = null

    override fun getFragment(position: Int): Fragment =
        if (!viewModel.topicList.isNullOrEmpty())
            CarouselFragment.newInstance(
                viewModel.topicList?.get(position)?.url.orEmpty(),
                viewModel.topicList?.get(position)?.title.orEmpty(),
                false
            )
        else
            CarouselFragment.newInstance(viewModel.url, viewModel.title)

    override fun initTabList() {
        tabList = if (!viewModel.topicList.isNullOrEmpty()) {
            binding.tabLayout.apply {
                tabGravity = TabLayout.GRAVITY_CENTER
                tabMode = TabLayout.MODE_SCROLLABLE
            }
            viewModel.topicList?.map { it.title } ?: listOf("")
        } else {
            binding.tabLayout.isVisible = false
            listOf("")
        }
    }

    override fun onBackClick() {
        activity?.finish()
    }

    override fun initBar() {
        super.initBar()
        binding.collapsingToolbar.isTitleEnabled = false
        binding.toolBar.apply {
            title = viewModel.pageTitle ?: viewModel.title
        }
    }

}