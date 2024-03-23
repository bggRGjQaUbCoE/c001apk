package com.example.c001apk.ui.dyh

import android.annotation.SuppressLint
import android.os.Bundle
import com.example.c001apk.R
import com.example.c001apk.databinding.BaseFragmentContainerBinding
import com.example.c001apk.ui.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DyhActivity : BaseActivity<BaseFragmentContainerBinding>() {

    private val id by lazy { intent.getStringExtra("id").orEmpty() }
    private val title by lazy { intent.getStringExtra("title").orEmpty() }

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
                    R.id.fragmentContainer, DyhFragment.newInstance(id, title)
                )
                .commit()
        }
    }

}