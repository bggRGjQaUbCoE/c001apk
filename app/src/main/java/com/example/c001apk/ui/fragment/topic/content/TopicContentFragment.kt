package com.example.c001apk.ui.fragment.topic.content

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.ThemeUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.databinding.FragmentTopicContentBinding
import com.example.c001apk.util.LinearItemDecoration

private const val URL = "url"
private const val TITLE = "title"

class TopicContentFragment : Fragment(), IOnLikeClickListener {

    private lateinit var binding: FragmentTopicContentBinding
    private val viewModel by lazy { ViewModelProvider(this)[TopicContentViewModel::class.java] }
    private lateinit var url: String
    private lateinit var title: String
    private lateinit var mAdapter: TopicContentAdapter
    private lateinit var mLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            url = it.getString(URL)!!
            title = it.getString(TITLE)!!
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TopicContentFragment().apply {
                arguments = Bundle().apply {
                    putString(URL, param1)
                    putString(TITLE, param2)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTopicContentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()

        if (viewModel.isInit) {
            viewModel.isInit = false
            initData()
            initView()
            initRefresh()
            initScroll()
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!viewModel.isInit) {
            initData()
            initView()
            initRefresh()
            initScroll()
        }

        viewModel.topicDataLiveData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val data = result.getOrNull()
                if (!data.isNullOrEmpty()) {
                    if (viewModel.isRefreshing)
                        viewModel.topicDataList.clear()
                    if (viewModel.isRefreshing || viewModel.isLoadMore)
                        for (element in data)
                            if (element.entityTemplate == "feed"
                                || element.entityType == "topic"
                                || element.entityType == "product"
                                || element.entityTemplate == "articleNews"
                                )
                                viewModel.topicDataList.add(element)
                    mAdapter.notifyDataSetChanged()
                    mAdapter.setLoadState(mAdapter.LOADING_COMPLETE)
                } else {
                    mAdapter.setLoadState(mAdapter.LOADING_END)
                    viewModel.isEnd = true
                    result.exceptionOrNull()?.printStackTrace()
                }
                binding.indicator.isIndeterminate = false
                binding.indicator.visibility = View.GONE
                viewModel.isLoadMore = false
                viewModel.isRefreshing = false
                binding.swipeRefresh.isRefreshing = false
            }
        }

        viewModel.likeFeedData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isPostLikeFeed) {
                viewModel.isPostLikeFeed = false

                val response = result.getOrNull()
                if (response != null) {
                    if (response.data != null) {
                        viewModel.topicDataList[viewModel.likePosition].likenum =
                            response.data.count
                        viewModel.topicDataList[viewModel.likePosition].userAction?.like = 1
                        mAdapter.notifyDataSetChanged()
                    } else
                        Toast.makeText(activity, response.message, Toast.LENGTH_SHORT).show()
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

        viewModel.unLikeFeedData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isPostUnLikeFeed) {
                viewModel.isPostUnLikeFeed = false

                val response = result.getOrNull()
                if (response != null) {
                    if (response.data != null) {
                        viewModel.topicDataList[viewModel.likePosition].likenum =
                            response.data.count
                        viewModel.topicDataList[viewModel.likePosition].userAction?.like = 0
                        mAdapter.notifyDataSetChanged()
                    } else
                        Toast.makeText(activity, response.message, Toast.LENGTH_SHORT).show()
                } else {
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (viewModel.lastVisibleItemPosition == viewModel.topicDataList.size
                        && !viewModel.isEnd
                    ) {
                        mAdapter.setLoadState(mAdapter.LOADING)
                        viewModel.isLoadMore = true
                        viewModel.page++
                        viewModel.isNew = true
                        viewModel.getTopicData()
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (viewModel.topicDataList.isNotEmpty()) {
                    viewModel.lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition()
                    viewModel.firstCompletelyVisibleItemPosition =
                        mLayoutManager.findFirstCompletelyVisibleItemPosition()
                }
            }
        })
    }

    @SuppressLint("RestrictedApi")
    private fun initRefresh() {
        binding.swipeRefresh.setColorSchemeColors(
            ThemeUtils.getThemeAttrColor(
                requireActivity(),
                rikka.preference.simplemenu.R.attr.colorPrimary
            )
        )
        binding.swipeRefresh.setOnRefreshListener {
            binding.indicator.isIndeterminate = false
            binding.indicator.visibility = View.GONE
            refreshData()
        }
    }

    private fun initView() {
        val space = resources.getDimensionPixelSize(R.dimen.normal_space)
        mAdapter = TopicContentAdapter(requireActivity(), viewModel.topicDataList)
        mAdapter.setIOnLikeReplyListener(this)
        mLayoutManager = LinearLayoutManager(activity)
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
            if (itemDecorationCount == 0)
                addItemDecoration(LinearItemDecoration(space))
        }
    }

    private fun initData() {
        if (viewModel.topicDataList.isEmpty()) {
            binding.indicator.visibility = View.VISIBLE
            binding.indicator.isIndeterminate = true
            refreshData()
        }
    }

    private fun refreshData() {
        viewModel.isEnd = false
        viewModel.isRefreshing = true
        viewModel.isLoadMore = false
        viewModel.url = url
        viewModel.title = title
        viewModel.isNew = true
        viewModel.getTopicData()
    }

    override fun onPostLike(isLike: Boolean, id: String, position: Int) {
        viewModel.likeFeedId = id
        viewModel.likePosition = position
        if (isLike) {
            viewModel.isPostUnLikeFeed = true
            viewModel.postUnLikeFeed()
        } else {
            viewModel.isPostLikeFeed = true
            viewModel.postLikeFeed()
        }
    }

}