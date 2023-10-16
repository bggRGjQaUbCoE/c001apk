package com.example.c001apk.logic.model

import com.google.gson.annotations.SerializedName

data class SearchTopicResponse(val data: List<Data>) {

    data class Data(
        val logo: String, val title: String, val id: String, val entityType: String,
        @SerializedName("hot_num") val hotNum:String, val commentnum:String
    )

}

