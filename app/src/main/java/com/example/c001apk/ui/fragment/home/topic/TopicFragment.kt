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
import com.example.c001apk.ui.fragment.minterface.INavViewContainer
import com.example.c001apk.view.vertical.adapter.TabAdapter
import com.example.c001apk.view.vertical.widget.ITabView
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.R

class TopicFragment : Fragment() {

    private lateinit var binding: FragmentHomeTopicBinding
    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.type = it.getString("TYPE")!!
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(type: String) =
            TopicFragment().apply {
                arguments = Bundle().apply {
                    putString("TYPE", type)
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

        binding.tabLayout.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            val contentView: View = binding.tabLayout.getChildAt(0)
            if (oldScrollY in 1..<scrollY) {
                (activity as INavViewContainer).hideNavigationView()
            } else if (scrollY < oldScrollY
                && contentView.measuredHeight > (binding.tabLayout.scrollY + binding.tabLayout.height)
            ) {
                (activity as INavViewContainer).showNavigationView()
            }
        }

        viewModel.dataListData.observe(viewLifecycleOwner) { result ->
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

        viewModel.productCategoryData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val data = result.getOrNull()
                if (!data.isNullOrEmpty()) {
                    if (viewModel.titleList.isEmpty()) {
                        for (element in data) {
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
            if (viewModel.type == "topic") {
                viewModel.url = "/page?url=V11_VERTICAL_TOPIC"
                viewModel.title = "话题"
                viewModel.getDataList()
            } else
                viewModel.getProductList()
        }
    }

    private fun initView() {
        binding.viewPager.adapter = MyPagerAdapter(childFragmentManager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)
        binding.indicator.isIndeterminate = false
        binding.indicator.visibility = View.GONE
        binding.topicLayout.visibility = View.VISIBLE
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