package com.example.c001apk.ui.follow

import android.annotation.SuppressLint
import android.os.Bundle
import com.example.c001apk.R
import com.example.c001apk.databinding.BaseFragmentContainerBinding
import com.example.c001apk.ui.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FFFListActivity : BaseActivity<BaseFragmentContainerBinding>() {

    private val uid by lazy { intent.getStringExtra("uid").orEmpty() }
    private val type by lazy { intent.getStringExtra("type") ?: "feed" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        beginTransaction()
    }

    @SuppressLint("CommitTransaction")
    private fun beginTransaction() {
        if (supportFragmentManager.findFragmentById(R.id.fragmentContainer) == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    FollowPagerFragment.newInstance(uid, type)
                )
                .commit()
        }
    }

}
