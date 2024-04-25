package com.example.c001apk.ui.feed

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
    var cuid: String? = null
    var uname: String? = null
    var type: String? = null
    var isRefreshReply: Boolean? = null
    private var blockStatus = 0
    var fromFeedAuthor = 0
    private var discussMode: Int = 1
    var listType: String = "lastupdate_desc"
    var firstItem: String? = null
    var itemCount = 2
    var feedUid: String? = null
    var funame: String? = null
    var avatar: String? = null
    var device: String? = null
    var replyCount: String? = null
    var dateLine: Long? = null
    private var topReplyId: String? = null
    private var replyMeId: String? = null
    private var isTop: Boolean? = null
    var feedType: String? = null

    var rid: String? = null
    var feedTypeName: String? = null

    var feedDataList: MutableList<HomeFeedResponse.Data>? = null
    var articleList: MutableList<FeedArticleContentBean.Data>? = null
    var articleMsg: String? = null
    var articleDateLine: Long? = null
    private val feedTopReplyList = ArrayList<TotalReplyResponse.Data>()

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
                    val data = result.getOrNull()
                    if (data != null) {
                        if (data.message != null) {
                            footerState.postValue(FooterState.LoadingError(data.message))
                            return@collect
                        } else if (!data.data.isNullOrEmpty()) {
                            if (firstItem == null)
                                firstItem = data.data.first().id
                            lastItem = data.data.last().id
                            if (isRefreshing) {
                                feedReplyList.clear()
                                if (listType == "lastupdate_desc" && feedTopReplyList.isNotEmpty())
                                    feedReplyList.addAll(feedTopReplyList)
                            }
                            if (isRefreshing || isLoadMore) {
                                data.data.forEach { reply ->
                                    if (reply.entityType == "feed_reply") {
                                        if (listType == "lastupdate_desc"
                                            && reply.id in listOf(topReplyId, replyMeId)
                                        )
                                            return@forEach
                                        if (!blackListRepo.checkUid(reply.uid)) {
                                            // reply tag
                                            val unameTag =
                                                when (reply.uid) {
                                                    feedUid -> " [楼主]"
                                                    else -> ""
                                                }
                                            reply.username = "${reply.username}$unameTag"

                                            if (!reply.replyRows.isNullOrEmpty()) {
                                                reply.replyRows = reply.replyRows?.filter {
                                                    !blackListRepo.checkUid(it.uid)
                                                }?.map {
                                                    it.copy(
                                                        message = generateMess(
                                                            it,
                                                            feedUid,
                                                            reply.uid
                                                        )
                                                    )
                                                }?.toMutableList()
                                            }
                                            feedReplyList.add(reply)
                                        }
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


    var feedData: HomeFeedResponse.Data? = null
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
                            feedData = feed.data
                            handleFeedData()
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


    private fun generateMess(
        reply: TotalReplyResponse.Data,
        feedUid: String?,
        uid: String?
    ): String =
        run {
            val replyTag =
                when (reply.uid) {
                    feedUid -> " [楼主] "
                    uid -> " [层主] "
                    else -> ""
                }

            val rReplyTag =
                when (reply.ruid) {
                    feedUid -> " [楼主] "
                    uid -> " [层主] "
                    else -> ""
                }

            val rReplyUser =
                when (reply.ruid) {
                    uid -> ""
                    else -> """<a class="feed-link-uname" href="/u/${reply.ruid}">${reply.rusername}${rReplyTag}</a>"""
                }

            val replyPic =
                when (reply.pic) {
                    "" -> ""
                    else -> """ <a class=\"feed-forward-pic\" href=${reply.pic}>查看图片(${reply.picArr?.size})</a>"""
                }

            """<a class="feed-link-uname" href="/u/${reply.uid}">${reply.username}${replyTag}</a>回复${rReplyUser}: ${reply.message}${replyPic}"""

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
                            val newList: List<TotalReplyResponse.Data> =
                                if (rPosition == null || rPosition == -1) {
                                    feedReplyData.value?.filterIndexed { index, _ ->
                                        index != position
                                    } ?: emptyList()
                                } else {
                                    feedReplyData.value?.mapIndexed { index, reply ->
                                        if (index == position) {
                                            reply.copy(
                                                lastupdate = System.currentTimeMillis(),
                                                replyRows = reply.replyRows.also {
                                                    it?.removeAt(rPosition)
                                                }
                                            )
                                        } else reply
                                    } ?: emptyList()
                                }
                            feedReplyData.postValue(newList)
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

    fun updateReply(data: TotalReplyResponse.Data) {
        val feedReplyList: List<TotalReplyResponse.Data> =
            if (type == "feed") { // feed
                val newList =
                    feedReplyData.value?.toMutableList() ?: ArrayList()
                newList.add(0, data)
                newList
            } else { //feed reply
                feedReplyData.value?.mapIndexed { index, reply ->
                    if (index == position) {
                        reply.copy(
                            lastupdate = System.currentTimeMillis(),
                            replyRows = (reply.replyRows ?: ArrayList()).also {
                                it.add(
                                    reply.replyRows?.size ?: 0,
                                    data.also { reply ->
                                        reply.message =
                                            generateMess(reply, feedUid, cuid)
                                    }
                                )
                            }
                        )
                    } else reply
                } ?: emptyList()
            }
        feedReplyData.postValue(feedReplyList)
    }

    fun handleFeedData() {
        feedData?.let {data->
            feedUid = data.uid
            funame = data.userInfo?.username
            avatar = data.userAvatar
            device = data.deviceTitle
            replyCount = data.replynum
            dateLine = data.dateline
            feedTypeName = data.feedTypeName
            feedType = data.feedType

            if (feedType in listOf("feedArticle", "trade")
                && data.messageRawOutput != "null"
            ) {
                articleMsg =
                    if ((data.message?.length ?: 0) > 150)
                        data.message?.substring(0, 150)
                    else data.message
                articleDateLine = data.dateline
                articleList = ArrayList<FeedArticleContentBean.Data>().also {
                    if (data.messageCover?.isNotEmpty() == true) {
                        it.add(
                            FeedArticleContentBean.Data(
                                "image", null, data.messageCover,
                                null, null, null, null
                            )
                        )
                    }
                    if (data.messageTitle?.isNotEmpty() == true) {
                        it.add(
                            FeedArticleContentBean.Data(
                                "text", data.messageTitle, null,
                                null, "true", null, null
                            )
                        )
                    }
                    val feedRaw = """{"data":${data.messageRawOutput}}"""
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
                    it.add(data)
                }
            }
            if (!data.topReplyRows.isNullOrEmpty()) {
                isTop = true
                feedTopReplyList.clear()
                data.topReplyRows.getOrNull(0)?.let {
                    topReplyId = it.id
                    val unameTag =
                        when (it.uid) {
                            feedUid -> " [楼主]"
                            else -> ""
                        }
                    val replyTag = " [置顶]"
                    it.username = "${it.username}$unameTag$replyTag"
                    if (!it.replyRows.isNullOrEmpty()) {
                        it.replyRows = it.replyRows?.map { reply ->
                            reply.copy(
                                message = generateMess(reply, feedUid, it.uid)
                            )
                        }?.toMutableList()
                    }
                }
                feedTopReplyList.addAll(data.topReplyRows)
            }
            if (!data.replyMeRows.isNullOrEmpty()) {
                run {
                    data.replyMeRows.getOrNull(0)?.let {
                        if (it.id == topReplyId)
                            return@run
                        else
                            replyMeId = it.id
                        val unameTag =
                            when (it.uid) {
                                feedUid -> " [楼主]"
                                else -> ""
                            }
                        it.username = "${it.username}$unameTag"
                        if (!it.replyRows.isNullOrEmpty()) {
                            it.replyRows = it.replyRows?.map { reply ->
                                reply.copy(
                                    message = generateMess(reply, feedUid, it.uid)
                                )
                            }?.toMutableList()
                        }
                    }
                    feedTopReplyList.addAll(data.replyMeRows)
                }
            }
        }
    }


}