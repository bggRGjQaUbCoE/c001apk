package com.example.c001apk.ui.coolpic

import android.annotation.SuppressLint
import android.os.Bundle
import com.example.c001apk.R
import com.example.c001apk.databinding.BaseFragmentContainerBinding
import com.example.c001apk.ui.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLDecoder

@AndroidEntryPoint
class CoolPicActivity : BaseActivity<BaseFragmentContainerBinding>() {

    private val title by lazy {
        URLDecoder.decode(intent.getStringExtra("title").orEmpty(), "UTF-8")
    }

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
                    R.id.fragmentContainer, CoolPicFragment.newInstance(title)
                )
                .commit()
        }
    }

}