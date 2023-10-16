package com.example.c001apk.logic.network

import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.model.TopicLayoutResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface TopicService {

    @GET("/v6/topic/newTagDetail?tmp=1")
    fun getTopicLayout(
        @Query("tag") tag: String
        //@Path("TAG") TAG: String
    ): Call<TopicLayoutResponse>

    @GET("/v6/page/dataList??tmp=1")
    fun getTopicData(
        @Query("url") url: String,
        @Query("title") title: String,
        @Query("subTitle") subTitle: String?,
        @Query("page") page: Int
    ): Call<HomeFeedResponse>


}