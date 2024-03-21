package com.example.c001apk.adapter

sealed class FooterState {
    data object Loading : FooterState()
    data object LoadingDone : FooterState()
    data object LoadingEnd : FooterState()
    class LoadingError(val errMsg: String) : FooterState()
    data object LoadingReply : FooterState()
}