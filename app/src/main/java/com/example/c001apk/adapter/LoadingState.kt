package com.example.c001apk.adapter

sealed class LoadingState {
    data object Loading : LoadingState()
    data object LoadingDone : LoadingState()
    class LoadingError(val errMsg: String) : LoadingState()
    class LoadingFailed(val msg: String) : LoadingState()
}