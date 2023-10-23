package com.example.c001apk.ui.fragment.meaasge

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
import com.example.c001apk.databinding.FragmentMessageBinding
import com.example.c001apk.ui.activity.MainActivity
import com.example.c001apk.ui.activity.login.LoginActivity
import com.example.c001apk.util.ActivityCollector
import com.example.c001apk.util.AppBarStateChangeListener
import com.example.c001apk.util.ImageShowUtil
import com.example.c001apk.util.LinearItemDecoration
import com.example.c001apk.util.PrefManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class MessageFragment : Fragment() {

    private lateinit var binding: FragmentMessageBinding
    private val viewModel by lazy { ViewModelProvider(this)[MessageViewModel::class.java] }
    private val countList = ArrayList<String>()
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
            showProfile()
            viewModel.getProfile()
        } else {
            binding.progress.visibility = View.GONE
            binding.clickToLogin.visibility = View.VISIBLE
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
            val data = result.getOrNull()
            if (data != null) {
                countList.apply {
                    add(data.feed)
                    add(data.follow)
                    add(data.fans)
                }
                mAdapter.notifyDataSetChanged()

                PrefManager.apply {
                    name = "username=${data.username}"
                    userAvatar = data.userAvatar
                    level = data.level
                    experience = data.experience.toString()
                    nextLevelExperience = data.nextLevelExperience.toString()
                }
                showProfile()
                binding.swipeRefresh.isRefreshing = false
            } else {
                binding.swipeRefresh.isRefreshing = false
                result.exceptionOrNull()?.printStackTrace()
            }
        }

    }

    private fun initView() {
        val space = resources.getDimensionPixelSize(R.dimen.normal_space)
        mAdapter = MessageAdapter(requireActivity(), countList)
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
                requireActivity(),
                rikka.preference.simplemenu.R.attr.colorPrimary
            )
        )
        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = true
            viewModel.getProfile()
        }
    }

    private fun initMenu() {
        binding.toolBar.inflateMenu(R.menu.message_menu)
        binding.toolBar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.logout -> {
                    MaterialAlertDialogBuilder(requireContext()).apply {
                        setTitle(R.string.logoutTitle)
                        setNegativeButton(android.R.string.cancel, null)
                        setPositiveButton(android.R.string.ok) { _, _ ->
                            PrefManager.apply {
                                isLogin = false
                                uid = ""
                                name = ""
                                token = ""
                            }
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
        binding.name.text = PrefManager.name.substring(9, PrefManager.name.length)
        binding.name1.text = PrefManager.name.substring(9, PrefManager.name.length)
        binding.level.text = "Lv.${PrefManager.level}"
        binding.exp.text = "${PrefManager.experience}/${PrefManager.nextLevelExperience}"
        binding.progress.max = PrefManager.nextLevelExperience.toInt()
        binding.progress.progress = PrefManager.experience.toInt()
        binding.progress.visibility = View.VISIBLE
        ImageShowUtil.showAvatar(binding.avatar, PrefManager.userAvatar)
        ImageShowUtil.showAvatar(binding.avatar1, PrefManager.userAvatar)
    }


}