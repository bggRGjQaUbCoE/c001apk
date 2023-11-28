package com.example.c001apk.ui.fragment.feed

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.Toast
import androidx.appcompat.widget.ThemeUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.c001apk.R
import com.example.c001apk.adapter.FeedContentAdapter
import com.example.c001apk.databinding.FragmentFeedVoteBinding
import com.example.c001apk.logic.database.FeedFavoriteDatabase
import com.example.c001apk.logic.model.FeedFavorite
import com.example.c001apk.ui.activity.WebViewActivity
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.ClipboardUtil
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.ToastUtil
import com.example.c001apk.view.VoteItemDecoration
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FeedVoteFragment : Fragment() {

    private lateinit var binding: FragmentFeedVoteBinding
    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var mAdapter: FeedContentAdapter
    private lateinit var mLayoutManager: StaggeredGridLayoutManager
    private lateinit var objectAnimator: ObjectAnimator
    private val feedFavoriteDao by lazy {
        FeedFavoriteDatabase.getDatabase(this@FeedVoteFragment.requireContext()).feedFavoriteDao()
    }

    private fun initAnimator() {
        objectAnimator = ObjectAnimator.ofFloat(binding.titleProfile, "translationY", 120f, 0f)
        objectAnimator.interpolator = AccelerateInterpolator()
        objectAnimator.duration = 150
    }

    companion object {
        @JvmStatic
        fun newInstance(id: String) =
            FeedVoteFragment().apply {
                arguments = Bundle().apply {
                    putString("ID", id)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.id = it.getString("ID", "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedVoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAnimator()
        initBar()
        initView()
        initData()
        initRefresh()
        //initScroll()

        viewModel.feedData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val feed = result.getOrNull()
                if (feed?.message != null) {
                    viewModel.errorMessage = feed.message
                    binding.indicator.isIndeterminate = false
                    binding.indicator.visibility = View.GONE
                    showErrorMessage()
                    return@observe
                } else if (feed?.data != null) {
                    viewModel.uid = feed.data.uid
                    viewModel.funame = feed.data.userInfo?.username.toString()
                    viewModel.avatar = feed.data.userAvatar
                    viewModel.device = feed.data.deviceTitle
                    viewModel.replyCount = feed.data.replynum
                    viewModel.dateLine = feed.data.dateline
                    viewModel.feedTypeName = feed.data.feedTypeName
                    viewModel.feedType = feed.data.feedType
                    viewModel.totalOptionNum = feed.data.vote!!.totalOptionNum
                    binding.toolBar.title = viewModel.feedTypeName
                    if (viewModel.isRefreshing) {
                        viewModel.feedContentList.clear()
                        viewModel.isRefreshing = true
                        viewModel.isNew = true

                        if (viewModel.totalOptionNum == 2) {
                            viewModel.extraKey = feed.data.vote.options[viewModel.currentOption].id
                        }
                        viewModel.getVoteComment()

                    }
                    if (viewModel.isRefreshing || viewModel.isLoadMore) {
                        viewModel.feedContentList.add(feed)
                    }
                } else {
                    viewModel.isEnd = true
                    viewModel.isLoadMore = false
                    viewModel.isRefreshing = false
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(activity, "加载失败", Toast.LENGTH_SHORT).show()
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

        viewModel.voteCommentData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val voteComment = result.getOrNull()
                if (voteComment?.data?.isNotEmpty() == true) {
                    if (viewModel.isRefreshing) {
                        viewModel.leftVoteCommentList.clear()
                        viewModel.rightVoteCommentList.clear()
                        viewModel.voteCommentList.clear()
                    }
                    if (viewModel.totalOptionNum == 2 && viewModel.currentOption == 0) {
                        viewModel.leftVoteCommentList.addAll(voteComment.data)
                        viewModel.currentOption++
                        viewModel.extraKey =
                            viewModel.feedContentList[0].data!!.vote!!.options[viewModel.currentOption].id
                        viewModel.isNew = true
                        viewModel.getVoteComment()
                    } else if (viewModel.totalOptionNum == 2 && viewModel.currentOption == 1) {
                        viewModel.rightVoteCommentList.addAll(voteComment.data)
                        if (viewModel.leftVoteCommentList.isNotEmpty() && viewModel.rightVoteCommentList.isNotEmpty()) {
                            if (viewModel.leftVoteCommentList.size >= viewModel.rightVoteCommentList.size) {
                                for (index in 0 until viewModel.rightVoteCommentList.size) {
                                    viewModel.voteCommentList.add(viewModel.leftVoteCommentList[index])
                                    viewModel.voteCommentList.add(viewModel.rightVoteCommentList[index])
                                }
                                viewModel.voteCommentList.addAll(
                                    viewModel.leftVoteCommentList.subList(
                                        viewModel.rightVoteCommentList.size,
                                        viewModel.leftVoteCommentList.size
                                    )
                                )
                            } else {
                                for (index in 0 until viewModel.leftVoteCommentList.size) {
                                    viewModel.voteCommentList.add(viewModel.leftVoteCommentList[index])
                                    viewModel.voteCommentList.add(viewModel.rightVoteCommentList[index])
                                }
                                viewModel.voteCommentList.addAll(
                                    viewModel.rightVoteCommentList.subList(
                                        viewModel.leftVoteCommentList.size,
                                        viewModel.rightVoteCommentList.size
                                    )
                                )
                            }
                        } else if (viewModel.leftVoteCommentList.isNotEmpty())
                            viewModel.voteCommentList = viewModel.leftVoteCommentList
                        else if (viewModel.rightVoteCommentList.isNotEmpty())
                            viewModel.voteCommentList = viewModel.rightVoteCommentList
                        mAdapter.setLoadState(mAdapter.LOADING_COMPLETE, null)
                        mAdapter.notifyDataSetChanged()
                        binding.indicator.isIndeterminate = false
                        binding.indicator.visibility = View.GONE
                        binding.contentLayout.visibility = View.VISIBLE
                    } else {
                        viewModel.voteCommentList.addAll(voteComment.data)
                        mAdapter.setLoadState(mAdapter.LOADING_COMPLETE, null)
                        mAdapter.notifyDataSetChanged()
                        binding.indicator.isIndeterminate = false
                        binding.indicator.visibility = View.GONE
                        binding.contentLayout.visibility = View.VISIBLE
                    }
                } else {
                    viewModel.isEnd = true
                    viewModel.isLoadMore = false
                    viewModel.isRefreshing = false
                    binding.swipeRefresh.isRefreshing = false
                    result.exceptionOrNull()?.printStackTrace()
                }

            }
        }

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

    private fun initBar() {
        binding.toolBar.apply {
            title = viewModel.feedTypeName
            setNavigationIcon(R.drawable.ic_back)
            setNavigationOnClickListener {
                requireActivity().finish()
            }
            setOnClickListener {
                binding.recyclerView.stopScroll()
                binding.titleProfile.visibility = View.GONE
                mLayoutManager.scrollToPositionWithOffset(0, 0)
            }
            inflateMenu(R.menu.feed_menu)
            val favorite = menu.findItem(R.id.favorite)
            CoroutineScope(Dispatchers.IO).launch {
                if (feedFavoriteDao.isFavorite(viewModel.id)) {
                    withContext(Dispatchers.Main) {
                        favorite.title = "取消收藏"
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        favorite.title = "收藏"
                    }
                }
            }
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.showReply -> {
                        binding.recyclerView.stopScroll()
                        if (viewModel.firstVisibleItemPosition <= 0)
                            mLayoutManager.scrollToPositionWithOffset(1, 0)
                        else {
                            binding.titleProfile.visibility = View.GONE
                            mLayoutManager.scrollToPositionWithOffset(0, 0)
                        }
                    }

                    R.id.block -> {
                        MaterialAlertDialogBuilder(requireContext()).apply {
                            setTitle("确定将 ${viewModel.funame} 加入黑名单？")
                            setNegativeButton(android.R.string.cancel, null)
                            setPositiveButton(android.R.string.ok) { _, _ ->
                                BlackListUtil.saveUid(viewModel.uid)
                                //requireActivity().finish()
                            }
                            show()
                        }
                    }

                    R.id.share -> {
                        IntentUtil.shareText(
                            requireContext(),
                            "https://www.coolapk1s.com/feed/${viewModel.id}"
                        )
                    }

                    R.id.copyLink -> {
                        ClipboardUtil.copyText(
                            requireContext(),
                            "https://www.coolapk1s.com/feed/${viewModel.id}"
                        )
                    }

                    R.id.report -> {
                        val intent = Intent(requireContext(), WebViewActivity::class.java)
                        intent.putExtra(
                            "url",
                            "https://m.coolapk.com/mp/do?c=feed&m=report&type=feed&id=${viewModel.id}"
                        )
                        requireContext().startActivity(intent)
                    }


                    R.id.favorite -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            if (feedFavoriteDao.isFavorite(viewModel.id)) {
                                feedFavoriteDao.delete(viewModel.id)
                                withContext(Dispatchers.Main) {
                                    favorite.title = "收藏"
                                    ToastUtil.toast("已取消收藏")
                                }
                            } else {
                                try {
                                    val fav = FeedFavorite(
                                        viewModel.id,
                                        viewModel.uid,
                                        viewModel.funame,
                                        viewModel.avatar,
                                        viewModel.device,
                                        viewModel.feedContentList[0].data?.message.toString(), // 还未加载完会空指针
                                        viewModel.feedContentList[0].data?.dateline.toString()
                                    )
                                    feedFavoriteDao.insert(fav)
                                    withContext(Dispatchers.Main) {
                                        favorite.title = "取消收藏"
                                        ToastUtil.toast("已收藏")
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    ToastUtil.toast("请稍后再试")
                                }
                            }

                        }
                    }
                }
                return@setOnMenuItemClickListener true
            }
        }
    }

    private fun showErrorMessage() {
        binding.errorMessage.visibility = View.VISIBLE
        binding.errorMessage.text = viewModel.errorMessage
    }

    private fun initData() {
        if (viewModel.feedContentList.isEmpty()) {
            if (objectAnimator.isRunning) {
                objectAnimator.cancel()
            }
            binding.titleProfile.visibility = View.GONE
            binding.indicator.visibility = View.VISIBLE
            binding.indicator.isIndeterminate = true
            refreshData()
        }
    }

    private fun refreshData() {
        viewModel.page = 1
        viewModel.isEnd = false
        viewModel.isRefreshing = true
        viewModel.isLoadMore = false
        viewModel.isNew = true
        viewModel.getFeed()
    }

    private fun initView() {
        binding.tabLayout.visibility = View.GONE
        val space = resources.getDimensionPixelSize(R.dimen.normal_space)
        mAdapter = FeedContentAdapter(
            requireContext(),
            viewModel.feedContentList,
            viewModel.voteCommentList
        )
        mLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
            addItemDecoration(VoteItemDecoration(space))
        }
    }

}