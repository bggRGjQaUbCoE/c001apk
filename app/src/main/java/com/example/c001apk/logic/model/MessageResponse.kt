package com.example.c001apk.logic.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class MessageResponse(
    val status: Int?,
    val error: Int?,
    val message: String?,
    val messageStatus: Int?,
    val data: List<Data>?
) {

    data class Data(
        val rid: Long?,
        val infoHtml: String?,
        val entityType: String,
        val id: String,
        val uid: String,
        val dateline: Long,
        val message: String,
        val username: String,
        val forwardid: String,
        @SerializedName("source_id") val sourceId: String,
        val pic: String,
        val istag: Int,
        val tags: String,
        val likenum: String,
        val commentnum: String,
        val replynum: String,
        val favnum: String,
        @SerializedName("device_title") val deviceTitle: String,
        val userAvatar: String,
        val title: String,
        val picArr: List<String>?,
        val userAction: HomeFeedResponse.UserAction?,
        val forwardSourceFeed: ForwardSourceFeed?,
        val feed: Feed?,
        val likeUsername: String,
        val likeUid: String,
        val likeTime: Long,
        val likeAvatar: String,
        val fid: String,
        val fromUserAvatar: String,
        val fromusername: String,
        val fromuid: String,
        val note: String
    )

    data class Feed(
        val id: String,
        val uid: String,
        val username: String,
        val message: String,
        val pic: String?,
        val url: String
    )

    @Parcelize
    data class ForwardSourceFeed(
        val entityType: String,
        val feedType: String,
        val id: String,
        val username: String,
        val uid: String,
        val message: String,
        @SerializedName("message_title") val messageTitle: String,
        val pic: String,
        val picArr: List<String>?,
    ): Parcelable
}

