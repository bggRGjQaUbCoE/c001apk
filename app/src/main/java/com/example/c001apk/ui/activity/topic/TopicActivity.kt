package com.example.c001apk.ui.activity.topic

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityTopicBinding
import com.example.c001apk.ui.fragment.topic.TopicFragment

class TopicActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTopicBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTopicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val title = intent.getStringExtra("title")!!

        if (supportFragmentManager.findFragmentById(R.id.topicFragment) == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.topicFragment, TopicFragment.newInstance(title, ""))
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
        }
    }

}