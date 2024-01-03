package com.example.c001apk.logic.network

import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import okhttp3.MultipartBody
import kotlin.coroutines.CoroutineContext

object Repository {

    fun getHomeFeed(
        page: Int,
        firstLaunch: Int,
        installTime: String,
        firstItem: String?,
        lastItem: String?
    ) =
        fire(Dispatchers.IO) {
            Result.success(Network.getHomeFeed(page, firstLaunch, installTime, firstItem, lastItem))
        }

    fun getFeedContent(id: String, rid: String?) = fire(Dispatchers.IO) {
        Result.success(Network.getFeedContent(id, rid))
    }

    fun getFeedContentReply(
        id: String,
        listType: String,
        page: Int,
        firstItem: String?,
        lastItem: String?,
        discussMode: Int,
        feedType: String,
        blockStatus: Int,
        fromFeedAuthor: Int
    ) =
        fire(Dispatchers.IO) {
            Result.success(
                Network.getFeedContentReply(
                    id,
                    listType,
                    page,
                    firstItem,
                    lastItem,
                    discussMode,
                    feedType,
                    blockStatus,
                    fromFeedAuthor
                )
            )
        }

    fun getSearch(
        type: String,
        feedType: String,
        sort: String,
        keyWord: String,
        pageType: String,
        pageParam: String,
        page: Int,
        showAnonymous: Int
    ) =
        fire(Dispatchers.IO) {
            Result.success(
                Network.getSearch(
                    type,
                    feedType,
                    sort,
                    keyWord,
                    pageType,
                    pageParam,
                    page,
                    showAnonymous
                )
            )
        }

    fun getReply2Reply(id: String, page: Int) =
        fire(Dispatchers.IO) {
            Result.success(Network.getReply2Reply(id, page))
        }

    fun getTopicLayout(tag: String) =
        fire(Dispatchers.IO) {
            Result.success(Network.getTopicLayout(tag))
        }

    fun getProductLayout(id: String) =
        fire(Dispatchers.IO) {
            Result.success(Network.getProductLayout(id))
        }

    fun getUserSpace(uid: String) =
        fire(Dispatchers.IO) {
            Result.success(Network.getUserSpace(uid))
        }

    fun getUserFeed(uid: String, page: Int) =
        fire(Dispatchers.IO) {
            Result.success(Network.getUserFeed(uid, page))
        }

    fun getAppInfo(id: String) =
        fire(Dispatchers.IO) {
            Result.success(Network.getAppInfo(id))
        }

    fun getAppDownloadLink(pn: String, aid: String, vc: String) =
        fire(Dispatchers.IO) {
            val appResponse = Network.getAppDownloadLink(pn, aid, vc)
            Result.success(appResponse.headers()["Location"])
        }

    fun getAppsUpdate(pkgs: String) =
        fire(Dispatchers.IO) {
            val multipartBody =
                MultipartBody.Part.createFormData("pkgs", pkgs)
            val appResponse = Network.getAppsUpdate(multipartBody)
            Result.success(appResponse.data)
        }

    fun getProfile(uid: String) =
        fire(Dispatchers.IO) {
            Result.success(Network.getProfile(uid))
        }

    fun getFollowList(url: String, uid: String, page: Int) =
        fire(Dispatchers.IO) {
            Result.success(Network.getFollowList(url, uid, page))
        }

    fun postLikeFeed(id: String) =
        fire(Dispatchers.IO) {
            Result.success(Network.postLikeFeed(id))
        }

    fun postUnLikeFeed(id: String) =
        fire(Dispatchers.IO) {
            Result.success(Network.postUnLikeFeed(id))
        }

    fun postLikeReply(id: String) =
        fire(Dispatchers.IO) {
            Result.success(Network.postLikeReply(id))
        }

    fun postUnLikeReply(id: String) =
        fire(Dispatchers.IO) {
            Result.success(Network.postUnLikeReply(id))
        }

    fun checkLoginInfo() =
        fire(Dispatchers.IO) {
            Result.success(Network.checkLoginInfo())
        }

    fun preGetLoginParam() =
        fire(Dispatchers.IO) {
            Result.success(Network.preGetLoginParam())
        }

    fun getLoginParam() =
        fire(Dispatchers.IO) {
            Result.success(Network.getLoginParam())
        }

    fun tryLogin(data: HashMap<String, String?>) =
        fire(Dispatchers.IO) {
            Result.success(Network.tryLogin(data))
        }

    fun getCaptcha(url: String) = fire(Dispatchers.IO) {
        Result.success(Network.getCaptcha(url))
    }

    fun getValidateCaptcha(url: String) = fire(Dispatchers.IO) {
        Result.success(Network.getValidateCaptcha(url))
    }

    fun postReply(data: HashMap<String, String>, id: String, type: String) = fire(Dispatchers.IO) {
        Result.success(Network.postReply(data, id, type))
    }

    fun getDataList(url: String, title: String, subTitle: String?, lastItem: String?, page: Int) =
        fire(Dispatchers.IO) {
            Result.success(Network.getDataList(url, title, subTitle, lastItem, page))
        }

    fun getDyhDetail(dyhId: String, type: String, page: Int) =
        fire(Dispatchers.IO) {
            Result.success(Network.getDyhDetail(dyhId, type, page))
        }

    fun getSmsToken(type: String, data: HashMap<String, String?>) =
        fire(Dispatchers.IO) {
            Result.success(Network.getSmsToken(type, data))
        }

    fun getSmsLoginParam(type: String) =
        fire(Dispatchers.IO) {
            Result.success(Network.getSmsLoginParam(type))
        }

    fun getMessage(url: String, page: Int) =
        fire(Dispatchers.IO) {
            Result.success(Network.getMessage(url, page))
        }

    fun postFollowUnFollow(url: String, uid: String) =
        fire(Dispatchers.IO) {
            Result.success(Network.postFollowUnFollow(url, uid))
        }

    fun postCreateFeed(data: HashMap<String, String?>) =
        fire(Dispatchers.IO) {
            Result.success(Network.postCreateFeed(data))
        }

    fun postRequestValidate(data: HashMap<String, String?>) =
        fire(Dispatchers.IO) {
            Result.success(Network.postRequestValidate(data))
        }

    fun getVoteComment(
        fid: String, extraKey: String, page: Int, firstItem: String?, lastItem: String?,
    ) = fire(Dispatchers.IO) {
        Result.success(Network.getVoteComment(fid, extraKey, page, firstItem, lastItem))
    }

    fun getProductList() =
        fire(Dispatchers.IO) {
            Result.success(Network.getProductList())
        }

    fun getCollectionList(url: String, uid: String?, id: String?, showDefault: Int, page: Int) =
        fire(Dispatchers.IO) {
            Result.success(Network.getCollectionList(url, uid, id, showDefault, page))
        }

    fun postDelete(url: String, id: String) =
        fire(Dispatchers.IO) {
            Result.success(Network.postDelete(url, id))
        }

    fun postFollow(data: HashMap<String, String>) =
        fire(Dispatchers.IO) {
            Result.success(Network.postFollow(data))
        }

    fun getFollow(url: String, tag: String?, id: String?) =
        fire(Dispatchers.IO) {
            Result.success(Network.getFollow(url, tag, id))
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