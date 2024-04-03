package com.example.c001apk.logic.model

import com.google.gson.annotations.SerializedName

data class UserProfileResponse(
    val status: Int?,
    val error: Int?,
    val message: String?,
    val messageStatus: Int?,
    val data: Data?
) {
    data class Data(
        val uid: String,
        val gender: Int,
        val regdate: Long,
        val cover: String,
        var isFollow: Int?,
        val bio: String?,
        @SerializedName("be_like_num") val beLikeNum: String,
        val logintime: Long,
        val feed: String,
        val follow: String,
        val fans: String,
        val username: String,
        val userAvatar: String,
        val level: String,
        val experience: Int,
        @SerializedName("next_level_experience") val nextLevelExperience: Int,
    )
}

