package com.example.c001apk.ui.appupdate

import android.annotation.SuppressLint
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.view.MenuItem
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityAppUpdateBinding
import com.example.c001apk.logic.model.UpdateCheckResponse
import com.example.c001apk.ui.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AppUpdateActivity : BaseActivity<ActivityAppUpdateBinding>() {

    private val appsUpdateList by lazy {
        val list = if (SDK_INT >= 33)
            intent.getParcelableArrayListExtra("list", UpdateCheckResponse.Data::class.java)
        else
            intent.getParcelableArrayListExtra("list")
        list ?: ArrayList()
    }

    @SuppressLint("CommitTransaction")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.appBar.setLiftable(true)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "应用更新：" + appsUpdateList.size

        if (supportFragmentManager.findFragmentById(R.id.appUpdateFragment) == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.appUpdateFragment, UpdateListFragment.newInstance(appsUpdateList))
                .commit()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}