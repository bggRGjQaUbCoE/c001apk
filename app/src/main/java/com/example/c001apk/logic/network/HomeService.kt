package com.example.c001apk.logic.network

import com.example.c001apk.logic.model.FeedContentResponse
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.model.SearchUserResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface HomeService {

    @GET("/v6/main/indexV8?ids=")
    fun getHomeFeed(
        @Query("page") page: Int,
        @Query("firstLaunch") firstLaunch: Int,
        @Query("installTime") installTime: String,
        @Query("lastItem") lastItem: String,
    ): Call<HomeFeedResponse>

    @GET("/v6/feed/detail?tmp=1")
    fun getFeedContent(
        @Query("id") id: String
    ): Call<FeedContentResponse>

    @GET("/v6/feed/replyList?tmp=1")
    fun getFeedContentReply(
        @Query("id") id: String,
        @Query("discussMode") discussMode: Int,
        @Query("listType") listType: String,
        @Query("page") page: Int
    ): Call<HomeFeedResponse>

    @GET("/v6/search?showAnonymous=-1")
    fun getSearchFeed(
        @Query("type") type: String,
        @Query("feedType") feedType: String,
        @Query("sort") sort: String,
        @Query("searchValue") keyWord: String,
        @Query("page") page: Int
    ): Call<HomeFeedResponse>

    @GET("/v6/search?showAnonymous=-1&type=user")
    fun getSearchUser(
        @Query("searchValue") keyWord: String,
        @Query("page") page: Int
    ): Call<SearchUserResponse>

    @GET("/v6/feed/replyList?listType=&discussMode=0&feedType=feed_reply&blockStatus=0&fromFeedAuthor=0")
    fun getReply2Reply(
        @Query("id") id: String,
        @Query("page") page: Int
    ): Call<HomeFeedResponse>

}