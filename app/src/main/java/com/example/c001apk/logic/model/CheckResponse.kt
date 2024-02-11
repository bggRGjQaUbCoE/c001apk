package com.example.c001apk.logic.model

import com.google.gson.annotations.SerializedName

data class CheckResponse(
    val status: Int?,
    val message: String?,
    val messageStatus: String?,
    val data: Data?
) {

    data class Data(
        val id: String?,
        val status: Int,
        @SerializedName("message_status") val messageStatus: Int?,
        val uid: String,
        val username: String,
        val token: String,
        val refreshToken: String,
        val userAvatar: String,
        val notifyCount: NotifyCount,
    )

    data class NotifyCount(
        val notification: Int,
        @SerializedName("contacts_follow") val contactsFollow: Int,
        val message: Int,
        val atme: Int,
        val atcommentme: Int,
        val commentme: Int,
        val feedlike: Int,
        val badge: Int,
        val dateline: String
    )

}

