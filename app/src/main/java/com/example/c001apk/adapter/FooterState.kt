package com.example.c001apk.adapter

sealed class FooterState {
    data object Loading : FooterState()
    data object LoadingDone : FooterState()
    class LoadingEnd(val msg: String) : FooterState()
    class LoadingError(val errMsg: String) : FooterState()
    data object LoadingReply : FooterState()
}