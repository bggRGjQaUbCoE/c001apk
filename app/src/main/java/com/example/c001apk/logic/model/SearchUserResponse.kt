package com.example.c001apk.logic.model

import com.google.gson.annotations.SerializedName

data class SearchUserResponse(val data: List<Data>) {

    data class Data(
        val uid: String, val username: String, val follow: String, val level: String,
        val fans: String, val logintime: String, val userAvatar:String,
        val experience:Int, val regdate:String,
        @SerializedName("next_level_experience") val nextLevelExperience:Int,
        val bio:String, val feed:String, val gender:Int, val city:String,
    )

}

