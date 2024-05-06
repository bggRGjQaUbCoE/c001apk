package com.example.c001apk.logic.model

import com.google.gson.annotations.SerializedName

data class CheckCountResponse(
    val status: Int?,
    val message: String?,
    val messageStatus: String?,
    val data: Data?
) {

    data class Data(
        val notification: Int,
        @SerializedName("contacts_follow")
        val contactsFollow: Int,
        val message: Int,
        val atme: Int,
        val atcommentme: Int,
        val commentme: Int,
        val feedlike: Int,
        val badge: Int,
        val dateline: String
    )

}

