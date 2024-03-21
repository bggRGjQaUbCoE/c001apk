package com.example.c001apk.ui.feed

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.example.c001apk.R
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.databinding.ActivityFeedBinding
import com.example.c001apk.ui.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FeedActivity : BaseActivity<ActivityFeedBinding>() {

    private val viewModel by viewModels<FeedViewModel>()
    private val id by lazy { intent.getStringExtra("id") }
    private val frid by lazy { intent.getStringExtra("rid") }
    private val isViewReply by lazy { intent.getBooleanExtra("viewReply", false) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initData()
        initObserve()

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
                    viewModel.fetchFeedData()
                }

                LoadingState.LoadingDone -> {
                    loadFeedDetail()
                }

                is LoadingState.LoadingError -> {
                    binding.errorMessage.errMsg.text = it.errMsg
                    binding.errorMessage.errMsg.isVisible = true

                }

                is LoadingState.LoadingFailed -> {
                    binding.errorLayout.msg.text = it.msg
                    binding.errorLayout.parent.isVisible = true
                }
            }
            if (it !is LoadingState.Loading) {
                binding.indicator.parent.isIndeterminate = false
                binding.indicator.parent.isVisible = false
            }
        }
    }

    private fun initData() {
        if (viewModel.isInitFeed) {
            viewModel.isInitFeed = false
            viewModel.id = id
            viewModel.frid = frid
            viewModel.isViewReply = isViewReply
            viewModel.loadingState.value = LoadingState.Loading
        }
    }

    @SuppressLint("CommitTransaction")
    private fun loadFeedDetail() {
        if (viewModel.feedType != "vote") // not done yet
            if (supportFragmentManager.findFragmentById(R.id.feedFragment) == null) {
                supportFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.feedFragment,
                        /*when (viewModel.feedType) {
                            "vote" -> FeedVoteFragment.newInstance(viewModel.id)
                            else -> FeedFragmentNew()
                        }*/
                        FeedFragment()
                    )
                    .commit()
            }
    }

}