package com.example.c001apk.ui.topic

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
class TopicActivity : BaseActivity<BaseFragmentContainerBinding>() {

    private val viewModel by viewModels<TopicViewModel>(
        extrasProducer = {
            defaultViewModelCreationExtras.withCreationCallback<TopicViewModel.Factory> { factory ->
                factory.create(
                    intent.getStringExtra("url").orEmpty(),
                    intent.getStringExtra("title").orEmpty(),
                    intent.getStringExtra("id").orEmpty(),
                    intent.getStringExtra("type").orEmpty(),
                )
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
            viewModel.loadingState.value = LoadingState.Loading
        }
    }

    private fun initObserve() {
        viewModel.loadingState.observe(this) {
            when (it) {
                LoadingState.Loading -> {
                    binding.indicator.parent.isIndeterminate = true
                    binding.indicator.parent.isVisible = true
                    if (viewModel.type == "topic") {
                        viewModel.url = viewModel.url.replace("/t/", "")
                        viewModel.fetchTopicLayout()
                    } else if (viewModel.type == "product") {
                        viewModel.fetchProductLayout()
                    }
                }

                LoadingState.LoadingDone -> {
                    beginTransaction()
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
                        retry.text = if (it.msg == Constants.LOADING_EMPTY) getString(R.string.refresh)
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
                    R.id.fragmentContainer, TopicFragment()
                )
                .commit()
        }
    }

    private fun initData() {
        if (viewModel.isInit) {
            viewModel.isInit = false
            viewModel.loadingState.value = LoadingState.Loading
        }
    }

}