package com.example.c001apk.ui.collection

import android.os.Bundle
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityCollectionBinding
import com.example.c001apk.ui.base.BaseActivity

class CollectionActivity : BaseActivity<ActivityCollectionBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (supportFragmentManager.findFragmentById(R.id.fragment) == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragment,
                    CollectionFragment()
                )
                .commit()
        }
    }

}