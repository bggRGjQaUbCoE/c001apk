package com.example.c001apk.ui.collection

import android.annotation.SuppressLint
import android.os.Bundle
import com.example.c001apk.R
import com.example.c001apk.databinding.BaseFragmentContainerBinding
import com.example.c001apk.ui.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CollectionActivity : BaseActivity<BaseFragmentContainerBinding>() {

    @SuppressLint("CommitTransaction")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (supportFragmentManager.findFragmentById(R.id.fragmentContainer) == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    CollectionFragment()
                )
                .commit()
        }
    }

}