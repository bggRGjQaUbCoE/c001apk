package com.example.c001apk.ui.fragment.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.ThemeUtils
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.c001apk.R
import com.example.c001apk.adapter.AppAdapter
import com.example.c001apk.databinding.FragmentHomeFeedBinding
import com.example.c001apk.ui.fragment.minterface.IOnLikeClickListener
import com.example.c001apk.ui.fragment.minterface.IOnTabClickContainer
import com.example.c001apk.ui.fragment.minterface.IOnTabClickListener
import com.example.c001apk.util.TokenDeviceUtils
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.view.ninegridimageview.NineGridImageView
import com.example.c001apk.view.ninegridimageview.OnImageItemClickListener
import com.example.c001apk.view.ninegridimageview.indicator.CircleIndexIndicator
import com.example.c001apk.viewmodel.AppViewModel
import net.mikaelzero.mojito.Mojito
import net.mikaelzero.mojito.impl.DefaultPercentProgress
import net.mikaelzero.mojito.impl.SimpleMojitoViewCallback


class HomeFeedFragment : Fragment(), IOnLikeClickListener,
    OnImageItemClickListener, IOnTabClickListener {

    private lateinit var binding: FragmentHomeFeedBinding
    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var mAdapter: AppAdapter
    private lateinit var mLayoutManager: LinearLayoutManager

    companion object {
        @JvmStatic
        fun newInstance(type: String) =
            HomeFeedFragment().apply {
                arguments = Bundle().apply {
                    putString("TYPE", type)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.type = it.getString("TYPE")!!
        }
    }

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

        if (!viewModel.isInit) {
            initView()
            initData()
            initRefresh()
            initScroll()
        }

        viewModel.homeFeedData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val feed = result.getOrNull()
                if (!feed.isNullOrEmpty()) {
                    if (viewModel.isRefreshing) {
                        viewModel.homeFeedList.clear()
                        /*for (element in feed) {
                            if (element.entityTemplate == "feed") {
                                viewModel.firstItem = element.entityId
                                break
                            }
                        }*/
                    }
                    if (viewModel.isRefreshing || viewModel.isLoadMore) {
                        for (element in feed) {
                            if (element.entityType == "feed"
                                || element.entityTemplate == "iconMiniScrollCard"
                                || element.entityTemplate == "iconLinkGridCard"
                                || element.entityTemplate == "imageCarouselCard_1"
                                || element.entityTemplate == "imageTextScrollCard"
                                || element.entityTemplate == "refreshCard"
                            ) {
                                viewModel.homeFeedList.add(element)
                            }

                        }
                        //viewModel.lastItem = viewModel.homeFeedList[viewModel.homeFeedList.size - 1].entityId
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
                binding.indicator.visibility = View.GONE
            }
        }

        viewModel.homeRankingData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val feed = result.getOrNull()
                if (!feed.isNullOrEmpty()) {
                    if (viewModel.isRefreshing)
                        viewModel.homeFeedList.clear()
                    if (viewModel.isRefreshing || viewModel.isLoadMore) {
                        for (element in feed) {
                            if (element.entityType == "feed"
                                || element.entityTemplate == "iconMiniGridCard"
                                || element.entityTemplate == "iconLinkGridCard"
                            )
                                viewModel.homeFeedList.add(element)
                        }
                        //viewModel.lastItem = viewModel.homeFeedList[viewModel.homeFeedList.size - 1].entityId
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
                binding.indicator.visibility = View.GONE
            }
        }

        viewModel.followFeedData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val feed = result.getOrNull()
                if (!feed.isNullOrEmpty()) {
                    if (viewModel.isRefreshing)
                        viewModel.homeFeedList.clear()
                    if (viewModel.isRefreshing || viewModel.isLoadMore) {
                        for (element in feed) {
                            if (element.entityType == "feed")
                                viewModel.homeFeedList.add(element)
                            //viewModel.lastItem = feed[feed.size - 1].entityId
                        }
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
                binding.indicator.visibility = View.GONE
            }
        }

        viewModel.likeFeedData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isPostLikeFeed) {
                viewModel.isPostLikeFeed = false

                val response = result.getOrNull()
                if (response != null) {
                    if (response.data != null) {
                        viewModel.homeFeedList[viewModel.likePosition].likenum =
                            response.data.count
                        viewModel.homeFeedList[viewModel.likePosition].userAction?.like = 1
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
                        viewModel.homeFeedList[viewModel.likePosition].likenum =
                            response.data.count
                        viewModel.homeFeedList[viewModel.likePosition].userAction?.like = 0
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
                    if (viewModel.lastVisibleItemPosition == viewModel.homeFeedList.size
                        && !viewModel.isEnd
                    ) {
                        mAdapter.setLoadState(mAdapter.LOADING)
                        viewModel.isLoadMore = true
                        viewModel.page++
                        viewModel.firstLaunch = 0
                        viewModel.isNew = true
                        when (viewModel.type) {
                            "feed" -> viewModel.getHomeFeed()
                            "rank" -> viewModel.getHomeRanking()
                            "follow" -> viewModel.getFollowFeed()
                        }

                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (viewModel.homeFeedList.isNotEmpty()) {
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
                requireContext(),
                rikka.preference.simplemenu.R.attr.colorPrimary
            )
        )
        binding.swipeRefresh.setOnRefreshListener {
            binding.indicator.isIndeterminate = false
            binding.indicator.visibility = View.GONE
            refreshData()
        }
    }

    private fun initData() {
        if (viewModel.homeFeedList.isEmpty()) {
            binding.indicator.isIndeterminate = true
            binding.indicator.visibility = View.VISIBLE
            refreshData()
        }
    }

    private fun refreshData() {
        viewModel.isEnd = false
        viewModel.page = 1
        viewModel.firstLaunch = 0
        viewModel.isRefreshing = true
        viewModel.isLoadMore = false
        viewModel.installTime = TokenDeviceUtils.getLastingInstallTime(requireContext())
        viewModel.isNew = true
        when (viewModel.type) {
            "feed" -> viewModel.getHomeFeed()
            "rank" -> viewModel.getHomeRanking()
            "follow" -> viewModel.getFollowFeed()
        }
    }

    private fun initView() {
        val space = resources.getDimensionPixelSize(R.dimen.normal_space)
        mAdapter = AppAdapter(
            requireContext(), viewModel.homeFeedList
        )
        mLayoutManager = LinearLayoutManager(activity)
        mAdapter.setIOnLikeReplyListener(this)
        mAdapter.setOnImageItemClickListener(this)
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
            if (itemDecorationCount == 0)
                addItemDecoration(LinearItemDecoration(space))
        }
    }

    override fun onReturnTop(position: Int) {
        val current = when (viewModel.type) {
            "feed" -> 2
            "rank" -> 3
            "follow" -> 0
            else -> throw IllegalArgumentException("type error")
        }
        if (current == position) {
            if (viewModel.firstCompletelyVisibleItemPosition == 0) {
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

        (requireParentFragment() as IOnTabClickContainer).controller = this

        if (viewModel.isInit) {
            viewModel.isInit = false
            initView()
            initData()
            initRefresh()
            initScroll()
        }
    }

    override fun onPostLike(type: String?, isLike: Boolean, id: String, position: Int?) {
        viewModel.likeFeedId = id
        viewModel.likePosition = position!!
        if (isLike) {
            viewModel.isPostUnLikeFeed = true
            viewModel.postUnLikeFeed()
        } else {
            viewModel.isPostLikeFeed = true
            viewModel.postLikeFeed()
        }

    }

    override fun onClick(
        nineGridView: NineGridImageView,
        imageView: ImageView,
        urlList: List<String>,
        position: Int
    ) {
        val imgList: MutableList<String> = ArrayList()
        for (img in urlList) {
            if (img.substring(img.length - 6, img.length) == ".s.jpg")
                imgList.add(img.replace(".s.jpg", ""))
            else
                imgList.add(img)
        }
        Mojito.start(imageView.context) {
            urls(imgList)
            position(position)
            progressLoader {
                DefaultPercentProgress()
            }
            setIndicator(CircleIndexIndicator())
            views(nineGridView.getImageViews().toTypedArray())
            setOnMojitoListener(object : SimpleMojitoViewCallback() {
                override fun onStartAnim(position: Int) {
                    nineGridView.getImageViewAt(position)?.apply {
                        postDelayed(200) {
                            this.visibility = View.GONE
                        }
                    }
                }

                override fun onMojitoViewFinish(pagePosition: Int) {
                    nineGridView.getImageViews().forEach {
                        it.visibility = View.VISIBLE
                    }
                }

                override fun onViewPageSelected(position: Int) {
                    nineGridView.getImageViews().forEachIndexed { index, imageView ->
                        if (position == index) {
                            imageView.visibility = View.GONE
                        } else {
                            imageView.visibility = View.VISIBLE
                        }
                    }
                }
            })
        }
    }

}