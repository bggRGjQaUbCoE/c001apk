package com.example.c001apk.logic.model

data class LoadUrlResponse(
    val status: Int?,
    val error: Int?,
    val message: String?,
    val data: Data?
) {
    data class Data(
        val title: String,
        val logo: String?,
        val url: String,
        val description: String,
    )
}