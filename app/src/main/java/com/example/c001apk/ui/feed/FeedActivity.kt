package com.example.c001apk.ui.feed

import android.annotation.SuppressLint
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import androidx.activity.viewModels
import com.example.c001apk.R
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.logic.model.HomeFeedResponse
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

    override fun getSavedData(savedInstanceState: Bundle?) {
        if (viewModel.feedData == null) {
            viewModel.feedData = if (SDK_INT >= 33)
                savedInstanceState?.getParcelable("feedData", HomeFeedResponse.Data::class.java)
            else savedInstanceState?.getParcelable("feedData")
        }
    }

    override fun initData() {
        if (viewModel.isAInit) {
            viewModel.isAInit = false
            if (viewModel.feedData == null) {
                viewModel.activityState.value = LoadingState.Loading
            } else {
                viewModel.handleFeedData()
            }
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

    override fun onSaveInstanceState(outState: Bundle) {
        viewModel.feedData?.let {
            outState.putParcelable("feedData", it)
        }
        super.onSaveInstanceState(outState)
    }

}