package com.example.c001apk.ui.fragment

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.ThemeUtils
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.absinthe.libraries.utils.extensions.dp
import com.example.c001apk.R
import com.example.c001apk.adapter.MessageAdapter
import com.example.c001apk.databinding.FragmentMessageBinding
import com.example.c001apk.ui.activity.LoginActivity
import com.example.c001apk.ui.activity.MainActivity
import com.example.c001apk.ui.fragment.minterface.INavViewContainer
import com.example.c001apk.ui.fragment.minterface.IOnNotiLongClickListener
import com.example.c001apk.util.ActivityCollector
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.CookieUtil.atcommentme
import com.example.c001apk.util.CookieUtil.atme
import com.example.c001apk.util.CookieUtil.contacts_follow
import com.example.c001apk.util.CookieUtil.feedlike
import com.example.c001apk.util.CookieUtil.message
import com.example.c001apk.util.ImageUtil
import com.example.c001apk.util.IntentUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.view.LinearItemDecoration
import com.example.c001apk.view.StaggerMessItemDecoration
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.net.URLDecoder
import java.net.URLEncoder


class MessageFragment : BaseFragment<FragmentMessageBinding>(), IOnNotiLongClickListener {

    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var mAdapter: MessageAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var sLayoutManager: StaggeredGridLayoutManager

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.url = "/v6/notification/list"

        binding.clickToLogin.setOnClickListener {
            IntentUtil.startActivity<LoginActivity>(requireContext()) {
            }
        }

        initRefresh()
        initView()
        initScroll()
        initMenu()

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
            } else if (viewModel.isEnd) {
                mAdapter.setLoadState(viewModel.loadState, viewModel.errorMessage)
                mAdapter.notifyItemChanged(viewModel.messageList.size + 6)
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

        viewModel.profileDataLiveData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val data = result.getOrNull()
                if (data?.data != null) {
                    viewModel.countList.clear()
                    viewModel.countList.apply {
                        add(data.data.feed)
                        add(data.data.follow)
                        add(data.data.fans)
                    }
                    PrefManager.username = URLEncoder.encode(data.data.username, "UTF-8")
                    PrefManager.userAvatar = data.data.userAvatar
                    PrefManager.level = data.data.level
                    PrefManager.experience = data.data.experience.toString()
                    PrefManager.nextLevelExperience = data.data.nextLevelExperience.toString()
                    showProfile()

                    viewModel.isNew = true
                    viewModel.getMessage()
                } else {
                    viewModel.isEnd = true
                    viewModel.isRefreshing = false
                    viewModel.isLoadMore = false
                    binding.swipeRefresh.isRefreshing = false
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

        viewModel.messageData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val feed = result.getOrNull()
                if (feed != null) {
                    if (!feed.message.isNullOrEmpty()) {
                        viewModel.loadState = mAdapter.LOADING_ERROR
                        viewModel.errorMessage = feed.message
                        mAdapter.setLoadState(viewModel.loadState, viewModel.errorMessage)
                        viewModel.isEnd = true
                        viewModel.isLoadMore = false
                        viewModel.isRefreshing = false
                        binding.swipeRefresh.isRefreshing = false
                        mAdapter.notifyItemChanged(viewModel.messageList.size)
                        return@observe
                    } else if (!feed.data.isNullOrEmpty()) {
                        if (viewModel.isRefreshing)
                            viewModel.messageList.clear()
                        if (viewModel.isRefreshing || viewModel.isLoadMore) {
                            viewModel.listSize = viewModel.messageList.size
                            for (element in feed.data)
                                if (element.entityType == "notification")
                                    if (!BlackListUtil.checkUid(element.fromuid))
                                        viewModel.messageList.add(element)
                        }
                        viewModel.loadState = mAdapter.LOADING_COMPLETE
                        mAdapter.setLoadState(viewModel.loadState, null)
                    } else if (feed.data?.isEmpty() == true) {
                        if (viewModel.isRefreshing)
                            viewModel.messageList.clear()
                        viewModel.loadState = mAdapter.LOADING_END
                        mAdapter.setLoadState(viewModel.loadState, null)
                        viewModel.isEnd = true
                    }
                } else {
                    viewModel.loadState = mAdapter.LOADING_ERROR
                    viewModel.errorMessage = getString(R.string.loading_failed)
                    mAdapter.setLoadState(viewModel.loadState, viewModel.errorMessage)
                    viewModel.isEnd = true
                    result.exceptionOrNull()?.printStackTrace()
                }
                if (viewModel.isLoadMore)
                    if (viewModel.isEnd)
                        mAdapter.notifyItemChanged(viewModel.messageList.size + 6)
                    else
                        mAdapter.notifyItemRangeChanged(
                            viewModel.listSize + 6,
                            viewModel.messageList.size - viewModel.listSize + 1
                        )
                else
                    mAdapter.notifyDataSetChanged()
                viewModel.isLoadMore = false
                binding.swipeRefresh.isRefreshing = false
                viewModel.isRefreshing = false
            }
        }

        viewModel.checkLoginInfoData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isGetCheckLoginInfo) {
                viewModel.isGetCheckLoginInfo = false

                val response = result.getOrNull()
                response?.let {
                    response.body()?.let {
                        if (response.body()?.data?.token != null) {
                            val login = response.body()?.data!!
                            viewModel.messCountList.apply {
                                add(login.notifyCount.atme)
                                add(login.notifyCount.atcommentme)
                                add(login.notifyCount.feedlike)
                                add(login.notifyCount.contactsFollow)
                                add(login.notifyCount.message)
                            }
                        }
                    }
                }
            }
        }

        viewModel.postDeleteData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val response = result.getOrNull()
                if (response != null) {
                    if (response.data == "删除成功") {
                        Toast.makeText(requireContext(), response.data, Toast.LENGTH_SHORT).show()
                        viewModel.messageList.removeAt(viewModel.position - 6)
                        mAdapter.notifyItemRemoved(viewModel.position)
                    } else if (!response.message.isNullOrEmpty()) {
                        Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT)
                            .show()
                    }
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

                    if (viewModel.messageList.isNotEmpty() && !viewModel.isEnd && isAdded) {
                        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                            viewModel.lastVisibleItemPosition =
                                mLayoutManager.findLastVisibleItemPosition()
                        } else {
                            val positions = sLayoutManager.findLastVisibleItemPositions(null)
                            for (pos in positions) {
                                if (pos > viewModel.lastVisibleItemPosition) {
                                    viewModel.lastVisibleItemPosition = pos
                                }
                            }
                        }
                    }

                    if (viewModel.lastVisibleItemPosition == viewModel.messageList.size + 6
                        && !viewModel.isEnd && !viewModel.isRefreshing && !viewModel.isLoadMore
                    ) {
                        viewModel.page++
                        loadMore()
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (viewModel.messageList.isNotEmpty()) {
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
        viewModel.loadState = mAdapter.LOADING
        mAdapter.setLoadState(viewModel.loadState, null)
        mAdapter.notifyItemChanged(viewModel.messageList.size + 6)
        viewModel.isLoadMore = true
        viewModel.isNew = true
        viewModel.getMessage()
    }

    private fun initView() {
        mAdapter = MessageAdapter(
            requireContext(),
            viewModel.countList,
            viewModel.messCountList,
            viewModel.messageList
        )
        mLayoutManager = LinearLayoutManager(requireContext())
        mAdapter.setIOnNotiLongClickListener(this)
        sLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager =
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                    mLayoutManager
                else sLayoutManager
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                addItemDecoration(LinearItemDecoration(10.dp))
            else
                addItemDecoration(StaggerMessItemDecoration(10.dp))
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
            getData()
            if (PrefManager.isLogin) {
                viewModel.isGetCheckLoginInfo = true
                viewModel.messCountList.clear()
                viewModel.getCheckLoginInfo()
            }
        }
    }

    private fun getData() {
        if (PrefManager.isLogin) {
            binding.swipeRefresh.isRefreshing = true
            viewModel.uid = PrefManager.uid
            viewModel.isNew = true
            viewModel.page = 1
            viewModel.isEnd = false
            viewModel.isRefreshing = true
            viewModel.isLoadMore = false
            viewModel.getProfile()
        } else
            binding.swipeRefresh.isRefreshing = false

    }


    @SuppressLint("NotifyDataSetChanged")
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
                                viewModel.messageList.clear()
                                viewModel.isInit = true
                                viewModel.isEnd = false
                                mAdapter.notifyDataSetChanged()
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

    override fun onDeleteNoti(uname: String, id: String, position: Int) {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle("删除来自 $uname 的通知？")
            setNegativeButton(android.R.string.cancel, null)
            setPositiveButton(android.R.string.ok) { _, _ ->
                viewModel.url = "/v6/notification/delete"
                viewModel.isNew = true
                viewModel.position = position
                viewModel.deleteId = id
                viewModel.postDelete()
            }
            show()
        }
    }

    override fun onReload() {
        viewModel.isEnd = false
        loadMore()
    }

}