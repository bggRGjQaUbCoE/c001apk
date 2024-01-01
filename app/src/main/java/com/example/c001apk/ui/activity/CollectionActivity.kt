package com.example.c001apk.ui.activity

import android.os.Bundle
import androidx.fragment.app.FragmentTransaction
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityCollectionBinding
import com.example.c001apk.ui.fragment.CollectionFragment

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
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
        }
    }

}