package com.example.c001apk.ui.fragment.home.topic

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.ThemeUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import com.example.c001apk.databinding.FragmentHomeTopicBinding
import com.example.c001apk.view.vertical.adapter.TabAdapter
import com.example.c001apk.view.vertical.widget.ITabView
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.R

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class TopicFragment : Fragment() {

    private lateinit var binding: FragmentHomeTopicBinding
    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TopicFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeTopicBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()

        if (viewModel.isInit) {
            viewModel.isInit = false
            initData()
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!viewModel.isInit)
            initView()

        viewModel.homeTopicTitleLiveData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val topic = result.getOrNull()
                if (!topic.isNullOrEmpty()) {
                    if (viewModel.titleList.isEmpty()) {
                        for (element in topic[0].entities) {
                            viewModel.titleList.add(element.title)
                            viewModel.fragmentList.add(
                                HomeTopicContentFragment.newInstance(
                                    element.url,
                                    element.title
                                )
                            )
                        }
                        initView()
                    }
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

    }


    private fun initData() {
        if (viewModel.homeTopicTitleList.isEmpty()) {
            binding.indicator.isIndeterminate = true
            binding.indicator.visibility = View.VISIBLE
            viewModel.isNew = true
            viewModel.getHomeTopicTitle()
        }
    }

    private fun initView() {
        //binding.viewPager.setNoScroll(false)
        binding.viewPager.adapter = MyPagerAdapter(childFragmentManager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)
        binding.indicator.isIndeterminate = false
        binding.indicator.visibility = View.GONE
        binding.topicLayout.visibility = View.VISIBLE
        //binding.tabLayout.getTabAt(1).isSelected = true
        //binding.viewPager.currentItem = 1
    }


    private inner class MyPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm), TabAdapter {

        override fun getItem(position: Int): Fragment {
            return viewModel.fragmentList[position]
        }

        override fun getCount(): Int {
            return viewModel.fragmentList.size
        }

        override fun getBadge(position: Int): ITabView.TabBadge? {
            return null
        }

        override fun getIcon(position: Int): ITabView.TabIcon? {
            return null
        }

        @SuppressLint("RestrictedApi")
        override fun getTitle(position: Int): ITabView.TabTitle {
            return ITabView.TabTitle.Builder()
                .setContent(viewModel.titleList[position])
                .setTextColor(
                    ThemeUtils.getThemeAttrColor(
                        requireContext(),
                        R.attr.colorPrimary
                    ), ThemeUtils.getThemeAttrColor(
                        requireContext(),
                        R.attr.colorControlNormal
                    )
                )
                .build()
        }

        /*override fun getPageTitle(position: Int): CharSequence {
            return titleList[position]
        }*/

        override fun getBackground(position: Int): Int {
            return -1
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {}

    }


}