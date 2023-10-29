package com.example.c001apk.logic.network

import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

object Repository {

    fun getHomeFeed(page: Int, firstLaunch: Int, installTime: String, lastItem: String) =
        fire(Dispatchers.IO) {
            val homeFeedResponse = Network.getHomeFeed(page, firstLaunch, installTime, lastItem)
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

    fun getSearch(type: String, feedType: String, sort: String, keyWord: String, page: Int) =
        fire(Dispatchers.IO) {
            val searchResponse = Network.getSearch(type, feedType, sort, keyWord, page)
            if (searchResponse.data.isNotEmpty())
                Result.success(searchResponse.data)
            else
                Result.failure(RuntimeException("response status is null"))
        }

    fun getReply2Reply(id: String, page: Int) =
        fire(Dispatchers.IO) {
            val searchResponse = Network.getReply2Reply(id, page)
            if (searchResponse.data.isNotEmpty())
                Result.success(searchResponse.data)
            else
                Result.failure(RuntimeException("response status is null"))
        }

    fun getHomeTopicTitle() =
        fire(Dispatchers.IO) {
            val searchResponse = Network.getHomeTopicTitle()
            if (searchResponse.data.isNotEmpty())
                Result.success(searchResponse.data)
            else
                Result.failure(RuntimeException("response status is null"))
        }

    fun getTopicLayout(tag: String) =
        fire(Dispatchers.IO) {
            val topicLayoutResponse = Network.getTopicLayout(tag)
            if (topicLayoutResponse.data != null)
                Result.success(topicLayoutResponse.data)
            else
                Result.failure(RuntimeException("response status is null"))
        }

    fun getTopicData(url: String, title: String, subTitle: String?, page: Int) =
        fire(Dispatchers.IO) {
            val topicDataResponse = Network.getTopicData(url, title, subTitle, page)
            if (topicDataResponse.data.isNotEmpty())
                Result.success(topicDataResponse.data)
            else
                Result.failure(RuntimeException("response status is null"))
        }

    fun getHomeRanking(page: Int, lastItem: String) =
        fire(Dispatchers.IO) {
            val topicDataResponse = Network.getHomeRanking(page, lastItem)
            if (topicDataResponse.data.isNotEmpty())
                Result.success(topicDataResponse.data)
            else
                Result.failure(RuntimeException("response status is null"))
        }

    fun getUserSpace(uid: String) =
        fire(Dispatchers.IO) {
            val userResponse = Network.getUserSpace(uid)
            if (userResponse.data != null)
                Result.success(userResponse.data)
            else
                Result.failure(RuntimeException("response status is null"))
        }

    fun getUserFeed(uid: String, page: Int) =
        fire(Dispatchers.IO) {
            val userResponse = Network.getUserFeed(uid, page)
            if (userResponse.data.isNotEmpty())
                Result.success(userResponse.data)
            else
                Result.failure(RuntimeException("response status is null"))
        }

    fun getAppInfo(id: String) =
        fire(Dispatchers.IO) {
            val appResponse = Network.getAppInfo(id)
            if (appResponse.data != null)
                Result.success(appResponse.data)
            else
                Result.failure(RuntimeException("response status is null"))
        }

    fun getAppComment(url: String, page: Int) =
        fire(Dispatchers.IO) {
            val appResponse = Network.getAppComment(url, page)
            if (appResponse.data.isNotEmpty())
                Result.success(appResponse.data)
            else
                Result.failure(RuntimeException("response status is null"))
        }

    fun getProfile(uid: String) =
        fire(Dispatchers.IO) {
            val profileResponse = Network.getProfile(uid)
            if (profileResponse.data != null)
                Result.success(profileResponse.data)
            else
                Result.failure(RuntimeException("response status is null"))
        }

    fun getFollowFeed(url: String, title: String, page: Int) =
        fire(Dispatchers.IO) {
            val feedResponse = Network.getFollowFeed(url, title, page)
            if (feedResponse.data.isNotEmpty())
                Result.success(feedResponse.data)
            else
                Result.failure(RuntimeException("response status is null"))
        }

    fun getFeedList(uid: String, page: Int) =
        fire(Dispatchers.IO) {
            val feedResponse = Network.getFeedList(uid, page)
            if (feedResponse.data.isNotEmpty())
                Result.success(feedResponse.data)
            else
                Result.failure(RuntimeException("response status is null"))
        }

    fun getFollowList(uid: String, page: Int) =
        fire(Dispatchers.IO) {
            val feedResponse = Network.getFollowList(uid, page)
            if (feedResponse.data.isNotEmpty())
                Result.success(feedResponse.data)
            else
                Result.failure(RuntimeException("response status is null"))
        }

    fun getFansList(uid: String, page: Int) =
        fire(Dispatchers.IO) {
            val feedResponse = Network.getFansList(uid, page)
            if (feedResponse.data.isNotEmpty())
                Result.success(feedResponse.data)
            else
                Result.failure(RuntimeException("response status is null"))
        }

    fun postLikeFeed(id: String) =
        fire(Dispatchers.IO) {
            val response = Network.postLikeFeed(id)
            if (response != null)
                Result.success(response)
            else
                Result.failure(RuntimeException("response status is null"))
        }

    fun postUnLikeFeed(id: String) =
        fire(Dispatchers.IO) {
            val response = Network.postUnLikeFeed(id)
            if (response != null)
                Result.success(response)
            else
                Result.failure(RuntimeException("response status is null"))
        }

    fun postLikeReply(id: String) =
        fire(Dispatchers.IO) {
            val response = Network.postLikeReply(id)
            if (response != null)
                Result.success(response)
            else
                Result.failure(RuntimeException("response status is null"))
        }

    fun postUnLikeReply(id: String) =
        fire(Dispatchers.IO) {
            val response = Network.postUnLikeReply(id)
            if (response != null)
                Result.success(response)
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