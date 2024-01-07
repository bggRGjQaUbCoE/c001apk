package com.example.c001apk.ui.activity

import android.os.Bundle
import androidx.fragment.app.FragmentTransaction
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityTopicBinding
import com.example.c001apk.ui.fragment.topic.TopicFragment

class TopicActivity : BaseActivity<ActivityTopicBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val type = intent.getStringExtra("type")
        val title = intent.getStringExtra("title")
        val url = intent.getStringExtra("url")
        val id = intent.getStringExtra("id")

        if (supportFragmentManager.findFragmentById(R.id.topicFragment) == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.topicFragment, TopicFragment.newInstance(type, title, url, id)
                )
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
        }
    }

}