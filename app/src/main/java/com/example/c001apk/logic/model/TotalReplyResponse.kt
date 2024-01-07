package com.example.c001apk.logic.model

import com.google.gson.annotations.SerializedName

data class TotalReplyResponse(
    val status: Int?,
    val error: Int?,
    val message: String?,
    val data: List<Data>?
) {

    data class Data(
        @SerializedName("extra_key") val extraKey: String?,
        val entityType: String?,
        val id: String,
        val ruid: String,
        val uid: String,
        val feedUid: String,
        val username: String,
        val rusername: String,
        val message: String,
        val pic: String,
        val picArr: List<String>?,
        val dateline: Long,
        var likenum: String,
        val replynum: String,
        val userAvatar: String,
        val replyRows: ArrayList<Data>?,
        val replyRowsMore: Int,
        val userAction: UserAction?
    )

    data class UserAction(var like: Int)

}

