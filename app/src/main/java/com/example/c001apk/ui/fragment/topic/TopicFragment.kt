package com.example.c001apk.ui.fragment.topic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.c001apk.R
import com.example.c001apk.databinding.FragmentTopicBinding
import com.example.c001apk.ui.fragment.topic.content.TopicContentFragment
import com.google.android.material.tabs.TabLayoutMediator

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class TopicFragment : Fragment() {

    private lateinit var binding: FragmentTopicBinding
    private val viewModel by lazy { ViewModelProvider(this)[TopicViewModel::class.java] }
    private var param2: String? = null

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.param1 = it.getString(ARG_PARAM1)!!
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTopicBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.tabList.isEmpty())
            getViewData()
        else
            initView(null)

        viewModel.topicLayoutLiveData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val data = result.getOrNull()
                if (data != null) {
                    if (viewModel.tabList.isEmpty()) {
                        binding.toolBar.apply {
                            title = viewModel.param1
                            subtitle = data.intro
                            //tooltipText = data.intro
                            setNavigationIcon(R.drawable.ic_back)
                            setNavigationOnClickListener {
                                requireActivity().finish()
                            }
                        }
                        for (element in data.tabList) {
                            viewModel.tabList.add(element.title)
                            viewModel.fragmentList.add(
                                TopicContentFragment.newInstance(
                                    element.url,
                                    element.title
                                )
                            )
                        }
                        var tabSelected = 0
                        for (element in data.tabList) {
                            if (data.selectedTab == element.pageName) break
                            else tabSelected++
                        }
                        initView(tabSelected)
                    }
                    binding.indicator.isIndeterminate = false
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

    }

    private fun initView(tabSelected: Int?) {
        binding.viewPager.offscreenPageLimit = viewModel.tabList.size
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int) = viewModel.fragmentList[position]
            override fun getItemCount() = viewModel.tabList.size
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = viewModel.tabList[position]
        }.attach()
        if (viewModel.isInit && tabSelected != null) {
            binding.viewPager.currentItem = tabSelected
            binding.tabLayout.getTabAt(tabSelected)!!.select()
            viewModel.isInit = false
        }

    }

    private fun getViewData() {
        binding.indicator.visibility = View.VISIBLE
        binding.indicator.isIndeterminate = true
        viewModel.tag = viewModel.param1
        viewModel.isNew = true
        viewModel.getTopicLayout()
    }


}