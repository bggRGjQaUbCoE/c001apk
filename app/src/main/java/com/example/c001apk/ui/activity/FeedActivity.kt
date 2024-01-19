package com.example.c001apk.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityFeedBinding
import com.example.c001apk.ui.fragment.feed.FeedFragment
import com.example.c001apk.ui.fragment.feed.FeedVoteFragment
import com.example.c001apk.viewmodel.AppViewModel

class FeedActivity : BaseActivity<ActivityFeedBinding>() {

    private val viewModel by lazy { ViewModelProvider(this)[AppViewModel::class.java] }

    @SuppressLint("CommitTransaction", "InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //viewModel.type = intent.getStringExtra("type")
        viewModel.id = intent.getStringExtra("id")
        viewModel.frid = intent.getStringExtra("rid")
        //viewModel.uid = intent.getStringExtra("uid")
        //viewModel.uname = intent.getStringExtra("uname")
        if (viewModel.isViewReply == null)
            viewModel.isViewReply = intent.getBooleanExtra("viewReply", false)

        binding.errorLayout.retry.setOnClickListener {
            binding.errorLayout.parent.visibility = View.GONE
            getFeedData()
        }

        if (supportFragmentManager.findFragmentById(R.id.feedFragment) == null && viewModel.feedContentList.isEmpty()) {
            getFeedData()
        } else {
            doNext()
        }

        viewModel.feedData.observe(this) { result ->
            if (viewModel.isNew) {
                viewModel.isNew = false

                val feed = result.getOrNull()
                if (feed?.message != null) {
                    viewModel.errorMessage = feed.message
                    binding.indicator.parent.isIndeterminate = false
                    binding.indicator.parent.visibility = View.GONE
                    showErrorMessage()
                    return@observe
                } else if (feed?.data != null) {
                    viewModel.uid = feed.data.uid
                    viewModel.funame = feed.data.userInfo?.username.toString()
                    viewModel.avatar = feed.data.userAvatar
                    viewModel.device = feed.data.deviceTitle.toString()
                    viewModel.replyCount = feed.data.replynum
                    viewModel.dateLine = feed.data.dateline
                    viewModel.feedTypeName = feed.data.feedTypeName
                    viewModel.feedType = feed.data.feedType

                    viewModel.feedContentList.clear()
                    viewModel.feedContentList.add(feed)
                    if (!feed.data.topReplyRows.isNullOrEmpty()) {
                        viewModel.isTop = true
                        viewModel.topReplyId = feed.data.topReplyRows[0].id
                        viewModel.feedTopReplyList.clear()
                        viewModel.feedTopReplyList.addAll(feed.data.topReplyRows)
                    } else if (!feed.data.replyMeRows.isNullOrEmpty()) {
                        viewModel.isTop = false
                        viewModel.topReplyId = feed.data.replyMeRows[0].id
                        viewModel.feedTopReplyList.clear()
                        viewModel.feedTopReplyList.addAll(feed.data.replyMeRows)
                    }

                    doNext()
                } else {
                    binding.errorLayout.parent.visibility = View.VISIBLE
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
            binding.indicator.parent.isIndeterminate = false
            binding.indicator.parent.visibility = View.GONE
        }


    }

    private fun getFeedData() {
        binding.indicator.parent.isIndeterminate = true
        binding.indicator.parent.visibility = View.VISIBLE
        viewModel.isNew = true
        viewModel.getFeed()
    }

    private fun showErrorMessage() {
        binding.errorMessage.parent.visibility = View.VISIBLE
        binding.errorMessage.parent.text = viewModel.errorMessage
    }

    private fun doNext() {
        if (supportFragmentManager.findFragmentById(R.id.feedFragment) == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.feedFragment,
                    when (viewModel.feedType) {
                        "vote" -> FeedVoteFragment.newInstance(viewModel.id)
                        else -> FeedFragment()
                    }
                )
                //.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
        }
    }

}