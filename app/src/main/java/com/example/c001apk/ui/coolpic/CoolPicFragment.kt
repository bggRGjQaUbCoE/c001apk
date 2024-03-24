package com.example.c001apk.ui.coolpic

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.c001apk.ui.base.BasePagerFragment
import com.example.c001apk.ui.collection.CollectionContentFragment
import com.example.c001apk.ui.home.IOnTabClickContainer
import com.example.c001apk.ui.home.IOnTabClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CoolPicFragment : BasePagerFragment(), IOnTabClickContainer {

    private val title by lazy { arguments?.getString("title").orEmpty() }
    override var tabController: IOnTabClickListener? = null
    private val typeList = listOf("recommend", "hot", "newest")

    companion object {
        @JvmStatic
        fun newInstance(title: String) =
            CoolPicFragment().apply {
                arguments = Bundle().apply {
                    putString("title", title)
                }
            }
    }

    override fun getFragment(position: Int): Fragment =
        CollectionContentFragment.newInstance(
            "/v6/picture/list?tag=$title&type=${typeList[position]}", null
        )

    override fun initTabList() {
        tabList = listOf("精选", "热门", "最新")
    }

    override fun onBackClick() {
        activity?.finish()
    }

    override fun initBar() {
        super.initBar()
        binding.collapsingToolbar.isTitleEnabled = false
        binding.toolBar.title = title
    }

}