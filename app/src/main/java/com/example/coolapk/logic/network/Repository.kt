package com.example.coolapk.logic.network

import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

object Repository {

    fun getHomeFeed(page: Int, firstLaunch: Int) = fire(Dispatchers.IO) {
        val homeFeedResponse = Network.getHomeFeed(page, firstLaunch)
        if (homeFeedResponse.data.isNotEmpty())
            Result.success(homeFeedResponse.data)
        else
            Result.failure(RuntimeException("response status is null"))
    }

    fun getFeedContent(id: String) = fire(Dispatchers.IO) {
        val feedResponse = Network.getFeedContent(id)
        if (feedResponse.data != null)
            Result.success(feedResponse)
        else
            Result.failure(RuntimeException("response status is null"))
    }

    fun getFeedContentReply(id: String, discussMode: Int, listType: String, page: Int) =
        fire(Dispatchers.IO) {
            val feedReplyResponse = Network.getFeedContentReply(id, discussMode, listType, page)
            if (feedReplyResponse.data.isNotEmpty())
                Result.success(feedReplyResponse.data)
            else
                Result.failure(RuntimeException("response status is null"))
        }

    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData<Result<T>>(context) {
            val result = try {
                block()
            } catch (e: Exception) {
                Result.failure<T>(e)
            }
            emit(result)
        }

}