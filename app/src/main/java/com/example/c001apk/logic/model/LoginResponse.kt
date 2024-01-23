package com.example.c001apk.logic.model

data class LoginResponse(
    val status: Int?,
    val messageStatus: Int?,
    val message: String?,
    val uid: String?,
    val username: String?,
    val token: String?,
    val refreshToken: String?
)
