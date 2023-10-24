package com.example.c001apk.logic.model

data class CheckResponse(val status: Int?, val message: String?, val data: Data?) {

    data class Data(
        val status: Int,
        val uid: String,
        val username: String,
        val token: String,
        val refreshToken: String,
        val userAvatar: String
    )

}

