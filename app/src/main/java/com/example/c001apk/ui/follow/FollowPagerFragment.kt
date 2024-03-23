package com.example.c001apk.ui.follow

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.c001apk.ui.base.BasePagerFragment
import com.example.c001apk.util.PrefManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FollowPagerFragment : BasePagerFragment() {

    private val uid by lazy { arguments?.getString("uid").orEmpty() }
    private val type by lazy { arguments?.getString("type").orEmpty() }

    companion object {
        @JvmStatic
        fun newInstance(uid: String, type: String) =
            FollowPagerFragment().apply {
                arguments = Bundle().apply {
                    putString("uid", uid)
                    putString("type", type)
                }
            }
    }

    override fun getFragment(position: Int): Fragment =
        when (type) {
            "follow" -> when (position) {
                0 -> FollowFragment.newInstance(type = "follow")
                1 -> FollowFragment.newInstance(type = "topic")
                2 -> FollowFragment.newInstance(type = "product")
                3 -> FollowFragment.newInstance(type = "apk")
                else -> throw IllegalArgumentException()
            }

            "reply" -> when (position) {
                0 -> FollowFragment.newInstance(type = "reply")
                1 -> FollowFragment.newInstance(type = "replyToMe")
                else -> throw IllegalArgumentException()
            }

            else -> FollowFragment.newInstance(uid, type)
        }

    override fun initTabList() {
        tabList =
            when (type) {
                "follow" -> listOf("用户", "话题", "数码", "应用")
                "reply" -> listOf("我的回复", "我收到的回复")
                else -> {
                    binding.tabLayout.isVisible = false
                    listOf("")
                }
            }
    }

    override fun onBackClick() {
        activity?.finish()
    }

    override fun initBar() {
        super.initBar()
        binding.collapsingToolbar.isTitleEnabled = false
        binding.toolBar.title = when (type) {
            "feed" -> "我的动态"

            "follow" -> {
                if (uid == PrefManager.uid)
                    "我的关注"
                else
                    "TA关注的人"
            }

            "fans" -> {
                if (uid == PrefManager.uid)
                    "关注我的人"
                else
                    "TA的粉丝"
            }

            "like" -> "我的赞"

            "reply" -> "我的回复"

            "recentHistory" -> "我的常去"

            else -> type

        }
    }

}