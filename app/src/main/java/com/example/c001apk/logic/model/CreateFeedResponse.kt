package com.example.c001apk.logic.model

import com.google.gson.annotations.SerializedName

data class CreateFeedResponse(
    val status: Int?,
    val error: Int?,
    val message: String?,
    val messageStatus: String?,
    val data: Data?
) {
    data class Data(
        val status: Int?,
        val error: Int?,
        val messageStatus: String?,
        val id: String?,
        val type: String?,
        val message: String?,
        val pic: String,
        @SerializedName("device_title") val deviceTitle: String?,
        @SerializedName("message_signature") val messageSignature: String?,
        val picArr: List<String>?,
    )
}
