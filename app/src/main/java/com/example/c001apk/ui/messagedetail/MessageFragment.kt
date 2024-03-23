package com.example.c001apk.ui.messagedetail

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.c001apk.ui.base.BasePagerFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MessageFragment : BasePagerFragment() {

    private val type by lazy { arguments?.getString("type").orEmpty() }

    companion object {
        @JvmStatic
        fun newInstance(type: String) =
            MessageFragment().apply {
                arguments = Bundle().apply {
                    putString("type", type)
                }
            }
    }

    override fun getFragment(position: Int): Fragment = MessageContentFragment.newInstance(type)

    override fun initTabList() {
        binding.tabLayout.isVisible = false
        tabList = listOf("")
    }

    override fun onBackClick() {
        activity?.finish()
    }

    override fun initBar() {
        super.initBar()
        binding.collapsingToolbar.isTitleEnabled = false
        binding.toolBar.title = when (type) {
            "atMe" -> "@我的动态"

            "atCommentMe" -> "@我的评论"


            "feedLike" -> "我收到的赞"


            "contactsFollow" -> "好友关注"

            "list" -> "私信"

            else -> ""
        }
    }


}