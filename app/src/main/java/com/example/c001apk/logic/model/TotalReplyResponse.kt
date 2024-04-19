package com.example.c001apk.logic.model

import com.google.gson.annotations.SerializedName

data class TotalReplyResponse(
    val status: Int?,
    val error: Int?,
    val message: String?,
    val data: List<Data>?
) {

    data class Data(
        var lastupdate: Long?,
        @SerializedName("extra_key") val extraKey: String?,
        val entityType: String?,
        val id: String,
        val ruid: String?,
        val uid: String,
        val feedUid: String?,
        var username: String,
        val rusername: String?,
        var message: String,
        val pic: String?,
        val picArr: List<String>?,
        val dateline: Long,
        var likenum: String,
        val replynum: String,
        val userAvatar: String,
        var replyRows: MutableList<Data>?,
        val replyRowsMore: Int?,
        val userAction: UserAction?,
        val userInfo: UserInfo
    )

    data class UserAction(var like: Int)

    data class UserInfo(val username: String)


}

