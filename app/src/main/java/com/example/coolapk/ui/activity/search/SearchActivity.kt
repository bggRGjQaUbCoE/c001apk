package com.example.coolapk.ui.activity.search

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.example.coolapk.R
import com.example.coolapk.databinding.ActivitySearchBinding
import com.example.coolapk.ui.fragment.search.SearchFragment

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (supportFragmentManager.findFragmentById(R.id.searchFragment) == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.searchFragment, SearchFragment())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
        }
    }

}