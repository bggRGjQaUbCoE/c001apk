package com.example.c001apk.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.FragmentTransaction
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityFeedBinding
import com.example.c001apk.ui.fragment.feed.FeedFragment
import com.example.c001apk.ui.fragment.feed.FeedVoteFragment

class FeedActivity : BaseActivity<ActivityFeedBinding>() {

    @SuppressLint("CommitTransaction", "InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val type = intent.getStringExtra("type")
        val id = intent.getStringExtra("id")
        val rid = intent.getStringExtra("rid")
        val uid = intent.getStringExtra("uid")
        val uname = intent.getStringExtra("uname")
        val viewReply = intent.getBooleanExtra("viewReply", false)

        if (type == "vote") {
            if (supportFragmentManager.findFragmentById(R.id.feedFragment) == null) {
                supportFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.feedFragment,
                        FeedVoteFragment.newInstance(id!!)
                    )
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit()
            }
        } else if (supportFragmentManager.findFragmentById(R.id.feedFragment) == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.feedFragment,
                    FeedFragment.newInstance(type, id!!, rid, uid, uname, viewReply)
                )
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
        }

    }
}