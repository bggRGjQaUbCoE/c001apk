package com.example.c001apk.logic.model

import com.google.gson.annotations.SerializedName

data class UserResponse(val data: Data) {

    data class Data(
        val uid: String,
        val username: String,
        val level: Int,
        val displayUsername: String,
        val userAvatar: String,
        val cover: String,
        val selectedTab: String,
        val homeTabCardRows: List<HomeTabCardRows>,
        val gender: Int,
        val bio: String,
        @SerializedName("be_like_num") val beLikeNum: String,
        val follow: String,
        val fans: String,
        val feed: String,
        val logintime: String,
        val regdate: String
    )

    data class HomeTabCardRows(
        val entityType: String,
        val entityTemplate: String,
        val title: String,
        val url: String,
        val entities: List<HomeFeedResponse.Entities>,
        val entityId: String,
    )

}

