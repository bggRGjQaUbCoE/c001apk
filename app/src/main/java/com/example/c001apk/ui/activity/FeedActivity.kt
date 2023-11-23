package com.example.c001apk.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.FragmentTransaction
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityFeedBinding
import com.example.c001apk.ui.fragment.feed.FeedFragment

class FeedActivity : BaseActivity() {

    private lateinit var binding: ActivityFeedBinding

    @SuppressLint("CommitTransaction")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var type = intent.getStringExtra("type")
        var id = intent.getStringExtra("id")
        val uid = intent.getStringExtra("uid")
        val uname = intent.getStringExtra("uname")
        val viewReply = intent.getBooleanExtra("viewReply", false)

        val data = intent.data
        if (data.toString().contains("coolmarket://feed/")) {
            type = "feed"
            id = data.toString().replace("coolmarket://feed/", "")
        }

        if (type == "feed" && supportFragmentManager.findFragmentById(R.id.feedFragment) == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.feedFragment,
                    FeedFragment.newInstance(id!!, uid, uname, viewReply)
                )
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
        }

    }
}