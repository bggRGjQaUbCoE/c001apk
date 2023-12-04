package com.example.c001apk.logic.model

data class LikeFeedResponse(
    val data: Data?,
    val status: Int?,
    val error: Int?,
    val message: String?,
    val messageStatus: String?,
) {
    data class Data(
        val follow: Int?,
        val count: String
    )
}