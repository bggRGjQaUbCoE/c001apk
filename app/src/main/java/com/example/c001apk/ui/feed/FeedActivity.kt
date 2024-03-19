package com.example.c001apk.ui.feed

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityFeedBinding
import com.example.c001apk.ui.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FeedActivity : BaseActivity<ActivityFeedBinding>() {

    private val viewModel by lazy { ViewModelProvider(this)[FeedViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.id = intent.getStringExtra("id")
        viewModel.frid = intent.getStringExtra("rid")
        if (viewModel.isViewReply == null)
            viewModel.isViewReply = intent.getBooleanExtra("viewReply", false)

        binding.errorLayout.retry.setOnClickListener {
            binding.errorLayout.parent.visibility = View.GONE
            getFeedData()
        }

        if (supportFragmentManager.findFragmentById(R.id.feedFragment) == null && viewModel.feedType == null) {
            getFeedData()
        } else {
            doNext()
        }


        viewModel.doNext.observe(this) { event ->
            event?.getContentIfNotHandledOrReturnNull()?.let {
                when (it.first) {
                    1 -> showErrorMessage(it.second.toString())
                    2 -> doNext()
                    3 -> binding.errorLayout.parent.visibility = View.VISIBLE
                }
                binding.indicator.parent.isIndeterminate = false
                binding.indicator.parent.visibility = View.GONE

            }
        }

    }

    private fun getFeedData() {
        binding.indicator.parent.isIndeterminate = true
        binding.indicator.parent.visibility = View.VISIBLE
        viewModel.fetchFeedData()
    }

    private fun showErrorMessage(errorMessage: String) {
        binding.errorMessage.parent.visibility = View.VISIBLE
        binding.errorMessage.parent.text = errorMessage
    }

    @SuppressLint("CommitTransaction")
    private fun doNext() {
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