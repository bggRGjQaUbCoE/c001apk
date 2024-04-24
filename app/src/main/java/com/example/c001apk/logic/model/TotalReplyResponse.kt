package com.example.c001apk.logic.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class TotalReplyResponse(
    val status: Int?,
    val error: Int?,
    val message: String?,
    val data: List<Data>?
) {

    @Parcelize
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
    ) : Parcelable

    @Parcelize
    data class UserAction(var like: Int) : Parcelable

    @Parcelize
    data class UserInfo(val username: String) : Parcelable


}

