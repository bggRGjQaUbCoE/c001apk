package com.example.c001apk.ui.fragment.home.feed

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
import com.example.c001apk.databinding.FragmentHomeFeedBinding
import com.example.c001apk.ui.fragment.home.HomeFragment
import com.example.c001apk.ui.fragment.minterface.IOnBottomClickContainer
import com.example.c001apk.ui.fragment.minterface.IOnBottomClickListener
import com.example.c001apk.util.LinearItemDecoration
import com.example.c001apk.util.TokenDeviceUtils

class HomeFeedFragment : Fragment(), IOnBottomClickListener, IOnLikeClickListener {

    private lateinit var binding: FragmentHomeFeedBinding
    private val viewModel by lazy { ViewModelProvider(this)[HomeFeedViewModel::class.java] }
    private lateinit var mAdapter: HomeFeedAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private var firstCompletelyVisibleItemPosition = -1
    private var lastVisibleItemPosition = -1
    private var likePosition = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initData()
        initRefresh()
        initScroll()

        viewModel.homeFeedData.observe(viewLifecycleOwner) { result ->
            val feed = result.getOrNull()
            if (!feed.isNullOrEmpty()) {
                if (viewModel.isRefreshing)
                    viewModel.homeFeedList.clear()
                if (viewModel.isRefreshing || viewModel.isLoadMore) {
                    for (element in feed) {
                        if (element.entityTemplate == "feed"
                            || element.entityTemplate == "iconMiniScrollCard"
                            || element.entityTemplate == "iconLinkGridCard"
                            || element.entityTemplate == "imageCarouselCard_1"
                            || element.entityTemplate == "imageTextScrollCard"
                        )
                            viewModel.homeFeedList.add(element)
                    }
                    viewModel.lastItem = feed[feed.size - 1].entityId
                }
                mAdapter.notifyDataSetChanged()
                mAdapter.setLoadState(mAdapter.LOADING_COMPLETE)
            } else {
                mAdapter.setLoadState(mAdapter.LOADING_END)
                viewModel.isEnd = true
                result.exceptionOrNull()?.printStackTrace()
            }
            viewModel.isLoadMore = false
            viewModel.isRefreshing = false
            binding.swipeRefresh.isRefreshing = false
            binding.indicator.isIndeterminate = false
        }

        viewModel.likeFeedData.observe(viewLifecycleOwner) { result ->
            val response = result.getOrNull()
            if (response != null) {
                if (response.data != null) {
                    viewModel.homeFeedList[likePosition].likenum = response.data.count
                    viewModel.homeFeedList[likePosition].userAction.like = 1
                    mAdapter.notifyDataSetChanged()
                } else
                    Toast.makeText(activity, response.message, Toast.LENGTH_SHORT).show()
            } else {
                result.exceptionOrNull()?.printStackTrace()
            }
        }

        viewModel.unLikeFeedData.observe(viewLifecycleOwner) { result ->
            val response = result.getOrNull()
            if (response != null) {
                if (response.data != null) {
                    viewModel.homeFeedList[likePosition].likenum = response.data.count
                    viewModel.homeFeedList[likePosition].userAction.like = 0
                    mAdapter.notifyDataSetChanged()
                } else
                    Toast.makeText(activity, response.message, Toast.LENGTH_SHORT).show()
            } else {
                result.exceptionOrNull()?.printStackTrace()
            }
        }

    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (lastVisibleItemPosition == viewModel.homeFeedList.size) {
                        if (!viewModel.isEnd) {
                            mAdapter.setLoadState(mAdapter.LOADING)
                            viewModel.isLoadMore = true
                            viewModel.page++
                            viewModel.firstLaunch = 0
                            viewModel.getHomeFeed()
                        }
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (viewModel.homeFeedList.isNotEmpty()) {
                    lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition()
                    firstCompletelyVisibleItemPosition =
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
            refreshData()
        }
    }

    private fun initData() {
        if (viewModel.homeFeedList.isEmpty())
            refreshData()
    }

    private fun refreshData() {
        viewModel.isEnd = false
        viewModel.page = 1
        viewModel.firstLaunch = 1
        viewModel.isRefreshing = true
        viewModel.isLoadMore = false
        viewModel.installTime = TokenDeviceUtils.getLastingInstallTime(requireActivity())
        viewModel.getHomeFeed()
    }

    private fun initView() {
        val space = resources.getDimensionPixelSize(R.dimen.normal_space)
        mAdapter = HomeFeedAdapter(requireActivity(), viewModel.homeFeedList)
        mLayoutManager = LinearLayoutManager(activity)
        mAdapter.setIOnLikeReplyListener(this)
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
            if (itemDecorationCount == 0)
                addItemDecoration(LinearItemDecoration(space))
        }
    }

    override fun onReturnTop() {
        if (HomeFragment.current == 2) {
            if (firstCompletelyVisibleItemPosition == 0) {
                binding.swipeRefresh.isRefreshing = true
                refreshData()
            } else {
                binding.recyclerView.scrollToPosition(0)
                binding.swipeRefresh.isRefreshing = true
                refreshData()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as IOnBottomClickContainer).controller = this
    }

    override fun onPostLike(isLike: Boolean, id: String, position: Int) {
        viewModel.likeFeedId = id
        this.likePosition = position
        if (isLike)
            viewModel.postUnLikeFeed()
        else
            viewModel.postLikeFeed()
    }

}