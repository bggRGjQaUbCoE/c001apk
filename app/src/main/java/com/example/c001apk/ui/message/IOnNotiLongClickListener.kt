package com.example.c001apk.ui.message

interface IOnNotiLongClickListener {
    fun onDeleteNoti(uname: String, id: String, position: Int)

    fun onReload()
}