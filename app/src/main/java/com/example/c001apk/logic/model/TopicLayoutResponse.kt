package com.example.c001apk.logic.model

import com.google.gson.annotations.SerializedName

data class TopicLayoutResponse(val data: Data) {

    data class Data(
        val title: String,
        val logo: String,
        val entityType: String,
        val intro: String,
        @SerializedName("tag_pics") val tagPics: List<String>,
        val tabList: List<TabList>,
        val selectedTab: String
    )

    data class TabList(
        val title: String,
        val url: String,
        @SerializedName("page_name") val pageName: String,
        val entityType: String,
        val entityId: Int
    )

}

