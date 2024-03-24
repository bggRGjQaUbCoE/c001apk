package com.example.c001apk.ui.carousel

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.example.c001apk.R
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.constant.Constants
import com.example.c001apk.databinding.BaseFragmentContainerBinding
import com.example.c001apk.ui.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CarouselActivity : BaseActivity<BaseFragmentContainerBinding>() {

    @Inject
    lateinit var viewModelAssistedFactory: CarouselViewModel.Factory
    private val viewModel by viewModels<CarouselViewModel>(
        factoryProducer = {
            CarouselViewModel.provideFactory(
                viewModelAssistedFactory,
                intent.getStringExtra("url").orEmpty(),
                intent.getStringExtra("title").orEmpty()
            )
        })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initData()
        initObserve()
        initError()

    }

    private fun initObserve() {
        viewModel.activityState.observe(this) {
            when (it) {
                LoadingState.Loading -> {
                    binding.indicator.parent.isIndeterminate = true
                    binding.indicator.parent.isVisible = true
                    viewModel.initCarouselList()
                }

                LoadingState.LoadingDone -> {
                    beginTransaction()
                }

                is LoadingState.LoadingError -> {
                    binding.errorMessage.errMsg.apply {
                        text = it.errMsg
                        isVisible = true
                    }
                }

                is LoadingState.LoadingFailed -> {
                    binding.errorLayout.apply {
                        msg.text = it.msg
                        retry.text =
                            if (it.msg == Constants.LOADING_EMPTY) getString(R.string.refresh)
                            else getString(R.string.retry)
                        parent.isVisible = true
                    }
                }
            }
            if (it !is LoadingState.Loading) {
                binding.indicator.parent.isIndeterminate = false
                binding.indicator.parent.isVisible = false
            }
        }
    }

    @SuppressLint("CommitTransaction")
    private fun beginTransaction() {
        if (supportFragmentManager.findFragmentById(R.id.fragmentContainer) == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, CarouselPagerFragment())
                .commit()

        }
    }

    private fun initError() {
        binding.errorLayout.retry.setOnClickListener {
            binding.errorLayout.parent.isVisible = false
            viewModel.activityState.value = LoadingState.Loading
        }
    }

    private fun initData() {
        if (viewModel.isAInit) {
            viewModel.isAInit = false
            viewModel.activityState.value = LoadingState.Loading
        }
    }

}