package com.example.c001apk.ui.topic

import android.annotation.SuppressLint
import androidx.activity.viewModels
import com.example.c001apk.R
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.constant.Constants.LOADING_EMPTY
import com.example.c001apk.ui.base.BaseViewActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback
import java.net.URLDecoder

@AndroidEntryPoint
class TopicActivity : BaseViewActivity<TopicViewModel>() {

    override val viewModel by viewModels<TopicViewModel>(
        extrasProducer = {
            defaultViewModelCreationExtras.withCreationCallback<TopicViewModel.Factory> { factory ->
                factory.create(
                    URLDecoder.decode(intent.getStringExtra("url").orEmpty(), "UTF-8"),
                    intent.getStringExtra("title").orEmpty(),
                    intent.getStringExtra("id").orEmpty(),
                    intent.getStringExtra("type").orEmpty(),
                )
            }
        }
    )

    @SuppressLint("CommitTransaction")
    override fun beginTransaction() {
        if (viewModel.topicList.isNotEmpty()) {
            if (supportFragmentManager.findFragmentById(R.id.fragmentContainer) == null) {
                supportFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.fragmentContainer, TopicFragment()
                    )
                    .commit()
            }
        } else {
            viewModel.loadingState.value = LoadingState.LoadingError(LOADING_EMPTY)
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