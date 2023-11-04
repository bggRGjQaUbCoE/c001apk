package com.example.c001apk.logic.network

import com.example.c001apk.logic.model.CheckResponse
import com.example.c001apk.logic.model.FeedContentResponse
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.model.LikeFeedResponse
import com.example.c001apk.logic.model.LikeReplyResponse
import com.example.c001apk.logic.model.TotalReplyResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url


interface ApiService {

    @GET("/v6/main/indexV8?ids=")
    fun getHomeFeed(
        @Query("page") page: Int,
        @Query("firstLaunch") firstLaunch: Int,
        @Query("installTime") installTime: String,
        @Query("firstItem") firstItem: String,
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

    @GET("/v6/topic/newTagDetail")
    fun getTopicLayout(
        @Query("tag") tag: String
    ): Call<FeedContentResponse>

    @GET("/v6/product/detail")
    fun getProductLayout(
        @Query("id") id: String
    ): Call<FeedContentResponse>

    @GET("/v6/user/profile")
    fun getProfile(
        @Query("uid") uid: String
    ): Call<FeedContentResponse>


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

    @POST("/v6/feed/like")
    fun postLikeFeed(
        @Query("id") id: String
    ): Call<LikeFeedResponse>

    @POST("/v6/feed/unlike")
    fun postUnLikeFeed(
        @Query("id") id: String
    ): Call<LikeFeedResponse>

    @POST("/v6/feed/likeReply")
    fun postLikeReply(
        @Query("id") id: String
    ): Call<LikeReplyResponse>

    @POST("/v6/feed/unLikeReply")
    fun postUnLikeReply(
        @Query("id") id: String
    ): Call<LikeReplyResponse>

    @GET("/v6/account/checkLoginInfo")
    fun checkLoginInfo(
    ): Call<CheckResponse>

    @GET("/auth/loginByCoolApk")
    fun getLoginParam(
    ): Call<ResponseBody>

    @POST("/auth/loginByCoolApk")
    @FormUrlEncoded
    fun tryLogin(@FieldMap data: HashMap<String, String?>): Call<ResponseBody>

    @GET
    fun getCaptcha(@Url url: String): Call<ResponseBody>

    @POST("v6/feed/reply")
    @FormUrlEncoded
    fun postReply(
        @FieldMap data: HashMap<String, String>,
        @Query("id") id: String,
        @Query("type") type: String
    ): Call<CheckResponse>

    @GET("/v6/page/dataList")
    fun getDataList(
        @Query("url") url: String,
        @Query("title") title: String,
        @Query("subTitle") subTitle: String,
        @Query("lastItem") lastItem: String,
        @Query("page") page: Int
    ): Call<HomeFeedResponse>

    @GET("/v6/dyhArticle/list")
    fun getDyhDetail(
        @Query("dyhId") dyhId: String,
        @Query("type") type: String,
        @Query("page") page: Int
    ): Call<HomeFeedResponse>

    @GET("/auth/login")
    fun getSmsLoginParam(
        @Query("type") type: String = "mobile",
    ): Call<ResponseBody>

    @POST("/auth/login")
    @FormUrlEncoded
    fun getSmsToken(
        @Query("type") type: String = "mobile",
        @FieldMap data: HashMap<String, String?>
    ): Call<ResponseBody>

}