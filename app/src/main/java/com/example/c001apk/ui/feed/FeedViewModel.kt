package com.example.c001apk.ui.feed

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.c001apk.adapter.FooterState
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.constant.Constants.LOADING_END
import com.example.c001apk.constant.Constants.LOADING_FAILED
import com.example.c001apk.logic.model.FeedArticleContentBean
import com.example.c001apk.logic.model.FeedEntity
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.model.Like
import com.example.c001apk.logic.model.TotalReplyResponse
import com.example.c001apk.logic.repository.BlackListRepo
import com.example.c001apk.logic.repository.HistoryFavoriteRepo
import com.example.c001apk.logic.repository.NetworkRepo
import com.example.c001apk.ui.base.BaseAppViewModel
import com.example.c001apk.util.Event
import com.example.c001apk.util.PrefManager
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLDecoder

@HiltViewModel(assistedFactory = FeedViewModel.Factory::class)
class FeedViewModel @AssistedInject constructor(
    @Assisted("id") val id: String,
    @Assisted("frid") var frid: String?,
    @Assisted var isViewReply: Boolean,
    blackListRepo: BlackListRepo,
    historyRepo: HistoryFavoriteRepo,
    networkRepo: NetworkRepo
) : BaseAppViewModel(blackListRepo, historyRepo, networkRepo) {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("id") id: String,
            @Assisted("frid") frid: String?,
            isViewReply: Boolean
        ): FeedViewModel
    }

    var isAInit = true
    var position: Int? = null
    var rPosition: Int? = null
    var ruid: String? = null
    var uname: String? = null
    var type: String? = null
    var isRefreshReply: Boolean? = null
    private var blockStatus = 0
    var fromFeedAuthor = 0
    private var discussMode: Int = 1
    var listType: String = "lastupdate_desc"
    var firstItem: String? = null
    var itemCount = 2
    var uid: String? = null
    var funame: String? = null
    var avatar: String? = null
    var device: String? = null
    var replyCount: String? = null
    private var dateLine: Long? = null
    var topReplyId: String? = null
    var isTop: Boolean? = null
    var feedType: String? = null

    var rid: String? = null
    var firstVisibleItemPosition = 0
    var feedTypeName: String? = null

    var feedDataList: MutableList<HomeFeedResponse.Data>? = null
    var articleList: MutableList<FeedArticleContentBean.Data>? = null
    var articleMsg: String? = null
    var articleDateLine: String? = null
    val feedTopReplyList = ArrayList<TotalReplyResponse.Data>()

    val feedReplyData = MutableLiveData<List<TotalReplyResponse.Data>>()
    var followState = MutableLiveData<Event<Int>>()

    fun onPostFollowUnFollow(url: String, uid: String, followAuthor: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postFollowUnFollow(url, uid)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.message != null) {
                            toastText.postValue(Event(response.message))
                        } else {
                            val isFollow = if (followAuthor == 1) 0
                            else 1
                            feedDataList?.getOrNull(0)?.userAction?.followAuthor = isFollow
                            followState.postValue(Event(isFollow))
                        }
                    } else {
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    fun onPostLikeReply(id: String, position: Int, likeData: Like) {
        val likeType = if (likeData.isLike.get() == 1) "unLikeReply"
        else "likeReply"
        val likeUrl = "/v6/feed/$likeType"
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postLikeReply(likeUrl, id)
                .catch { err ->
                    err.message?.let {
                        toastText.postValue(Event(it))
                    }
                }
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.data != null) {
                            val count = response.data
                            val isLike = if (likeData.isLike.get() == 1) 0 else 1
                            likeData.likeNum.set(count)
                            likeData.isLike.set(isLike)
                            val currentList = feedReplyData.value?.toMutableList() ?: ArrayList()
                            currentList[position].likenum = count
                            currentList[position].userAction?.like = isLike
                            feedReplyData.postValue(currentList)
                        } else {
                            response.message?.let {
                                toastText.postValue(Event(it))
                            }
                        }
                    } else {
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    fun fetchFeedReply() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getFeedContentReply(
                id, listType, page, firstItem, lastItem, discussMode,
                feedType.toString(), blockStatus, fromFeedAuthor
            )
                .onStart {
                    if (isLoadMore)
                        footerState.postValue(FooterState.Loading)
                }
                .collect { result ->
                    val feedReplyList = feedReplyData.value?.toMutableList() ?: ArrayList()
                    val reply = result.getOrNull()
                    if (reply != null) {
                        if (reply.message != null) {
                            footerState.postValue(FooterState.LoadingError(reply.message))
                            return@collect
                        } else if (!reply.data.isNullOrEmpty()) {
                            if (firstItem == null)
                                firstItem = reply.data.first().id
                            lastItem = reply.data.last().id
                            if (isRefreshing) {
                                feedReplyList.clear()
                                if (listType == "lastupdate_desc" && feedTopReplyList.isNotEmpty())
                                    feedReplyList.addAll(feedTopReplyList)
                            }
                            if (isRefreshing || isLoadMore) {
                                reply.data.forEach {
                                    if (it.entityType == "feed_reply") {
                                        if (listType == "lastupdate_desc" && topReplyId != null
                                            && it.id == topReplyId
                                        )
                                            return@forEach
                                        if (!blackListRepo.checkUid(it.uid))
                                            feedReplyList.add(it)
                                    }
                                }
                            }
                            page++
                            feedReplyData.postValue(feedReplyList)
                            footerState.postValue(FooterState.LoadingDone)
                        } else if (reply.data?.isEmpty() == true) {
                            isEnd = true
                            if (isRefreshing)
                                feedReplyData.postValue(emptyList())
                            footerState.postValue(FooterState.LoadingEnd(LOADING_END))
                        }
                    } else {
                        footerState.postValue(FooterState.LoadingError(LOADING_FAILED))
                        result.exceptionOrNull()?.printStackTrace()
                    }
                    isRefreshing = false
                    isLoadMore = false
                }
        }
    }

    fun fetchFeedData() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getFeedContent(id, frid)
                .collect { result ->
                    val feed = result.getOrNull()
                    if (feed != null) {
                        if (feed.message != null) {
                            activityState.postValue(LoadingState.LoadingError(feed.message))
                            return@collect
                        } else if (feed.data != null) {
                            uid = feed.data.uid
                            funame = feed.data.userInfo?.username.toString()
                            avatar = feed.data.userAvatar
                            device = feed.data.deviceTitle.toString()
                            replyCount = feed.data.replynum
                            dateLine = feed.data.dateline
                            feedTypeName = feed.data.feedTypeName
                            feedType = feed.data.feedType

                            if (feedType == "feedArticle") {
                                articleMsg =
                                    if (feed.data.message.length > 150)
                                        feed.data.message.substring(0, 150)
                                    else feed.data.message
                                articleDateLine = feed.data.dateline.toString()
                                articleList = ArrayList<FeedArticleContentBean.Data>().also {
                                    if (feed.data.messageCover?.isNotEmpty() == true) {
                                        it.add(
                                            FeedArticleContentBean.Data(
                                                "image", null, feed.data.messageCover,
                                                null, null, null, null
                                            )
                                        )
                                    }
                                    if (feed.data.messageTitle?.isNotEmpty() == true) {
                                        it.add(
                                            FeedArticleContentBean.Data(
                                                "text", feed.data.messageTitle, null,
                                                null, "true", null, null
                                            )
                                        )
                                    }
                                    val feedRaw = """{"data":${feed.data.messageRawOutput}}"""
                                    val feedJson: FeedArticleContentBean = Gson().fromJson(
                                        feedRaw, FeedArticleContentBean::class.java
                                    )
                                    feedJson.data.forEach { item ->
                                        if (item.type == "text" || item.type == "image" || item.type == "shareUrl")
                                            it.add(item)
                                    }
                                    itemCount = it.size + 1
                                }
                            } else {
                                feedDataList = ArrayList<HomeFeedResponse.Data>().also {
                                    it.add(feed.data)
                                }
                            }
                            if (!feed.data.topReplyRows.isNullOrEmpty()) {
                                isTop = true
                                topReplyId = feed.data.topReplyRows[0].id
                                feedTopReplyList.clear()
                                feedTopReplyList.addAll(feed.data.topReplyRows)
                            } else if (!feed.data.replyMeRows.isNullOrEmpty()) {
                                isTop = false
                                topReplyId = feed.data.replyMeRows[0].id
                                feedTopReplyList.clear()
                                feedTopReplyList.addAll(feed.data.replyMeRows)
                            }
                            activityState.postValue(LoadingState.LoadingDone)
                        }
                    } else {
                        activityState.postValue(LoadingState.LoadingFailed(LOADING_FAILED))
                        result.exceptionOrNull()?.printStackTrace()
                    }
                    isRefreshing = false
                    isLoadMore = false
                }
        }
    }

    val closeSheet = MutableLiveData<Event<Boolean>>()
    val scroll = MutableLiveData<Event<Boolean>>()
    val notify = MutableLiveData<Event<Boolean>>()
    var replyData = HashMap<String, String>()
    fun onPostReply() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postReply(replyData, rid.toString(), type.toString())
                .collect { result ->
                    val feedReplyList = feedReplyData.value?.toMutableList() ?: ArrayList()
                    val response = result.getOrNull()
                    response?.let {
                        if (response.data != null) {
                            if (response.data.id != null) {
                                toastText.postValue(Event("回复成功"))
                                closeSheet.postValue(Event(true))
                                if (type == "feed") {
                                    feedReplyList.add(
                                        0, TotalReplyResponse.Data(
                                            null,
                                            "feed_reply",
                                            (12345678..87654321).random()
                                                .toString(), // just random local id
                                            ruid.toString(),
                                            PrefManager.uid,
                                            id,
                                            URLDecoder.decode(PrefManager.username, "UTF-8"),
                                            uname.toString(),
                                            replyData["message"].toString(),
                                            "",
                                            null,
                                            System.currentTimeMillis() / 1000,
                                            "0",
                                            "0",
                                            PrefManager.userAvatar,
                                            ArrayList(),
                                            0,
                                            TotalReplyResponse.UserAction(0)
                                        )
                                    )
                                    scroll.postValue(Event(true))
                                } else {
                                    feedReplyList.getOrNull(position ?: 0)?.replyRows?.add(
                                        feedReplyList.getOrNull(position ?: 0)?.replyRows?.size
                                            ?: 0,
                                        TotalReplyResponse.Data(
                                            null,
                                            "feed_reply",
                                            rid.toString(),
                                            ruid.toString(),
                                            PrefManager.uid,
                                            rid.toString(),
                                            URLDecoder.decode(PrefManager.username, "UTF-8"),
                                            uname.toString(),
                                            replyData["message"].toString(),
                                            "",
                                            null,
                                            System.currentTimeMillis() / 1000,
                                            "0",
                                            "0",
                                            PrefManager.userAvatar,
                                            null,
                                            0,
                                            null
                                        )
                                    )
                                    notify.postValue(Event(true))
                                }
                                feedReplyData.postValue(feedReplyList)
                            }
                        } else {
                            response.message?.let {
                                toastText.postValue(Event(it))
                            }
                            if (response.messageStatus == "err_request_captcha") {
                                onGetValidateCaptcha()
                            }
                        }
                    }
                }
        }
    }

    val createDialog = MutableLiveData<Event<Bitmap>>()
    private fun onGetValidateCaptcha() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getValidateCaptcha("/v6/account/captchaImage?${System.currentTimeMillis() / 1000}&w=270=&h=113")
                .collect { result ->
                    val response = result.getOrNull()
                    response?.let {
                        val responseBody = response.body()
                        val bitmap = BitmapFactory.decodeStream(responseBody?.byteStream())
                        createDialog.postValue(Event(bitmap))
                    }
                }
        }
    }

    lateinit var requestValidateData: HashMap<String, String?>
    fun onPostRequestValidate() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postRequestValidate(requestValidateData)
                .collect { result ->
                    val response = result.getOrNull()
                    response?.let {
                        if (response.data != null) {
                            toastText.postValue(Event(response.data))
                            if (response.data == "验证通过") {
                                onPostReply()
                            }
                        } else if (response.message != null) {
                            toastText.postValue(Event(response.message))
                            if (response.message == "请输入正确的图形验证码") {
                                onGetValidateCaptcha()
                            }
                        }
                    }
                }
        }
    }

    fun onPostLikeFeed(id: String, likeData: Like) {
        val likeType = if (likeData.isLike.get() == 1) "unlike"
        else "like"
        val likeUrl = "/v6/feed/$likeType"
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postLikeFeed(likeUrl, id)
                .catch { err ->
                    err.message?.let {
                        toastText.postValue(Event(it))
                    }
                }
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.data != null) {
                            val count = response.data.count
                            val isLike = if (likeData.isLike.get() == 1) 0 else 1
                            likeData.likeNum.set(count)
                            likeData.isLike.set(isLike)
                            feedDataList?.getOrNull(0)?.likenum = count
                            feedDataList?.getOrNull(0)?.userAction?.like = isLike
                        } else {
                            response.message?.let {
                                toastText.postValue(Event(it))
                            }
                        }
                    } else {
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    fun postDeleteFeedReply(url: String, id: String, position: Int, rPosition: Int?) {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postDelete(url, id)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.data == "删除成功") {
                            toastText.postValue(Event("删除成功"))
                            val replyList =
                                feedReplyData.value?.toMutableList() ?: ArrayList()
                            if (rPosition == null || rPosition == -1) {
                                replyList.removeAt(position)
                            } else {
                                replyList[position].replyRows?.removeAt(rPosition)
                            }
                            feedReplyData.postValue(replyList)
                            notify.postValue(Event(true))
                        } else if (!response.message.isNullOrEmpty()) {
                            response.message.let {
                                toastText.postValue(Event(it))
                            }
                        }
                    } else {
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    fun saveUid(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            blackListRepo.saveUid(uid)
        }
    }

    suspend fun isFavorite(fid: String): Boolean {
        return withContext(Dispatchers.IO) {
            historyRepo.checkFavorite(fid)
        }
    }

    suspend fun delete(fid: String) {
        withContext(Dispatchers.IO) {
            historyRepo.deleteFavorite(fid)
        }
    }

    suspend fun insert(fav: FeedEntity) {
        withContext(Dispatchers.IO) {
            historyRepo.insertFavorite(fav)
        }
    }

    fun saveHistory(
        id: String,
        uid: String,
        username: String,
        userAvatar: String,
        deviceTitle: String,
        message: String,
        dateline: String,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            historyRepo.saveHistory(
                id,
                uid,
                username,
                userAvatar,
                deviceTitle,
                message,
                dateline,
            )
        }
    }

    override fun fetchData() {}


}