package com.example.c001apk.ui.topic

import android.annotation.SuppressLint
import androidx.activity.viewModels
import com.example.c001apk.R
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.ui.base.BaseViewActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback

@AndroidEntryPoint
class TopicActivity : BaseViewActivity<TopicViewModel>() {

    override val viewModel by viewModels<TopicViewModel>(
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

    @SuppressLint("CommitTransaction")
    override fun beginTransaction() {
        if (supportFragmentManager.findFragmentById(R.id.fragmentContainer) == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragmentContainer, TopicFragment()
                )
                .commit()
        }
    }

    override fun fetchData() {
        if (viewModel.type == "topic") {
            viewModel.url = viewModel.url.replace("/t/", "")
            viewModel.fetchTopicLayout()
        } else if (viewModel.type == "product") {
            viewModel.fetchProductLayout()
        }
    }

    override fun initData() {
        if (viewModel.isAInit) {
            viewModel.isAInit = false
            viewModel.activityState.value = LoadingState.Loading
        }
    }

}