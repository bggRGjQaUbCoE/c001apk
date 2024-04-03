package com.example.c001apk.logic.repository

import com.example.c001apk.di.AccountService
import com.example.c001apk.di.Api1Service
import com.example.c001apk.di.Api1ServiceNoRedirect
import com.example.c001apk.di.Api2Service
import com.example.c001apk.logic.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton
class NetworkRepo @Inject constructor(
    @Api1Service
    private val apiService: ApiService,
    @Api1ServiceNoRedirect
    private val apiServiceNoRedirect: ApiService,
    @Api2Service
    private val api2Service: ApiService,
    @AccountService
    private val accountService: ApiService,
) {

    suspend fun getHomeFeed(
        page: Int,
        firstLaunch: Int,
        installTime: String,
        firstItem: String?,
        lastItem: String?
    ) = fire {
        Result.success(
            api2Service.getHomeFeed(page, firstLaunch, installTime, firstItem, lastItem).await()
        )
    }

    suspend fun getFeedContent(id: String, rid: String?) = fire {
        Result.success(api2Service.getFeedContent(id, rid).await())
    }

    suspend fun getFeedContentReply(
        id: String,
        listType: String,
        page: Int,
        firstItem: String?,
        lastItem: String?,
        discussMode: Int,
        feedType: String,
        blockStatus: Int,
        fromFeedAuthor: Int
    ) = fire {
        Result.success(
            api2Service.getFeedContentReply(
                id,
                listType,
                page,
                firstItem,
                lastItem,
                discussMode,
                feedType,
                blockStatus,
                fromFeedAuthor
            ).await()
        )
    }

    suspend fun getSearch(
        type: String, feedType: String, sort: String, keyWord: String, pageType: String?,
        pageParam: String?, page: Int, lastItem: String?
    ) = fire {
        Result.success(
            apiService.getSearch(
                type, feedType, sort, keyWord, pageType, pageParam, page, lastItem
            ).await()
        )
    }

    suspend fun getReply2Reply(id: String, page: Int, lastItem: String?) = fire {
        Result.success(apiService.getReply2Reply(id, page, lastItem).await())
    }

    suspend fun getTopicLayout(tag: String) = fire {
        Result.success(api2Service.getTopicLayout(tag).await())
    }

    suspend fun getProductLayout(id: String) = fire {
        Result.success(apiService.getProductLayout(id).await())
    }

    suspend fun getUserSpace(uid: String) = fire {
        Result.success(apiService.getUserSpace(uid).await())
    }

    suspend fun getUserFeed(uid: String, page: Int, lastItem: String?) = fire {
        Result.success(apiService.getUserFeed(uid, page, lastItem).await())
    }

    suspend fun getAppInfo(id: String) = fire {
        Result.success(apiService.getAppInfo(id).await())
    }

    suspend fun getAppDownloadLink(pn: String, aid: String, vc: String) = fire {
        val appResponse = apiServiceNoRedirect.getAppDownloadLink(pn, aid, vc).response()
        Result.success(appResponse.headers()["Location"])
    }

    suspend fun getAppsUpdate(pkgs: String) = fire {
        val multipartBody =
            MultipartBody.Part.createFormData("pkgs", pkgs)
        val appResponse = apiService.getAppsUpdate(multipartBody).await()
        Result.success(appResponse.data)
    }

    suspend fun getProfile(uid: String) = fire {
        Result.success(api2Service.getProfile(uid).await())
    }

    suspend fun getFollowList(url: String, uid: String, page: Int, lastItem: String?) = fire {
        Result.success(apiService.getFollowList(url, uid, page, lastItem).await())
    }

    suspend fun postLikeFeed(url: String, id: String) = fire {
        Result.success(apiService.postLikeFeed(url, id).await())
    }

    suspend fun postLikeReply(url: String, id: String) = fire {
        Result.success(apiService.postLikeReply(url, id).await())
    }

    suspend fun checkLoginInfo() = fire {
        Result.success(apiService.checkLoginInfo().response())
    }

    suspend fun preGetLoginParam() = fire {
        Result.success(accountService.preGetLoginParam().response())
    }

    suspend fun getLoginParam() = fire {
        Result.success(accountService.getLoginParam().response())
    }

    suspend fun tryLogin(data: HashMap<String, String?>) = fire {
        Result.success(accountService.tryLogin(data).response())
    }

    suspend fun getCaptcha(url: String) = fire {
        Result.success(accountService.getCaptcha(url).response())
    }

    suspend fun getValidateCaptcha(url: String) = fire {
        Result.success(apiService.getValidateCaptcha(url).response())
    }

    suspend fun postReply(data: HashMap<String, String>, id: String, type: String) = fire {
        Result.success(apiService.postReply(data, id, type).await())
    }

    suspend fun getDataList(
        url: String, title: String, subTitle: String?, lastItem: String?, page: Int
    ) = fire {
        Result.success(apiService.getDataList(url, title, subTitle, lastItem, page).await())
    }

    suspend fun getDyhDetail(dyhId: String, type: String, page: Int, lastItem: String?) =
        fire {
            Result.success(apiService.getDyhDetail(dyhId, type, page, lastItem).await())
        }

    suspend fun getMessage(url: String, page: Int, lastItem: String?) = fire {
        Result.success(apiService.getMessage(url, page, lastItem).await())
    }

    suspend fun postFollowUnFollow(url: String, uid: String) = fire {
        Result.success(apiService.postFollowUnFollow(url, uid).await())
    }

    suspend fun postCreateFeed(data: HashMap<String, String?>) = fire {
        Result.success(apiService.postCreateFeed(data).await())
    }

    suspend fun postRequestValidate(data: HashMap<String, String?>) = fire {
        Result.success(apiService.postRequestValidate(data).await())
    }

    suspend fun getVoteComment(
        fid: String,
        extraKey: String,
        page: Int,
        firstItem: String?,
        lastItem: String?,
    ) = fire {
        Result.success(apiService.getVoteComment(fid, extraKey, page, firstItem, lastItem).await())
    }

    suspend fun getAnswerList(
        id: String,
        sort: String,
        page: Int,
        firstItem: String?,
        lastItem: String?,
    ) = fire {
        Result.success(apiService.getAnswerList(id, sort, page, firstItem, lastItem).await())
    }

    suspend fun getProductList() = fire {
        Result.success(apiService.getProductList().await())
    }

    suspend fun getCollectionList(
        url: String,
        uid: String?,
        id: String?,
        showDefault: Int,
        page: Int,
        lastItem: String?
    ) = fire {
        Result.success(
            apiService.getCollectionList(url, uid, id, showDefault, page, lastItem).await()
        )
    }

    suspend fun postDelete(url: String, id: String) = fire {
        Result.success(apiService.postDelete(url, id).await())
    }

    suspend fun postFollow(data: HashMap<String, String>) = fire {
        Result.success(apiService.postFollow(data).await())
    }

    suspend fun getFollow(url: String, tag: String?, id: String?) = fire {
        Result.success(apiService.getFollow(url, tag, id).await())
    }

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

    private fun <T> fire(block: suspend () -> Result<T>) =
        flow {
            val result = try {
                block()
            } catch (e: Exception) {
                Result.failure(e)
            }
            emit(result)
        }.flowOn(Dispatchers.IO)

}