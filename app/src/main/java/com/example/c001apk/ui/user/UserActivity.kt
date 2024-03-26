package com.example.c001apk.ui.user

import android.annotation.SuppressLint
import androidx.activity.viewModels
import com.example.c001apk.R
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.ui.base.BaseViewActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback


@AndroidEntryPoint
class UserActivity : BaseViewActivity<UserViewModel>() {

    override val viewModel by viewModels<UserViewModel>(
        extrasProducer = {
            defaultViewModelCreationExtras.withCreationCallback<UserViewModel.Factory> { factory ->
                factory.create(uid = intent.getStringExtra("id").orEmpty())
            }
        }
    )

    override fun initData() {
        if (viewModel.isAInit) {
            viewModel.isAInit = false
            viewModel.activityState.value = LoadingState.Loading
        }
    }

    @SuppressLint("CommitTransaction")
    override fun beginTransaction() {
        if (supportFragmentManager.findFragmentById(R.id.fragmentContainer) == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, UserPagerFragment())
                .commit()
        }
    }

    override fun fetchData() {
        viewModel.fetchUser()
    }

}