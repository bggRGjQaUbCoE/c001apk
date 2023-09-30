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