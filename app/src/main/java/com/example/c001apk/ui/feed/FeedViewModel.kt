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
import com.example.c001apk.logic.model.TotalReplyResponse
import com.example.c001apk.logic.repository.BlackListRepo
import com.example.c001apk.logic.repository.HistoryFavoriteRepo
import com.example.c001apk.logic.repository.NetworkRepo
import com.example.c001apk.ui.base.BaseAppViewModel
import com.example.c001apk.util.Event
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    var dateLine: Long? = null
    var topReplyId: String? = null
    var isTop: Boolean? = null
    var feedType: String? = null

    var rid: String? = null
    var feedTypeName: String? = null

    var feedDataList: MutableList<HomeFeedResponse.Data>? = null
    var articleList: MutableList<FeedArticleContentBean.Data>? = null
    var articleMsg: String? = null
    var articleDateLine: Long? = null
    val feedTopReplyList = ArrayList<TotalReplyResponse.Data>()

    val feedReplyData = MutableLiveData<List<TotalReplyResponse.Data>>()
    val feedUserState = MutableLiveData<Event<Boolean>>()

    fun onFollowUnFollow(url: String, uid: String, followAuthor: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postFollowUnFollow(url, uid)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.message != null) {
                            toastText.postValue(Event(response.message))
                        } else {
                            feedDataList?.getOrNull(0)?.userAction?.followAuthor =
                                if (followAuthor == 1) 0 else 1
                            feedUserState.postValue(Event(true))
                        }
                    } else {
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    fun onLikeReply(id: String, isLike: Int) {
        val likeType = if (isLike == 1) "unLikeReply" else "likeReply"
        val likeUrl = "/v6/feed/$likeType"
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postLikeReply(likeUrl, id)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.data != null) {
                            val currentList = feedReplyData.value?.map {
                                if (it.id == id) {
                                    it.copy(
                                        likenum = response.data,
                                        userAction = it.userAction?.copy(like = if (isLike == 1) 0 else 1)
                                    )
                                } else it
                            } ?: emptyList()
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
                            funame = feed.data.userInfo?.username
                            avatar = feed.data.userAvatar
                            device = feed.data.deviceTitle
                            replyCount = feed.data.replynum
                            dateLine = feed.data.dateline
                            feedTypeName = feed.data.feedTypeName
                            feedType = feed.data.feedType

                            if (feedType in listOf("feedArticle", "trade")
                                && feed.data.messageRawOutput != "null"
                            ) {
                                articleMsg =
                                    if ((feed.data.message?.length ?: 0) > 150)
                                        feed.data.message?.substring(0, 150)
                                    else feed.data.message
                                articleDateLine = feed.data.dateline
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
                                    feedJson.data?.forEach { item ->
                                        if (item.type in listOf("text", "image", "shareUrl"))
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
                            toastText.postValue(Event("回复成功"))
                            closeSheet.postValue(Event(true))
                            if (type == "feed") {
                                feedReplyList.add(0, response.data)
                                scroll.postValue(Event(true))
                            } else {
                                feedReplyList.getOrNull(position ?: 0)?.replyRows?.add(
                                    feedReplyList.getOrNull(position ?: 0)?.replyRows?.size
                                        ?: 0, response.data
                                )
                                notify.postValue(Event(true))
                            }
                            feedReplyData.postValue(feedReplyList)
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

    fun onLikeFeed(id: String, isLike: Int) {
        val likeType = if (isLike == 1) "unlike" else "like"
        val likeUrl = "/v6/feed/$likeType"
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postLikeFeed(likeUrl, id)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.data != null) {
                            feedDataList?.getOrNull(0)?.likenum = response.data.count
                            feedDataList?.getOrNull(0)?.userAction?.like = if (isLike == 1) 0 else 1
                            feedUserState.postValue(Event(true))
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

    fun preFetchVoteComment() {
        val fid = feedDataList?.getOrNull(0)?.vote?.id ?: ""
        when (val type = feedDataList?.getOrNull(0)?.vote?.type) {
            0 -> { // 2 options
                fetchVoteCommentType0(
                    fid,
                    feedDataList?.getOrNull(0)?.vote?.options?.getOrNull(0)?.id ?: ""
                )
            }

            1 -> {
                fetchVoteCommentType1(fid)
            }

            else -> {
                toastText.postValue(Event("unsupported vote type: $type"))
            }
        }
    }

    private fun fetchVoteCommentType0(fid: String, extraKey: String) {
        val isLeft = extraKey == feedDataList?.getOrNull(0)?.vote?.options?.getOrNull(0)?.id
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getVoteComment(fid, extraKey, page, null, null)
                .onStart {
                    if (isLeft)
                        footerState.postValue(FooterState.Loading)
                }
                .collect { result ->
                    val feedReplyList = feedReplyData.value?.toMutableList() ?: ArrayList()
                    val data = result.getOrNull()
                    if (data != null) {
                        if (data.message != null) {
                            footerState.postValue(FooterState.LoadingError(data.message))
                            return@collect
                        } else if (!data.data.isNullOrEmpty()) {
                            if (isRefreshing) {
                                if (isLeft)
                                    feedReplyList.clear()
                            }
                            if (isRefreshing || isLoadMore) {
                                data.data.forEach {
                                    if (it.entityType == "feed" && !blackListRepo.checkUid(it.uid)) {
                                        feedReplyList.add(it)
                                    }
                                }
                            }
                            feedReplyData.postValue(feedReplyList)
                            if (!isLeft)
                                footerState.postValue(FooterState.LoadingDone)
                        } else if (data.data?.isEmpty() == true) {
                            if (!isLeft)
                                footerState.postValue(FooterState.LoadingEnd(LOADING_END))
                        }
                        if (!isLeft)
                            page++
                    } else {
                        if (!isLeft)
                            footerState.postValue(FooterState.LoadingError(LOADING_FAILED))
                        result.exceptionOrNull()?.printStackTrace()
                    }
                    if (isLeft) {
                        fetchVoteCommentType0(
                            fid,
                            feedDataList?.getOrNull(0)?.vote?.options?.getOrNull(1)?.id ?: ""
                        )
                    }
                    if (!isLeft) {
                        isRefreshing = false
                        isLoadMore = false
                    }
                }
        }
    }

    private fun fetchVoteCommentType1(fid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getVoteComment(fid, "", page, null, lastItem)
                .onStart {
                    footerState.postValue(FooterState.Loading)
                }
                .collect { result ->
                    val feedReplyList = feedReplyData.value?.toMutableList() ?: ArrayList()
                    val data = result.getOrNull()
                    if (data != null) {
                        if (data.message != null) {
                            footerState.postValue(FooterState.LoadingError(data.message))
                            return@collect
                        } else if (!data.data.isNullOrEmpty()) {
                            lastItem = data.data.last().id
                            if (isRefreshing)
                                feedReplyList.clear()
                            if (isRefreshing || isLoadMore) {
                                data.data.forEach {
                                    if (it.entityType == "feed" && !blackListRepo.checkUid(it.uid)) {
                                        feedReplyList.add(it)
                                    }
                                }
                            }
                            page++
                            feedReplyData.postValue(feedReplyList)
                            footerState.postValue(FooterState.LoadingDone)
                        } else if (data.data?.isEmpty() == true) {
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


    fun fetchAnswerList(sort: String = "reply") {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getAnswerList(id, sort, page, null, lastItem)
                .onStart {
                    footerState.postValue(FooterState.Loading)
                }
                .collect { result ->
                    val feedReplyList = feedReplyData.value?.toMutableList() ?: ArrayList()
                    val data = result.getOrNull()
                    if (data != null) {
                        if (data.message != null) {
                            footerState.postValue(FooterState.LoadingError(data.message))
                            return@collect
                        } else if (!data.data.isNullOrEmpty()) {
                            lastItem = data.data.last().id
                            if (isRefreshing)
                                feedReplyList.clear()
                            if (isRefreshing || isLoadMore) {
                                data.data.forEach {
                                    if (it.entityType == "feed" && !blackListRepo.checkUid(it.uid)) {
                                        feedReplyList.add(it)
                                    }
                                }
                            }
                            page++
                            feedReplyData.postValue(feedReplyList)
                            footerState.postValue(FooterState.LoadingDone)
                        } else if (data.data.isNullOrEmpty()) {
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


}