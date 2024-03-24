package com.example.c001apk.ui.app

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.example.c001apk.R
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.constant.Constants
import com.example.c001apk.databinding.BaseFragmentContainerBinding
import com.example.c001apk.ui.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback

@AndroidEntryPoint
class AppActivity : BaseActivity<BaseFragmentContainerBinding>() {

    private val viewModel by viewModels<AppViewModel>(
        extrasProducer = {
            defaultViewModelCreationExtras.withCreationCallback<AppViewModel.Factory> { factory ->
                factory.create(id = intent.getStringExtra("id") ?: "com.coolapk.market")
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initData()
        initObserve()
        initError()

    }

    private fun initError() {
        binding.errorLayout.retry.setOnClickListener {
            binding.errorLayout.parent.isVisible = false
            viewModel.activityState.value = LoadingState.Loading
        }
    }

    private fun initObserve() {
        viewModel.activityState.observe(this) {
            when (it) {
                LoadingState.Loading -> {
                    binding.indicator.parent.isIndeterminate = true
                    binding.indicator.parent.isVisible = true
                    viewModel.fetchAppInfo()
                }

                LoadingState.LoadingDone -> {
                    if (!viewModel.tabList.isNullOrEmpty()
                        || !viewModel.errMsg.isNullOrEmpty()
                    ) {
                        beginTransaction()
                    }
                }

                is LoadingState.LoadingError -> {
                    binding.errorMessage.errMsg.apply {
                        text = it.errMsg
                        isVisible = true
                    }
                }

                is LoadingState.LoadingFailed -> {
                    binding.errorLayout.apply {
                        msg.text = it.msg
                        retry.text =
                            if (it.msg == Constants.LOADING_EMPTY) getString(R.string.refresh)
                            else getString(R.string.retry)
                        parent.isVisible = true
                    }
                }
            }
            if (it !is LoadingState.Loading) {
                binding.indicator.parent.isIndeterminate = false
                binding.indicator.parent.isVisible = false
            }
        }

    }

    @SuppressLint("CommitTransaction")
    private fun beginTransaction() {
        if (supportFragmentManager.findFragmentById(R.id.fragmentContainer) == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragmentContainer, AppFragment()
                )
                .commit()
        }
    }


    private fun initData() {
        if (viewModel.isInit) {
            viewModel.isInit = false
            viewModel.activityState.value = LoadingState.Loading
        }
    }

}