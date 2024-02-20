package com.example.c001apk.ui.search

import android.os.Bundle
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivitySearchBinding
import com.example.c001apk.ui.base.BaseActivity

class SearchActivity : BaseActivity<ActivitySearchBinding>() {

    private val pageType by lazy { intent.getStringExtra("pageType").orEmpty() }
    private val pageParam by lazy { intent.getStringExtra("pageParam").orEmpty() }
    private val title by lazy { intent.getStringExtra("title").orEmpty() }

    private val searchFragment by lazy {
        SearchFragment.newInstance(pageType, pageParam, title)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupSearchFragment()
    }

    private fun setupSearchFragment() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.searchFragment, searchFragment)
            .commit()
    }
}
