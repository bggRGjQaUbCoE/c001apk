package com.example.c001apk.ui.carousel

import android.annotation.SuppressLint
import androidx.activity.viewModels
import com.example.c001apk.R
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.ui.base.BaseViewActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CarouselActivity : BaseViewActivity<CarouselViewModel>() {

    @Inject
    lateinit var viewModelAssistedFactory: CarouselViewModel.Factory
    override val viewModel by viewModels<CarouselViewModel>(
        factoryProducer = {
            CarouselViewModel.provideFactory(
                viewModelAssistedFactory,
                intent.getStringExtra("url").orEmpty(),
                intent.getStringExtra("title").orEmpty()
            )
        })

    @SuppressLint("CommitTransaction")
    override fun beginTransaction() {
        if (supportFragmentManager.findFragmentById(R.id.fragmentContainer) == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, CarouselPagerFragment())
                .commit()
        }
    }

    override fun fetchData() {
        viewModel.initCarouselList()
    }

    override fun initData() {
        if (viewModel.isAInit) {
            viewModel.isAInit = false
            viewModel.activityState.value = LoadingState.Loading
        }
    }

}