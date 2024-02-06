package com.example.c001apk.ui.activity

import android.os.Bundle
import android.view.MenuItem
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityAppUpdateBinding
import com.example.c001apk.ui.fragment.home.app.UpdateListFragment
import com.example.c001apk.util.UpdateListUtil

class AppUpdateActivity : BaseActivity<ActivityAppUpdateBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "应用更新：" + UpdateListUtil.appsUpdate.size

        supportFragmentManager.beginTransaction()
            .replace(R.id.appUpdateFragment, UpdateListFragment())
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}