package com.example.coolapk.ui.activity.feed

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.example.coolapk.R
import com.example.coolapk.databinding.ActivityFeedBinding
import com.example.coolapk.ui.fragment.feed.FeedFragment

class FeedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedBinding

    @SuppressLint("CommitTransaction")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val type = intent.getStringExtra("type")
        val id = intent.getStringExtra("id")
        //val uname = intent.getStringExtra("uname")
        //val device = intent.getStringExtra("device")

        if (type == "feed" && supportFragmentManager.findFragmentById(R.id.feedFragment) == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.feedFragment, FeedFragment.newInstance(id!!))
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
        }

    }
}