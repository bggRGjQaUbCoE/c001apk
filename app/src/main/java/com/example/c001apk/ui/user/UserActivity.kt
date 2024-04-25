package com.example.c001apk.ui.user

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import com.example.c001apk.R
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.logic.model.UserProfileResponse
import com.example.c001apk.ui.base.BaseViewActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.withCreationCallback


@AndroidEntryPoint
class UserActivity : BaseViewActivity<UserViewModel>() {

    override val viewModel by viewModels<UserViewModel>(
        extrasProducer = {
            defaultViewModelCreationExtras.withCreationCallback<UserViewModel.Factory> { factory ->
                factory.create(uid = intent.getStringExtra("id").orEmpty())
            }
        }
    )

    override fun getSavedData(savedInstanceState: Bundle?) {
        if (viewModel.userData == null) {
            viewModel.userData = if (Build.VERSION.SDK_INT >= 33)
                savedInstanceState?.getParcelable("userData", UserProfileResponse.Data::class.java)
            else savedInstanceState?.getParcelable("userData")
        }
    }

    override fun initData() {
        if (viewModel.isAInit) {
            viewModel.isAInit = false
            if (viewModel.userData == null) {
                viewModel.activityState.value = LoadingState.Loading
            } else {
                viewModel.uid = viewModel.userData?.uid.orEmpty()
            }
        }
    }

    @SuppressLint("CommitTransaction")
    override fun beginTransaction() {
        if (supportFragmentManager.findFragmentById(R.id.fragmentContainer) == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, UserPagerFragment())
                .commit()
        }
    }

    override fun fetchData() {
        viewModel.fetchUser()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        viewModel.userData?.let {
            outState.putParcelable("userData", it)
        }
        super.onSaveInstanceState(outState)
    }

}