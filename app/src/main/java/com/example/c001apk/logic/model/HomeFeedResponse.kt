package com.example.c001apk.logic.model

import com.google.gson.annotations.SerializedName

data class HomeFeedResponse(val data: List<Data>) {

    data class Data(
        val relationRows: ArrayList<RelationRows>,
        val targetRow: TargetRow?,
        @SerializedName("change_count") val changeCount: Int,
        val isModified: Int,
        @SerializedName("ip_location") val ipLocation: String,
        val isFeedAuthor: Int,
        val topReplyRows: List<TotalReplyResponse.Data>,
        val extraDataArr: ExtraDataArr?,
        val intro: String?,
        @SerializedName("tag_pics") val tagPics: List<String>,
        val tabList: List<TabList>,
        val displayUsername: String,
        val cover: String,
        val selectedTab: String,
        val homeTabCardRows: List<HomeTabCardRows>,
        @SerializedName("be_like_num") val beLikeNum: String,
        val version: String,
        val apkversioncode: String,
        val apksize: String,
        val lastupdate: String?,
        val follow: String,
        val level: String,
        val fans: String,
        val logintime: String,
        val experience: Int,
        val regdate: String,
        @SerializedName("next_level_experience") val nextLevelExperience: Int,
        val bio: String,
        val feed: String,
        val gender: Int,
        val city: String,
        val downnum: String,
        val downCount: String,
        val apkname: String,
        val entityType: String,
        val feedType: String,
        val entityTemplate: String,
        val entities: List<Entities>,
        val id: String,
        val url: String,
        val uid: String,
        val ruid: String,
        val username: String,
        val rusername: String,
        val tpic: String,
        val message: String,
        val pic: String,
        val tags: String,
        var likenum: String,
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
        val picArr: List<String>?,
        val replyRows: List<ReplyRows>,
        val replyRowsMore: Int,
        val logo: String,
        @SerializedName("hot_num") val hotNum: String,
        @SerializedName("feed_comment_num") val feedCommentNum: String,
        @SerializedName("hot_num_txt") val hotNumTxt: String,
        @SerializedName("feed_comment_num_txt") val feedCommentNumTxt: String,
        @SerializedName("commentnum_txt") val commentnumTxt: String,
        val commentCount: String,
        @SerializedName("alias_title") val aliasTitle: String,
        val userAction: UserAction?,
        val userInfo: UserInfo?,
        val fUserInfo: UserInfo?,
    )

    data class RelationRows(
        val id: String,
        val logo: String,
        val title: String,
        val url: String,
        val entityType: String,
    )

    data class TargetRow(
        val id: String,
        val logo: String,
        val title: String,
        val url: String,
        val entityType: String,
        val targetType: String?
    )

    data class ExtraDataArr(
        val pageTitle: String,
        val cardPageName: String
    )

    data class UserInfo(
        val uid: String,
        val username: String,
        val level: Int,
        val logintime: String,
        val regdate: String,
        val entityType: String,
        val displayUsername: String,
        val userAvatar: String,
        val cover: String,
        val fans: String,
        val follow: String,
        val bio: String
    )

    data class TabList(
        val title: String,
        val url: String,
        @SerializedName("page_name") val pageName: String,
        val entityType: String,
        val entityId: Int
    )

    data class HomeTabCardRows(
        val entityType: String,
        val entityTemplate: String,
        val title: String,
        val url: String,
        val entities: List<Entities>,
        val entityId: String,
    )

    data class UserAction(
        var like: Int,
        val favorite: Int,
        val follow: Int,
        val collect: Int,
        val followAuthor: Int,
        val authorFollowYou: Int
    )

    data class ReplyRows(
        val id: String,
        val uid: String,
        val username: String,
        val message: String,
        val ruid: String,
        val rusername: String,
        val picArr: List<String>?,
        val pic: String
    )

    data class Entities(
        val username: String,
        val url: String,
        val pic: String,
        val title: String,
        val logo: String,
        val id: String,
        val entityType: String,
        @SerializedName("alias_title") val aliasTitle: String,
        val userInfo: UserInfo
    )

}

