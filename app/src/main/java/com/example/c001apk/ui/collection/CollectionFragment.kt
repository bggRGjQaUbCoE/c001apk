package com.example.c001apk.ui.collection

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.c001apk.ui.base.BasePagerFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CollectionFragment : BasePagerFragment() {

    private val id by lazy { arguments?.getString("id") }
    private val title by lazy { arguments?.getString("title").orEmpty() }

    companion object {
        @JvmStatic
        fun newInstance(id: String?, title: String) =
            CollectionFragment().apply {
                arguments = Bundle().apply {
                    putString("id", id)
                    putString("title", title)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBar()
    }

    override fun getFragment(position: Int): Fragment =
        CollectionContentFragment.newInstance(
            if (id.isNullOrEmpty()) "/v6/collection/list"
            else "/v6/collection/itemList",
            id
        )


    override fun initTabList() {
        binding.tabLayout.isVisible = false
        tabList = listOf("")
    }

    override fun initBar() {
        super.initBar()
        binding.collapsingToolbar.isTitleEnabled = false
        binding.toolBar.title = title.ifEmpty { "我的收藏单" }
    }

    override fun onBackClick() {
        if (id.isNullOrEmpty())
            activity?.finish()
        else
            activity?.supportFragmentManager?.popBackStack()
    }

}