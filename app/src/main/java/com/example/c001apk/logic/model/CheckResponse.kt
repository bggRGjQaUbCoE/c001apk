package com.example.c001apk.logic.model

data class CheckResponse(val status: Int?, val data: Data?) {

    data class Data(
        val status: String,
        val uid: String,
        val username: String,
        val token: String,
        val refreshToken: String,
        val userAvatar: String
    )

}

