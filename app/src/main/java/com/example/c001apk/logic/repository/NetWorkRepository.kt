package com.example.c001apk.logic.repository

import com.example.c001apk.di.AccountService
import com.example.c001apk.di.Api1Service
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
class NetWorkRepository @Inject constructor(
    @Api1Service
    private val apiService: ApiService,
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
            api2Service.getSearch(
                type, feedType, sort, keyWord, pageType, pageParam, page, lastItem
            )
        )
    }

    suspend fun getReply2Reply(id: String, page: Int, lastItem: String?) = fire {
        Result.success(api2Service.getReply2Reply(id, page, lastItem).await())
    }

    suspend fun getTopicLayout(tag: String) = fire {
        Result.success(api2Service.getTopicLayout(tag).await())
    }

    suspend fun getProductLayout(id: String) = fire {
        Result.success(api2Service.getProductLayout(id).await())
    }

    suspend fun getUserSpace(uid: String) = fire {
        Result.success(api2Service.getUserSpace(uid).await())
    }

    suspend fun getUserFeed(uid: String, page: Int, lastItem: String?) = fire {
        Result.success(api2Service.getUserFeed(uid, page, lastItem).await())
    }

    suspend fun getAppInfo(id: String) = fire {
        Result.success(api2Service.getAppInfo(id).await())
    }

    suspend fun getAppDownloadLink(pn: String, aid: String, vc: String) = fire {
        val appResponse = api2Service.getAppDownloadLink(pn, aid, vc).response()
        Result.success(appResponse.headers()["Location"])
    }

    suspend fun getAppsUpdate(pkgs: String) = fire {
        val multipartBody =
            MultipartBody.Part.createFormData("pkgs", pkgs)
        val appResponse = api2Service.getAppsUpdate(multipartBody).await()
        Result.success(appResponse.data)
    }

    suspend fun getProfile(uid: String) = fire {
        Result.success(api2Service.getProfile(uid).await())
    }

    suspend fun getFollowList(url: String, uid: String, page: Int, lastItem: String?) = fire {
        Result.success(api2Service.getFollowList(url, uid, page, lastItem).await())
    }

    suspend fun postLikeFeed(url: String, id: String) = fire {
        Result.success(api2Service.postLikeFeed(url, id).await())
    }

    suspend fun postLikeReply(url: String, id: String) = fire {
        Result.success(api2Service.postLikeReply(url, id).await())
    }

    suspend fun checkLoginInfo() = fire {
        Result.success(api2Service.checkLoginInfo().await())
    }

    suspend fun preGetLoginParam() = fire {
        Result.success(api2Service.preGetLoginParam().await())
    }

    suspend fun getLoginParam() = fire {
        Result.success(api2Service.getLoginParam().await())
    }

    suspend fun tryLogin(data: HashMap<String, String?>) = fire {
        Result.success(api2Service.tryLogin(data).await())
    }

    suspend fun getCaptcha(url: String) = fire {
        Result.success(api2Service.getCaptcha(url).await())
    }

    suspend fun getValidateCaptcha(url: String) = fire {
        Result.success(api2Service.getValidateCaptcha(url).await())
    }

    suspend fun postReply(data: HashMap<String, String>, id: String, type: String) = fire {
        Result.success(api2Service.postReply(data, id, type).await())
    }

    suspend fun getDataList(
        url: String, title: String, subTitle: String?, lastItem: String?, page: Int
    ) = fire {
        Result.success(api2Service.getDataList(url, title, subTitle, lastItem, page).await())
    }

    suspend fun getDyhDetail(dyhId: String, type: String, page: Int, lastItem: String?) =
        fire {
            Result.success(api2Service.getDyhDetail(dyhId, type, page, lastItem).await())
        }

    suspend fun getMessage(url: String, page: Int, lastItem: String?) = fire {
        Result.success(api2Service.getMessage(url, page, lastItem).await())
    }

    suspend fun postFollowUnFollow(url: String, uid: String) = fire {
        Result.success(api2Service.postFollowUnFollow(url, uid).await())
    }

    suspend fun postCreateFeed(data: HashMap<String, String?>) = fire {
        Result.success(api2Service.postCreateFeed(data).await())
    }

    suspend fun postRequestValidate(data: HashMap<String, String?>) = fire {
        Result.success(api2Service.postRequestValidate(data).await())
    }

    suspend fun getProductList() = fire {
        Result.success(api2Service.getProductList().await())
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
            api2Service.getCollectionList(url, uid, id, showDefault, page, lastItem).await()
        )
    }

    suspend fun postDelete(url: String, id: String) = fire {
        Result.success(api2Service.postDelete(url, id).await())
    }

    suspend fun postFollow(data: HashMap<String, String>) = fire {
        Result.success(api2Service.postFollow(data).await())
    }

    suspend fun getFollow(url: String, tag: String?, id: String?) = fire {
        Result.success(api2Service.getFollow(url, tag, id).await())
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