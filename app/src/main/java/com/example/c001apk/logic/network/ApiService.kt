package com.example.c001apk.logic.network

import androidx.webkit.internal.ApiFeature.T
import com.example.c001apk.logic.model.FeedContentResponse
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.model.TotalReplyResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("/v6/main/indexV8?ids=")
    fun getHomeFeed(
        @Query("page") page: Int,
        @Query("firstLaunch") firstLaunch: Int,
        @Query("installTime") installTime: String,
        @Query("lastItem") lastItem: String,
    ): Call<HomeFeedResponse>

    @GET("/v6/feed/detail")
    fun getFeedContent(
        @Query("id") id: String
    ): Call<FeedContentResponse>

    @GET("/v6/feed/replyList")
    fun getFeedContentReply(
        @Query("id") id: String,
        @Query("discussMode") discussMode: Int,
        @Query("listType") listType: String,
        @Query("page") page: Int
    ): Call<TotalReplyResponse>

    @GET("/v6/search?showAnonymous=-1")
    fun getSearch(
        @Query("type") type: String,
        @Query("feedType") feedType: String,
        @Query("sort") sort: String,
        @Query("searchValue") keyWord: String,
        @Query("page") page: Int
    ): Call<HomeFeedResponse>

    @GET("/v6/feed/replyList?listType=&discussMode=0&feedType=feed_reply&blockStatus=0&fromFeedAuthor=0")
    fun getReply2Reply(
        @Query("id") id: String,
        @Query("page") page: Int
    ): Call<TotalReplyResponse>

    @GET("/v6/page/dataList?url=%2Fpage%3Furl%3DV11_VERTICAL_TOPIC&title=%E8%AF%9D%E9%A2%98&subTitle=&page=1")
    fun getHomeTopicTitle(
    ): Call<HomeFeedResponse>

    @GET("/v6/page/dataList?url=%2Fpage%3Furl%3DV9_HOME_TAB_RANKING&title=%E7%83%AD%E6%A6%9C&subTitle=")
    fun getHomeRanking(
        @Query("page") page: Int,
        @Query("lastItem") lastItem: String
    ): Call<HomeFeedResponse>

    @GET("/v6/user/space")
    fun getUserSpace(
        @Query("uid") uid: String,
    ): Call<FeedContentResponse>

    @GET("/v6/user/feedList?showAnonymous=0&isIncludeTop=1&showDoing=0")
    fun getUserFeed(
        @Query("uid") uid: String,
        @Query("page") page: Int,
    ): Call<HomeFeedResponse>

    @GET("/v6/apk/detail?installed=1")
    fun getAppInfo(
        @Query("id") id: String,
    ): Call<FeedContentResponse>

    @GET("/v6/page/dataList?title=%E7%82%B9%E8%AF%84&subTitle=")
    fun getAppComment(
        @Query("url") url: String,
        @Query("page") page: Int
    ): Call<HomeFeedResponse>

    @GET("/v6/topic/newTagDetail")
    fun getTopicLayout(
        @Query("tag") tag: String
        //@Path("TAG") TAG: String
    ): Call<FeedContentResponse>

    @GET("/v6/page/dataList")
    fun getTopicData(
        @Query("url") url: String,
        @Query("title") title: String,
        @Query("subTitle") subTitle: String?,
        @Query("page") page: Int
    ): Call<HomeFeedResponse>

    @GET("/v6/user/profile")
    fun getProfile(
        @Query("uid") uid: String
    ): Call<FeedContentResponse>

    @GET("/v6/page/dataList?/page?subTitle=")
    fun getFollowFeed(
        @Query("url") url: String,
        @Query("title") title: String,
        @Query("page") page: Int
    ): Call<HomeFeedResponse>

    @GET("/v6/user/feedList?showAnonymous=0&isIncludeTop=1")
    fun getFeedList(
        @Query("uid") uid: String,
        @Query("page") page: Int
    ): Call<HomeFeedResponse>

    @GET("/v6/user/followList")
    fun getFollowList(
        @Query("uid") uid: String,
        @Query("page") page: Int
    ): Call<HomeFeedResponse>

    @GET("/v6/user/fansList")
    fun getFansList(
        @Query("uid") uid: String,
        @Query("page") page: Int
    ): Call<HomeFeedResponse>

}