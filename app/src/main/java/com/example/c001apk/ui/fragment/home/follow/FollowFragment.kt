package com.example.c001apk.ui.fragment.home.follow

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.ThemeUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cc.shinichi.library.ImagePreview
import cc.shinichi.library.bean.ImageInfo
import com.example.c001apk.R
import com.example.c001apk.databinding.FragmentFollowBinding
import com.example.c001apk.ui.fragment.home.HomeFragment
import com.example.c001apk.ui.fragment.home.feed.HomeFeedAdapter
import com.example.c001apk.ui.fragment.minterface.IOnBottomClickContainer
import com.example.c001apk.ui.fragment.minterface.IOnBottomClickListener
import com.example.c001apk.ui.fragment.minterface.IOnFeedPicClickContainer
import com.example.c001apk.ui.fragment.minterface.IOnFeedPicClickListener
import com.example.c001apk.util.LinearItemDecoration
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FollowFragment : Fragment(), IOnBottomClickListener, IOnFeedPicClickListener {

    private lateinit var binding: FragmentFollowBinding
    private val viewModel by lazy { ViewModelProvider(this)[FollowViewModel::class.java] }
    private lateinit var mAdapter: HomeFeedAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private var firstCompletelyVisibleItemPosition = 0
    private var lastVisibleItemPosition = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFollowBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!viewModel.isInit) {
            initView()
            initData()
            initRefresh()
            initScroll()
        }

        viewModel.followFeedData.observe(viewLifecycleOwner) { result ->
            val feed = result.getOrNull()
            if (!feed.isNullOrEmpty()) {
                if (viewModel.isRefreshing)
                    viewModel.followFeedList.clear()
                if (viewModel.isRefreshing || viewModel.isLoadMore) {
                    for (element in feed) {
                        if (element.entityTemplate == "feed")
                            viewModel.followFeedList.add(element)
                    }
                    viewModel.lastItem = feed[feed.size - 1].entityId
                }
                mAdapter.notifyDataSetChanged()
                viewModel.isLoadMore = false
                viewModel.isRefreshing = false
                binding.swipeRefresh.isRefreshing = false
            } else {
                viewModel.isLoadMore = false
                viewModel.isRefreshing = false
                binding.swipeRefresh.isRefreshing = false
                //Toast.makeText(activity, "没有更多了", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
        }

    }

    private fun initView() {
        val space = resources.getDimensionPixelSize(R.dimen.normal_space)
        mAdapter = HomeFeedAdapter(requireActivity(), viewModel.followFeedList)
        mLayoutManager = LinearLayoutManager(activity)
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
            if (itemDecorationCount == 0)
                addItemDecoration(LinearItemDecoration(space))
        }
    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (lastVisibleItemPosition == viewModel.followFeedList.size - 1) {
                        viewModel.isLoadMore = true
                        viewModel.page++
                        viewModel.getFollowFeed()
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition()
                firstCompletelyVisibleItemPosition =
                    mLayoutManager.findFirstCompletelyVisibleItemPosition()
            }
        })
    }

    private fun initData() {
        if (viewModel.followFeedList.isEmpty())
            refreshData()
    }

    private fun refreshData() {
        binding.swipeRefresh.isRefreshing = true
        viewModel.page = 1
        viewModel.isRefreshing = true
        viewModel.isLoadMore = false
        lifecycleScope.launch {
            delay(500)
            viewModel.getFollowFeed()
        }
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
            refreshData()
        }
    }

    override fun onReturnTop() {
        if (HomeFragment.current == 0) {
            if (firstCompletelyVisibleItemPosition == 0)
                refreshData()
            else {
                binding.recyclerView.scrollToPosition(0)
                refreshData()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as IOnBottomClickContainer).controller = this
        IOnFeedPicClickContainer.controller = this

        if (viewModel.isInit) {
            viewModel.isInit = false
            initView()
            initData()
            initRefresh()
            initScroll()
        }

    }

    override fun onShowPic(position: Int, urlList: MutableList<ImageInfo>) {
        ImagePreview.instance
            .setContext(requireActivity())
            .setImageInfoList(urlList)
            .setIndex(position)
            .setShowCloseButton(true)
            .setEnableDragClose(true)
            .setEnableUpDragClose(true)
            .setFolderName("c001apk")
            .start()
    }

}