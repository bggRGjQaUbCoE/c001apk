package com.example.coolapk.logic.network

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object Network {

    private val homeService = ServiceCreator.create<HomeService>()
    private val searchService = SearchServiceCreator.create<HomeService>()

    suspend fun getHomeFeed(page: Int, firstLaunch: Int, installTime: String, lastItem: String) =
        homeService.getHomeFeed(page, firstLaunch, installTime, lastItem).await()

    suspend fun getFeedContent(id: String) =
        homeService.getFeedContent(id).await()

    suspend fun getFeedContentReply(id: String, discussMode: Int, listType: String, page: Int) =
        homeService.getFeedContentReply(id, discussMode, listType, page).await()

    suspend fun getSearch(
        type: String,
        feedType: String,
        sort: String,
        keyWord: String,
        page: Int
    ) = searchService.getSearch(type, feedType, sort, keyWord, page).await()

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

}