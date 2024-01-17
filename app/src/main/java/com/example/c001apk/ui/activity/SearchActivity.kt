package com.example.c001apk.ui.activity

import android.os.Bundle
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivitySearchBinding
import com.example.c001apk.ui.fragment.search.SearchFragment

class SearchActivity : BaseActivity<ActivitySearchBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pageType = intent.getStringExtra("pageType")
        val pageParam = intent.getStringExtra("pageParam")
        val title = intent.getStringExtra("title")

        if (supportFragmentManager.findFragmentById(R.id.searchFragment) == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.searchFragment,
                    SearchFragment.newInstance(pageType, pageParam, title)
                )
                .commit()
        }
    }

}