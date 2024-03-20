package com.example.c001apk.ui.appupdate

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityAppUpdateBinding
import com.example.c001apk.logic.model.UpdateCheckResponse
import com.example.c001apk.ui.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AppUpdateActivity : BaseActivity<ActivityAppUpdateBinding>() {

    @SuppressLint("CommitTransaction")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.appBar.setLiftable(true)

        val appsUpdateList = intent?.getParcelableArrayListExtra<UpdateCheckResponse.Data>("list")
                as ArrayList<UpdateCheckResponse.Data>

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "应用更新：" + appsUpdateList.size

        supportFragmentManager.beginTransaction()
            .replace(R.id.appUpdateFragment, UpdateListFragment.newInstance(appsUpdateList))
            .commit()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}