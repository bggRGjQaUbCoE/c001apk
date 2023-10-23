package com.example.c001apk.ui.fragment.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.c001apk.databinding.FragmentSettingsBinding
import com.example.c001apk.ui.activity.login.LoginActivity
import com.example.c001apk.util.AppBarStateChangeListener
import com.example.c001apk.util.ImageShowUtil
import com.example.c001apk.util.PrefManager
import com.google.android.material.appbar.AppBarLayout

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private val viewModel by lazy { ViewModelProvider(this)[SettingsViewModel::class.java] }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (PrefManager.isLogin) {
            binding.clickToLogin.visibility = View.GONE
            viewModel.getProfile()
        } else
            binding.clickToLogin.visibility = View.VISIBLE

        binding.clickToLogin.setOnClickListener {
            startActivity(Intent(activity, LoginActivity::class.java))
        }

        binding.appBar.addOnOffsetChangedListener(object : AppBarStateChangeListener() {
            override fun onStateChanged(appBarLayout: AppBarLayout?, state: State?) {
                if (state == State.EXPANDED) {
                    binding.titleProfile.visibility = View.GONE
                    binding.title.visibility = View.VISIBLE
                } else if (state == State.COLLAPSED) {
                    binding.titleProfile.visibility = View.VISIBLE
                    if (PrefManager.isLogin)
                        binding.title.visibility = View.GONE
                } else {
                    binding.titleProfile.visibility = View.GONE
                    if (PrefManager.isLogin)
                        binding.title.visibility = View.GONE
                }
            }
        })

        viewModel.profileDataLiveData.observe(viewLifecycleOwner) { result ->
            val data = result.getOrNull()
            if (data != null) {
                binding.name.text = data.username
                binding.name1.text = data.username
                binding.level.text = "Lv.${data.level}"
                ImageShowUtil.showAvatar(binding.avatar, data.userAvatar)
                ImageShowUtil.showAvatar(binding.avatar1, data.userAvatar)
                binding.exp.text = "${data.experience}/${data.nextLevelExperience}"
                binding.progress.max = data.nextLevelExperience
                binding.progress.progress = data.experience
                binding.progress.visibility = View.VISIBLE
            } else {
                result.exceptionOrNull()?.printStackTrace()
            }
        }

    }

}