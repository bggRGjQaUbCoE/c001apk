package com.example.c001apk.ui.topic

import android.annotation.SuppressLint
import android.os.Bundle
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityTopicBinding
import com.example.c001apk.ui.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TopicActivity : BaseActivity<ActivityTopicBinding>() {

    private val type by lazy { intent.getStringExtra("type") }
    private val title by lazy { intent.getStringExtra("title") }
    private val url by lazy { intent.getStringExtra("url") }
    private val id by lazy { intent.getStringExtra("id") }

    @SuppressLint("CommitTransaction")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (supportFragmentManager.findFragmentById(R.id.topicFragment) == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.topicFragment, TopicFragment.newInstance(type, title, url, id)
                )
                .commit()
        }
    }

}