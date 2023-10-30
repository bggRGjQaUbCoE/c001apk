package com.example.c001apk.logic.network

import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object Network {

    private val apiService = ApiServiceCreator.create<ApiService>()
    private val api2Service = Api2ServiceCreator.create<ApiService>()
    private val accountService = AccountServiceCreator.create<ApiService>()

    suspend fun getHomeFeed(page: Int, firstLaunch: Int, installTime: String, lastItem: String) =
        api2Service.getHomeFeed(page, firstLaunch, installTime, lastItem).await()

    suspend fun getFeedContent(id: String) =
        api2Service.getFeedContent(id).await()

    suspend fun getFeedContentReply(id: String, discussMode: Int, listType: String, page: Int) =
        api2Service.getFeedContentReply(id, discussMode, listType, page).await()

    suspend fun getSearch(
        type: String,
        feedType: String,
        sort: String,
        keyWord: String,
        page: Int
    ) = apiService.getSearch(type, feedType, sort, keyWord, page).await()

    suspend fun getReply2Reply(
        id: String,
        page: Int
    ) = apiService.getReply2Reply(id, page).await()

    suspend fun getHomeTopicTitle() = apiService.getHomeTopicTitle().await()

    suspend fun getTopicLayout(tag: String) = api2Service.getTopicLayout(tag).await()

    suspend fun getTopicData(url: String, title: String, subTitle: String?, page: Int) =
        api2Service.getTopicData(url, title, subTitle, page).await()

    suspend fun getHomeRanking(page: Int, lastItem: String) =
        apiService.getHomeRanking(page, lastItem).await()

    suspend fun getUserSpace(uid: String) =
        apiService.getUserSpace(uid).await()

    suspend fun getUserFeed(uid: String, page: Int) =
        apiService.getUserFeed(uid, page).await()

    suspend fun getAppInfo(id: String) =
        apiService.getAppInfo(id).await()

    suspend fun getAppComment(url: String, page: Int) =
        apiService.getAppComment(url, page).await()

    suspend fun getProfile(uid: String) =
        api2Service.getProfile(uid).await()

    suspend fun getFollowFeed(url: String, title: String, page: Int) =
        apiService.getFollowFeed(url, title, page).await()

    suspend fun getFeedList(uid: String, page: Int) =
        apiService.getFeedList(uid, page).await()

    suspend fun getFollowList(uid: String, page: Int) =
        apiService.getFollowList(uid, page).await()

    suspend fun getFansList(uid: String, page: Int) =
        apiService.getFansList(uid, page).await()

    suspend fun postLikeFeed(id: String) =
        apiService.postLikeFeed(id).await()

    suspend fun postUnLikeFeed(id: String) =
        apiService.postUnLikeFeed(id).await()

    suspend fun postLikeReply(id: String) =
        apiService.postLikeReply(id).await()

    suspend fun postUnLikeReply(id: String) =
        apiService.postUnLikeReply(id).await()

    suspend fun checkLoginInfo() =
        apiService.checkLoginInfo().response()

    suspend fun getLoginParam() =
        accountService.getLoginParam().response()

    suspend fun tryLogin(data: HashMap<String, String?>) =
        accountService.tryLogin(data).response()

    suspend fun getCaptcha(url: String) =
        accountService.getCaptcha(url).response()

    private suspend fun <T> Call<T>.await(): T {
        return suspendCoroutine { continuation ->
            enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    if (body != null) continuation.resume(body)
                    else continuation.resumeWithException(
                        RuntimeException("response body is null")
                    )
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })
        }
    }

    private suspend fun <T> Call<T>.response(): Response<T> {
        return suspendCoroutine { continuation ->
            enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    continuation.resume(response)
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })
        }
    }

}