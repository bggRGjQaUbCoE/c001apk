package com.example.c001apk.ui.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.c001apk.adapter.LoadingState

abstract class BaseViewModel : ViewModel() {

    var lastItem: String? = null
    var isInit: Boolean = true
    var listSize: Int = -1
    var isRefreshing: Boolean = false
    var isLoadMore: Boolean = false
    var isEnd: Boolean = false
    var lastVisibleItemPosition: Int = 0
    var page: Int = 1

    val activityState = MutableLiveData<LoadingState>()
    val loadingState = MutableLiveData<LoadingState>()

    abstract fun fetchData()

}
