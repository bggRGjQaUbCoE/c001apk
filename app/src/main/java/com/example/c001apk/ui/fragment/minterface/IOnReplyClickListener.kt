package com.example.c001apk.ui.fragment.minterface

interface IOnReplyClickListener {

    fun onReply2Reply(
        rPosition: Int,
        r2rPosition: Int?,
        id: String,
        uid: String,
        uname: String,
        type: String
    )

}