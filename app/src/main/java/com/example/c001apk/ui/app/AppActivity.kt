package com.example.c001apk.ui.app

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import com.example.c001apk.R
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.logic.model.HomeFeedResponse
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

    override fun getSavedData(savedInstanceState: Bundle?) {
        if (viewModel.appData == null) {
            viewModel.appData = if (Build.VERSION.SDK_INT >= 33)
                savedInstanceState?.getParcelable("appData", HomeFeedResponse.Data::class.java)
            else savedInstanceState?.getParcelable("appData")
        }
    }

    @SuppressLint("CommitTransaction")
    override fun beginTransaction() {
        if (supportFragmentManager.findFragmentById(R.id.fragmentContainer) == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, AppFragment())
                .commit()
        }
    }

    override fun fetchData() {
        viewModel.fetchAppInfo()
    }

    override fun initData() {
        if (viewModel.isInit) {
            viewModel.isInit = false
            if (viewModel.appData == null) {
                viewModel.activityState.value = LoadingState.Loading
            } else {
                viewModel.handleAppData()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        viewModel.appData?.let {
            outState.putParcelable("appData", it)
        }
        super.onSaveInstanceState(outState)
    }

}