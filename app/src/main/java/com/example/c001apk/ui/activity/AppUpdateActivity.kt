package com.example.c001apk.ui.activity

import android.os.Bundle
import android.view.MenuItem
import com.example.c001apk.R
import com.example.c001apk.databinding.ActivityAppUpdateBinding
import com.example.c001apk.logic.model.UpdateCheckResponse
import com.example.c001apk.ui.fragment.home.app.UpdateListFragment

class AppUpdateActivity : BaseActivity<ActivityAppUpdateBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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