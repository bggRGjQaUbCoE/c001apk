package com.example.c001apk.ui.dyh

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.c001apk.ui.base.BasePagerFragment
import com.example.c001apk.ui.home.IOnTabClickContainer
import com.example.c001apk.ui.home.IOnTabClickListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DyhFragment : BasePagerFragment(), IOnTabClickContainer {

    private val id by lazy { arguments?.getString("id").orEmpty() }
    private val title by lazy { arguments?.getString("title").orEmpty() }
    override var tabController: IOnTabClickListener? = null
    private val typeList = listOf("all", "square")

    companion object {
        @JvmStatic
        fun newInstance(id: String, title: String) =
            DyhFragment().apply {
                arguments = Bundle().apply {
                    putString("id", id)
                    putString("title", title)
                }
            }
    }

    override fun getFragment(position: Int): Fragment =
        DyhDetailFragment.newInstance(id, typeList[position])

    override fun initTabList() {
        tabList = listOf("精选", "广场")
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