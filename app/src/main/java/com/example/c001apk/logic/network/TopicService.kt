package com.example.c001apk.logic.network

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

}