package com.example.c001apk.ui.message

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.R
import com.example.c001apk.adapter.FooterAdapter
import com.example.c001apk.adapter.HeaderAdapter
import com.example.c001apk.adapter.ItemListener
import com.example.c001apk.databinding.FragmentMessageBinding
import com.example.c001apk.ui.base.BaseFragment
import com.example.c001apk.ui.login.LoginActivity
import com.example.c001apk.ui.main.INavViewContainer
import com.example.c001apk.ui.main.MainActivity
import com.example.c001apk.util.ActivityCollector
import com.example.c001apk.util.CookieUtil.atcommentme
import com.example.c001apk.util.CookieUtil.atme
import com.example.c001apk.util.CookieUtil.contacts_follow
import com.example.c001apk.util.CookieUtil.feedlike
import com.example.c001apk.util.CookieUtil.message
import com.example.c001apk.util.Event
import com.example.c001apk.util.ImageUtil
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.Utils.getColorFromAttr
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.view.StaggerMessItemDecoration
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLDecoder

@AndroidEntryPoint
class MessageFragment : BaseFragment<FragmentMessageBinding>() {

    private val viewModel by lazy { ViewModelProvider(this)[MessageViewModel::class.java] }
    private val messageFirstAdapter by lazy { MessageFirstAdapter() }
    private val messageSecondAdapter by lazy { MessageSecondAdapter() }
    private val messageThirdAdapter by lazy { MessageThirdAdapter() }
    private lateinit var mAdapter: MessageAdapter
    private lateinit var footerAdapter: FooterAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var sLayoutManager: StaggeredGridLayoutManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.clickToLogin.setOnClickListener {
            IntentUtil.startActivity<LoginActivity>(requireContext()) {
            }
        }

        initView()
        initScroll()
        initRefresh()
        initMenu()
        initObserve()

        if (PrefManager.isLogin) {
            binding.clickToLogin.visibility = View.GONE
            binding.avatar.visibility = View.VISIBLE
            binding.name.visibility = View.VISIBLE
            binding.levelLayout.visibility = View.VISIBLE
            binding.progress.visibility = View.VISIBLE
            showProfile()
            if (viewModel.isInit) {
                viewModel.isInit = false
                getData()
            } else {
                if (viewModel.countList.isNotEmpty())
                    messageFirstAdapter.setFFFList(viewModel.countList)
                if (viewModel.messCountList.isNotEmpty())
                    messageThirdAdapter.setBadgeList(viewModel.messCountList)
            }
            viewModel.messCountList.apply {
                add(atme)
                add(atcommentme)
                add(feedlike)
                add(contacts_follow)
                add(message)
            }
        } else {
            binding.clickToLogin.visibility = View.VISIBLE
            binding.avatar.visibility = View.INVISIBLE
            binding.name.visibility = View.INVISIBLE
            binding.levelLayout.visibility = View.INVISIBLE
            binding.progress.visibility = View.INVISIBLE
        }
    }

    private fun initObserve() {
        viewModel.toastText.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.doWhat.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandledOrReturnNull()?.let {
                when (it) {
                    "showProfile" -> showProfile()

                    "isRefreshing" -> binding.swipeRefresh.isRefreshing = false

                    "countList" -> messageFirstAdapter.setFFFList(viewModel.countList)

                    "messCountList" -> messageThirdAdapter.setBadgeList(viewModel.messCountList)
                }
            }
        }

        viewModel.changeState.observe(viewLifecycleOwner) {
            footerAdapter.setLoadState(it.first, it.second)
            footerAdapter.notifyItemChanged(0)
            if (it.first != FooterAdapter.LoadState.LOADING) {
                binding.swipeRefresh.isRefreshing = false
                viewModel.isLoadMore = false
                viewModel.isRefreshing = false
            }
        }

        viewModel.messageData.observe(viewLifecycleOwner) {
            viewModel.listSize = it.size
            mAdapter.submitList(it)
        }
    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {

                    if (viewModel.listSize != -1 && !viewModel.isEnd && isAdded) {
                        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                            viewModel.lastVisibleItemPosition =
                                mLayoutManager.findLastVisibleItemPosition()
                        } else {
                            val positions = sLayoutManager.findLastVisibleItemPositions(null)
                            viewModel.lastVisibleItemPosition = positions[0]
                            for (pos in positions) {
                                if (pos > viewModel.lastVisibleItemPosition) {
                                    viewModel.lastVisibleItemPosition = pos
                                }
                            }
                        }
                    }

                    if (viewModel.lastVisibleItemPosition == viewModel.listSize + 7
                        && !viewModel.isEnd && !viewModel.isRefreshing && !viewModel.isLoadMore
                        && !binding.swipeRefresh.isShown
                    ) {
                        viewModel.page++
                        loadMore()
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (viewModel.listSize != -1) {
                    if (dy > 0) {
                        (activity as? INavViewContainer)?.hideNavigationView()
                    } else if (dy < 0) {
                        (activity as? INavViewContainer)?.showNavigationView()
                    }
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
        messageThirdAdapter.setBadgeList(listOf(atme, atcommentme, feedlike, contacts_follow))
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
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    mLayoutManager = LinearLayoutManager(requireContext())
                    mLayoutManager
                } else {
                    sLayoutManager =
                        StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                    sLayoutManager
                }
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                addItemDecoration(LinearItemDecoration(10.dp))
            else
                addItemDecoration(StaggerMessItemDecoration(10.dp))
        }
    }

    private fun initRefresh() {
        binding.swipeRefresh.setColorSchemeColors(
            requireContext().getColorFromAttr(
                rikka.preference.simplemenu.R.attr.colorPrimary
            )
        )
        binding.swipeRefresh.setOnRefreshListener {
            getData()
            if (PrefManager.isLogin) {
                viewModel.lastItem = null
                viewModel.messCountList.clear()
                viewModel.countList.clear()
                viewModel.fetchCheckLoginInfo()
            }
        }
    }

    private fun getData() {
        if (PrefManager.isLogin) {
            binding.swipeRefresh.isRefreshing = true
            viewModel.uid = PrefManager.uid
            viewModel.page = 1
            viewModel.isEnd = false
            viewModel.isRefreshing = true
            viewModel.isLoadMore = false
            viewModel.fetchProfile()
        } else
            binding.swipeRefresh.isRefreshing = false
    }


    private fun initMenu() {
        binding.toolBar.apply {
            inflateMenu(R.menu.message_menu)
            menu.findItem(R.id.logout).isVisible = PrefManager.isLogin
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.logout -> {
                        MaterialAlertDialogBuilder(requireContext()).apply {
                            setTitle(R.string.logoutTitle)
                            setNegativeButton(android.R.string.cancel, null)
                            setPositiveButton(android.R.string.ok) { _, _ ->
                                viewModel.countList.clear()
                                viewModel.messCountList.clear()
                                viewModel.doWhat.postValue(Event("countList"))
                                viewModel.doWhat.postValue(Event("messCountList"))
                                viewModel.messageData.postValue(emptyList())
                                viewModel.isInit = true
                                viewModel.isEnd = false
                                PrefManager.isLogin = false
                                PrefManager.uid = ""
                                PrefManager.username = ""
                                PrefManager.token = ""
                                PrefManager.userAvatar = ""
                                ActivityCollector.recreateActivity(MainActivity::class.java.name)
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
        binding.progress.max =
            if (PrefManager.nextLevelExperience != "" && PrefManager.nextLevelExperience != "null") PrefManager.nextLevelExperience.toInt()
            else -1
        binding.progress.progress =
            if (PrefManager.experience != "" && PrefManager.experience != "null") PrefManager.experience.toInt()
            else -1
        if (PrefManager.userAvatar.isNotEmpty())
            ImageUtil.showIMG(binding.avatar, PrefManager.userAvatar)
    }

    inner class ReloadListener : FooterAdapter.FooterListener {
        override fun onReLoad() {
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
                    id.toString(), uid.toString(), username.toString(), userAvatar.toString(),
                    deviceTitle.toString(), message.toString(), dateline.toString()
                )
        }

        override fun onBlockUser(id: String, uid: String, position: Int) {
            viewModel.saveUid(uid)
            val currentList = viewModel.messageData.value!!.toMutableList()
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