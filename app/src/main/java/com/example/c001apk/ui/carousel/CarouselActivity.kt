package com.example.c001apk.ui.carousel

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import com.example.c001apk.R
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.logic.model.TopicBean
import com.example.c001apk.ui.base.BaseViewActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CarouselActivity : BaseViewActivity<CarouselViewModel>() {

    private var isSinglePage: Int? = null

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

    override fun getSavedData(savedInstanceState: Bundle?) {
        if (viewModel.topicList == null) {
            viewModel.topicList = if (Build.VERSION.SDK_INT >= 33)
                savedInstanceState?.getParcelableArrayList("topicList", TopicBean::class.java)
            else
                savedInstanceState?.getParcelableArrayList("topicList")
            isSinglePage = savedInstanceState?.getInt("isSinglePage")
        }
    }

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
            if (viewModel.topicList == null && isSinglePage == null) {
                viewModel.activityState.value = LoadingState.Loading
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (!viewModel.topicList.isNullOrEmpty()) {
            outState.putParcelableArrayList("topicList", viewModel.topicList)
        } else if (!viewModel.dataList.value.isNullOrEmpty()) {
            outState.putInt("isSinglePage", 1)
        }
        super.onSaveInstanceState(outState)
    }

}