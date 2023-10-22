package com.example.c001apk.logic.model

import com.google.gson.annotations.SerializedName

data class HomeFeedResponse(val data: List<Data>) {

    data class Data(
        val entityType: String,
        val feedType: String,
        val entityTemplate: String,
        val entities: List<Entities>,
        val id: String,
        val uid: String,
        val ruid: String,
        val username: String,
        val rusername: String,
        val tpic: String,
        val message: String,
        val pic: String,
        val tags: String,
        val likenum: String,
        val commentnum: String,
        val replynum: String,
        val forwardnum: String,
        val favnum: String,
        val dateline: String,
        @SerializedName("create_time") val createTime: String,
        @SerializedName("device_title") val deviceTitle: String,
        @SerializedName("device_name") val deviceName: String,
        @SerializedName("recent_reply_ids") val recentReplyIds: String,
        @SerializedName("recent_like_list") val recentLikeList: String,
        val entityId: String,
        val userAvatar: String,
        val infoHtml: String,
        val title: String,
        val picArr: List<String>,
        val replyRows: List<ReplyRows>,
        val replyRowsMore: Int,
        val logo: String,
        @SerializedName("hot_num") val hotNum: String,
        @SerializedName("feed_comment_num") val feedCommentNum: String,
        @SerializedName("alias_title") val aliasTitle: String,
    )

    data class ReplyRows(
        val id: String, val uid: String, val username: String,
        val message: String, val likenum: String, val ruid: String,
        val rusername: String
    )

    data class Entities(
        val url: String,
        val pic: String,
        val title: String,
        val logo: String,
        val id: String,
        val entityType: String,
        @SerializedName("alias_title") val aliasTitle: String
    )

}

