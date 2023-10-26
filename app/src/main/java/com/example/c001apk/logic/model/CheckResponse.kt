package com.example.c001apk.logic.model

import com.google.gson.annotations.SerializedName

data class CheckResponse(val status: Int?,
                         val message: String?,
                         val data: Data?) {

    data class Data(
        val status: Int,
        @SerializedName("message_status")val messageStatus: Int?,
        val uid: String,
        val username: String,
        val token: String,
        val refreshToken: String,
        val userAvatar: String
    )

}

