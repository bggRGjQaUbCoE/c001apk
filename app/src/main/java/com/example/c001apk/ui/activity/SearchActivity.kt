package com.example.c001apk.ui.activity

import android.os.Bundle
import androidx.fragment.app.FragmentTransaction
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivitySearchBinding
import com.example.c001apk.ui.fragment.search.SearchFragment

class SearchActivity : BaseActivity() {

    private lateinit var binding: ActivitySearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pageType = intent.getStringExtra("pageType" )!!
        val pageParam = intent.getStringExtra("pageParam")!!
        val title = intent.getStringExtra("title")!!

        if (supportFragmentManager.findFragmentById(R.id.searchFragment) == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.searchFragment, SearchFragment.newInstance(pageType, pageParam, title))
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
        }
    }

}