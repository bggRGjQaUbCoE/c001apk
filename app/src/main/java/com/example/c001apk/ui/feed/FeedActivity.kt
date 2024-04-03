package com.example.c001apk.ui.feed

import android.annotation.SuppressLint
import androidx.activity.viewModels
import com.example.c001apk.R
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.ui.base.BaseViewActivity
import com.example.c001apk.ui.feed.question.FeedQuestionFragment
import com.example.c001apk.ui.feed.vote.FeedVoteFragment
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback

@AndroidEntryPoint
class FeedActivity : BaseViewActivity<FeedViewModel>() {

    override val viewModel by viewModels<FeedViewModel>(
        extrasProducer = {
            defaultViewModelCreationExtras.withCreationCallback<FeedViewModel.Factory> { factory ->
                factory.create(
                    intent.getStringExtra("id").orEmpty(),
                    intent.getStringExtra("rid"),
                    intent.getBooleanExtra("viewReply", false),
                )
            }
        }
    )

    override fun initData() {
        if (viewModel.isAInit) {
            viewModel.isAInit = false
            viewModel.activityState.value = LoadingState.Loading
        }
    }

    override fun fetchData() {
        viewModel.fetchFeedData()
    }

    @SuppressLint("CommitTransaction")
    override fun beginTransaction() {
        if (supportFragmentManager.findFragmentById(R.id.fragmentContainer) == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    when (viewModel.feedType) {
                        "vote" -> FeedVoteFragment()
                        "question" -> FeedQuestionFragment()
                        else -> FeedFragment()
                    }
                )
                .commit()
        }
    }

}