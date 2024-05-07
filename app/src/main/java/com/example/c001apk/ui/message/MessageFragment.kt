package com.example.c001apk.ui.message

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.c001apk.R
import com.example.c001apk.adapter.FooterAdapter
import com.example.c001apk.adapter.FooterState
import com.example.c001apk.adapter.HeaderAdapter
import com.example.c001apk.adapter.ItemListener
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.adapter.PlaceHolderAdapter
import com.example.c001apk.databinding.FragmentMessageBinding
import com.example.c001apk.ui.base.BaseFragment
import com.example.c001apk.ui.login.LoginActivity
import com.example.c001apk.ui.main.MainActivity
import com.example.c001apk.util.CookieUtil
import com.example.c001apk.util.CookieUtil.atcommentme
import com.example.c001apk.util.CookieUtil.atme
import com.example.c001apk.util.CookieUtil.contacts_follow
import com.example.c001apk.util.CookieUtil.feedlike
import com.example.c001apk.util.ImageUtil
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.dp
import com.example.c001apk.util.setSpaceFooterView
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.view.MessStaggerItemDecoration
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLDecoder

@AndroidEntryPoint
class MessageFragment : BaseFragment<FragmentMessageBinding>() {

    private val viewModel by viewModels<MessageViewModel>()
    private val messageFirstAdapter by lazy { MessageFirstAdapter() }
    private val messageSecondAdapter by lazy { MessageSecondAdapter() }
    private val messageThirdAdapter by lazy { MessageThirdAdapter() }
    private lateinit var mAdapter: MessageAdapter
    private lateinit var footerAdapter: FooterAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var sLayoutManager: StaggeredGridLayoutManager
    private val placeHolderAdapter by lazy { PlaceHolderAdapter() }
    private val isPortrait by lazy { resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT }
    private val isLogin by lazy { PrefManager.isLogin }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!viewModel.isInit) {
            initView()
            initLogin()
            initScroll()
            initRefresh()
            initObserve()
        }

    }

    private fun initLogin() {
        binding.isLogin = isLogin
        if (isLogin) {
            viewModel.messCountList.value = true
            if (viewModel.initLogin) {
                viewModel.initLogin = false
                showProfile()
                getData()
            }
            initMenu()
        } else {
            binding.clickToLogin.setOnClickListener {
                IntentUtil.startActivity<LoginActivity>(requireContext()) {}
            }
        }
    }

    private fun initObserve() {
        viewModel.countList.observe(viewLifecycleOwner) {
            messageFirstAdapter.setFFFList(it)
        }

        viewModel.messCountList.observe(viewLifecycleOwner) {
            messageThirdAdapter.updateBadge()
        }

        viewModel.toastText.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                (activity as? MainActivity)?.showNavigationView()
            }
        }

        viewModel.loadingState.observe(viewLifecycleOwner) {
            when (it) {
                LoadingState.Loading -> {}
                LoadingState.LoadingDone -> {
                    showProfile()
                }

                is LoadingState.LoadingError -> {}
                is LoadingState.LoadingFailed -> {
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }

        viewModel.footerState.observe(viewLifecycleOwner) {
            footerAdapter.setLoadState(it)
            if (it !is FooterState.Loading) {
                binding.swipeRefresh.isRefreshing = false
            }
        }

        viewModel.messageData.observe(viewLifecycleOwner) {
            viewModel.listSize = it.size
            mAdapter.submitList(it)
            binding.vfContainer.displayedChild = it.size
        }
    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    lastVisibleItemPosition = if (isPortrait)
                        mLayoutManager.findLastVisibleItemPosition()
                    else
                        sLayoutManager.findLastVisibleItemPositions(null).max()

                    if (lastVisibleItemPosition + 1 == binding.recyclerView.adapter?.itemCount
                        && !viewModel.isEnd && !viewModel.isRefreshing && !viewModel.isLoadMore
                        && !binding.swipeRefresh.isRefreshing && isLogin
                    ) {
                        loadMore()
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    (activity as? MainActivity)?.hideNavigationView()
                } else if (dy < 0) {
                    (activity as? MainActivity)?.showNavigationView()
                }
            }
        })
    }

    private fun loadMore() {
        viewModel.isLoadMore = true
        viewModel.fetchMessage()
    }

    private fun initView() {
        mAdapter = MessageAdapter(ItemClickListener())
        footerAdapter = FooterAdapter(ReloadListener())
        binding.recyclerView.apply {
            adapter = ConcatAdapter(
                HeaderAdapter(),
                messageFirstAdapter,
                messageSecondAdapter,
                messageThirdAdapter,
                mAdapter,
                footerAdapter
            )
            layoutManager =
                if (isPortrait) {
                    mLayoutManager = LinearLayoutManager(requireContext())
                    mLayoutManager
                } else {
                    sLayoutManager =
                        StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                    sLayoutManager
                }
            if (isPortrait)
                addItemDecoration(LinearItemDecoration(10.dp))
            else
                addItemDecoration(MessStaggerItemDecoration(10.dp))
        }
        binding.vfContainer.setOnDisplayedChildChangedListener {
            binding.recyclerView.setSpaceFooterView(placeHolderAdapter)
        }
    }

    private fun initRefresh() {
        binding.swipeRefresh.apply {
            setColorSchemeColors(
                MaterialColors.getColor(
                    requireContext(),
                    com.google.android.material.R.attr.colorPrimary,
                    0
                )
            )
            setOnRefreshListener {
                if (isLogin) {
                    if (!viewModel.isLoadMore) {
                        binding.swipeRefresh.isRefreshing = true
                        getData()
                    }
                } else
                    binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun getData() {
        viewModel.lastItem = null
        viewModel.page = 1
        viewModel.isEnd = false
        viewModel.isRefreshing = true
        viewModel.isLoadMore = false
        viewModel.onCheckCount()
        viewModel.fetchProfile()
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.isInit) {
            viewModel.isInit = false
            initView()
            initLogin()
            initScroll()
            initRefresh()
            initObserve()
        }
        if (!viewModel.isInit && isLogin) {
            if (CookieUtil.badge != 0) {
                CookieUtil.badge = 0
                viewModel.messCountList.value = true
                if (CookieUtil.notification != 0) {
                    CookieUtil.notification = 0
                    viewModel.refreshMessage()
                }
            }
        }
    }

    private fun initMenu() {
        binding.toolBar.apply {
            inflateMenu(R.menu.message_menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.logout -> {
                        MaterialAlertDialogBuilder(requireContext()).apply {
                            setTitle(R.string.logoutTitle)
                            setNegativeButton(android.R.string.cancel, null)
                            setPositiveButton(android.R.string.ok) { _, _ ->
                                viewModel.countList.value = emptyList()
                                atme = null
                                atcommentme = null
                                feedlike = null
                                contacts_follow = null
                                viewModel.messCountList.value = true
                                viewModel.footerState.value = FooterState.LoadingDone
                                viewModel.messageData.postValue(emptyList())
                                viewModel.isInit = true
                                viewModel.isEnd = false
                                PrefManager.isLogin = false
                                PrefManager.uid = ""
                                PrefManager.username = ""
                                PrefManager.token = ""
                                PrefManager.userAvatar = ""
                                (requireActivity() as? MainActivity)?.recreate()
                            }
                            show()
                        }
                    }
                }
                return@setOnMenuItemClickListener true
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showProfile() {
        binding.name.text = URLDecoder.decode(PrefManager.username, "UTF-8")
        binding.level.text = "Lv.${PrefManager.level}"
        binding.exp.text = "${PrefManager.experience}/${PrefManager.nextLevelExperience}"
        binding.progress.max = PrefManager.nextLevelExperience.toIntOrNull() ?: -1
        binding.progress.progress = PrefManager.experience.toIntOrNull() ?: -1
        if (PrefManager.userAvatar.isNotEmpty())
            ImageUtil.showIMG(binding.avatar, PrefManager.userAvatar)
    }

    inner class ReloadListener : FooterAdapter.FooterListener {
        override fun onReLoad() {
            viewModel.isEnd = false
            loadMore()
        }
    }

    inner class ItemClickListener : ItemListener {
        override fun onViewFeed(
            view: View,
            id: String?,
            uid: String?,
            username: String?,
            userAvatar: String?,
            deviceTitle: String?,
            message: String?,
            dateline: String?,
            rid: Any?,
            isViewReply: Any?
        ) {
            super.onViewFeed(
                view,
                id,
                uid,
                username,
                userAvatar,
                deviceTitle,
                message,
                dateline,
                rid,
                isViewReply
            )
            if (!uid.isNullOrEmpty() && PrefManager.isRecordHistory)
                viewModel.saveHistory(
                    id.toString(),
                    uid.toString(),
                    username.toString(),
                    userAvatar.toString(),
                    deviceTitle.toString(),
                    message.toString(),
                    dateline.toString()
                )
        }

        override fun onBlockUser(id: String, uid: String, position: Int) {
            viewModel.saveUid(uid)
            val currentList = viewModel.messageData.value?.toMutableList() ?: ArrayList()
            currentList.removeAt(position)
            viewModel.messageData.postValue(currentList)
        }

        override fun onMessLongClicked(uname: String, id: String, position: Int): Boolean {
            MaterialAlertDialogBuilder(requireContext()).apply {
                setTitle("删除来自 $uname 的通知？")
                setNegativeButton(android.R.string.cancel, null)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    viewModel.onPostDelete(position, id)
                }
                show()
            }
            return true
        }
    }

}