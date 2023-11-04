package com.example.c001apk.ui.fragment.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.c001apk.databinding.FragmentSearchResultBinding
import com.google.android.material.tabs.TabLayoutMediator

class SearchResultFragment : Fragment() {

    private lateinit var binding: FragmentSearchResultBinding
    private var keyWord: String = ""
    private val tabList = arrayOf("动态", "应用", "数码", "用户", "话题")
    private var fragmentList = ArrayList<Fragment>()

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
        arguments?.let {
            keyWord = it.getString("KEYWORD")!!
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

        initData()
        initView()

    }

    private fun initData() {
        fragmentList.run {
            add(SearchContentFragment.newInstance(keyWord, "feed"))
            add(SearchContentFragment.newInstance(keyWord, "apk"))
            add(SearchContentFragment.newInstance(keyWord, "product"))
            add(SearchContentFragment.newInstance(keyWord, "user"))
            add(SearchContentFragment.newInstance(keyWord, "feedTopic"))
        }
    }

    private fun initView() {
        binding.viewPager.offscreenPageLimit = tabList.size
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int) = fragmentList[position]
            override fun getItemCount() = tabList.size
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabList[position]
        }.attach()
    }

}