package com.example.c001apk.logic.model

class PostReplyResponse(
    val status: Int?,
    val message: String?,
    val messageStatus: String?,
    val data: TotalReplyResponse.Data?
)