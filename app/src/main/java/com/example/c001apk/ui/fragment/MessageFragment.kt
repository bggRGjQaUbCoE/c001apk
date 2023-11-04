package com.example.c001apk.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.ThemeUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.c001apk.R
import com.example.c001apk.adapter.MessageAdapter
import com.example.c001apk.databinding.FragmentMessageBinding
import com.example.c001apk.ui.activity.LoginActivity
import com.example.c001apk.ui.activity.MainActivity
import com.example.c001apk.util.ActivityCollector
import com.example.c001apk.util.ImageShowUtil
import com.example.c001apk.util.LinearItemDecoration
import com.example.c001apk.util.PrefManager
import com.example.c001apk.view.AppBarStateChangeListener
import com.example.c001apk.viewmodel.AppViewModel
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.net.URLDecoder
import java.net.URLEncoder


class MessageFragment : Fragment() {

    private lateinit var binding: FragmentMessageBinding
    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }
    private lateinit var mAdapter: MessageAdapter
    private lateinit var mLayoutManager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.clickToLogin.setOnClickListener {
            startActivity(Intent(activity, LoginActivity::class.java))
        }

        initRefresh()
        initView()

        if (PrefManager.isLogin) {
            initMenu()
            binding.clickToLogin.visibility = View.GONE
            binding.titleProfile.visibility = View.VISIBLE
            binding.avatar.visibility = View.VISIBLE
            binding.name.visibility = View.VISIBLE
            binding.levelLayout.visibility = View.VISIBLE
            binding.progress.visibility = View.VISIBLE
            showProfile()
            if (viewModel.isInit) {
                viewModel.isInit = false
                getData()
            }
        } else {
            binding.clickToLogin.visibility = View.VISIBLE
            binding.titleProfile.visibility = View.INVISIBLE
            binding.avatar.visibility = View.INVISIBLE
            binding.name.visibility = View.INVISIBLE
            binding.levelLayout.visibility = View.INVISIBLE
            binding.progress.visibility = View.INVISIBLE
        }

        binding.appBar.addOnOffsetChangedListener(object : AppBarStateChangeListener() {
            override fun onStateChanged(appBarLayout: AppBarLayout?, state: State?) {
                if (state == State.EXPANDED) {
                    binding.titleProfile.visibility = View.GONE
                } else if (state == State.COLLAPSED) {
                    binding.titleProfile.visibility = View.VISIBLE
                } else {
                    binding.titleProfile.visibility = View.GONE
                }
            }
        })

        viewModel.profileDataLiveData.observe(viewLifecycleOwner) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val data = result.getOrNull()
                if (data != null) {
                    viewModel.countList.clear()
                    viewModel.countList.apply {
                        add(data.feed)
                        add(data.follow)
                        add(data.fans)
                    }
                    PrefManager.username = URLEncoder.encode(data.username, "UTF-8")
                    PrefManager.userAvatar = data.userAvatar
                    PrefManager.level = data.level
                    PrefManager.experience = data.experience.toString()
                    PrefManager.nextLevelExperience = data.nextLevelExperience.toString()
                    mAdapter.notifyDataSetChanged()
                    showProfile()
                    binding.swipeRefresh.isRefreshing = false
                } else {
                    binding.swipeRefresh.isRefreshing = false
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
        }

    }

    private fun initView() {
        val space = resources.getDimensionPixelSize(R.dimen.normal_space)
        mAdapter = MessageAdapter(requireContext(), viewModel.countList)
        mLayoutManager = LinearLayoutManager(activity)
        binding.recyclerView.apply {
            adapter = mAdapter
            binding.recyclerView.layoutManager = mLayoutManager
            if (itemDecorationCount == 0)
                addItemDecoration(LinearItemDecoration(space))
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
        }
    }

    private fun getData() {
        if (PrefManager.isLogin) {
            binding.swipeRefresh.isRefreshing = true
            viewModel.uid = PrefManager.uid
            viewModel.isNew = true
            viewModel.getProfile()
        } else
            binding.swipeRefresh.isRefreshing = false

    }


    @SuppressLint("NotifyDataSetChanged")
    private fun initMenu() {
        binding.toolBar.inflateMenu(R.menu.message_menu)
        binding.toolBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.logout -> {
                    MaterialAlertDialogBuilder(requireContext()).apply {
                        setTitle(R.string.logoutTitle)
                        setNegativeButton(android.R.string.cancel, null)
                        setPositiveButton(android.R.string.ok) { _, _ ->
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

    @SuppressLint("SetTextI18n")
    private fun showProfile() {
        binding.name.text = URLDecoder.decode(PrefManager.username, "UTF-8")
        binding.name1.text = URLDecoder.decode(PrefManager.username, "UTF-8")
        binding.level.text = "Lv.${PrefManager.level}"
        binding.exp.text = "${PrefManager.experience}/${PrefManager.nextLevelExperience}"
        binding.progress.max = PrefManager.nextLevelExperience.toInt()
        binding.progress.progress = PrefManager.experience.toInt()
        ImageShowUtil.showAvatar(binding.avatar, PrefManager.userAvatar)
        ImageShowUtil.showAvatar(binding.avatar1, PrefManager.userAvatar)
    }

}