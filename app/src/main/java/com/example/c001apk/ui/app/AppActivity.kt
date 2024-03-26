package com.example.c001apk.ui.app

import android.annotation.SuppressLint
import androidx.activity.viewModels
import com.example.c001apk.R
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.ui.base.BaseViewActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback

@AndroidEntryPoint
class AppActivity : BaseViewActivity<AppViewModel>() {

    override val viewModel by viewModels<AppViewModel>(
        extrasProducer = {
            defaultViewModelCreationExtras.withCreationCallback<AppViewModel.Factory> { factory ->
                factory.create(id = intent.getStringExtra("id") ?: "com.coolapk.market")
            }
        }
    )

    @SuppressLint("CommitTransaction")
    override fun beginTransaction() {
        if (supportFragmentManager.findFragmentById(R.id.fragmentContainer) == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragmentContainer, AppFragment()
                )
                .commit()
        }
    }

    override fun fetchData() {
        viewModel.fetchAppInfo()
    }

    override fun initData() {
        if (viewModel.isInit) {
            viewModel.isInit = false
            viewModel.activityState.value = LoadingState.Loading
        }
    }

}