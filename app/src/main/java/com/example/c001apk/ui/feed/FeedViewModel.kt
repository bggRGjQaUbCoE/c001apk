package com.example.c001apk.ui.feed

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.adapter.Event
import com.example.c001apk.adapter.FooterAdapter
import com.example.c001apk.constant.Constants.LOADING_FAILED
import com.example.c001apk.logic.model.FeedArticleContentBean
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.model.Like
import com.example.c001apk.logic.model.TotalReplyResponse
import com.example.c001apk.logic.network.Repository
import com.example.c001apk.logic.network.Repository.postLikeFeed
import com.example.c001apk.logic.network.Repository.postLikeReply
import com.example.c001apk.logic.network.Repository.postReply
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.PrefManager
import com.google.gson.Gson
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.net.URLDecoder

class FeedViewModel : ViewModel() {

    var position: Int? = null
    var rPosition: Int? = null
    var rid: String? = null
    var ruid: String? = null
    var uname: String? = null
    var type: String? = null
    var listSize: Int = -1
    var isRefreshReply: Boolean? = null
    private var blockStatus = 0
    var fromFeedAuthor = 0
    private var discussMode: Int = 1
    var listType: String = "lastupdate_desc"
    var page = 1
    var firstItem: String? = null
    var lastItem: String? = null
    var isInit: Boolean = true
    var isRefreshing: Boolean = true
    var isLoadMore: Boolean = false
    var isEnd: Boolean = false
    var lastVisibleItemPosition: Int = 0
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
    var errorMessage: String? = null
    var isViewReply: Boolean? = null
    var frid: String? = null
    var firstVisibleItemPosition = 0
    var id: String? = null
    var feedTypeName: String? = null

    var feedDataList: MutableList<HomeFeedResponse.Data>? = null
    var articleList: MutableList<FeedArticleContentBean.Data>? = null
    var articleMsg: String? = null
    var articleDateLine: String? = null
    val feedTopReplyList = ArrayList<TotalReplyResponse.Data>()

    val doNext = MutableLiveData<Event<Pair<Int, String?>>>()
    val changeState = MutableLiveData<Pair<FooterAdapter.LoadState, String?>>()
    val feedReplyData = MutableLiveData<List<TotalReplyResponse.Data>>()
    var afterFollow = MutableLiveData<Event<Int>>()
    fun onPostFollowUnFollow(url: String, uid: String, followAuthor: Int) {
        viewModelScope.launch {
            Repository.postFollowUnFollow(url, uid)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        val isFollow = if (followAuthor == 1) 0
                        else 1
                        feedDataList!![0].userAction?.followAuthor = isFollow
                        afterFollow.postValue(Event(isFollow))
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
        viewModelScope.launch {
            postLikeReply(likeUrl, id)
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
                            val currentList = feedReplyData.value!!.toMutableList()
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
        viewModelScope.launch {
            Repository.getFeedContentReply(
                id.toString(), listType, page, firstItem, lastItem, discussMode,
                feedType.toString(), blockStatus, fromFeedAuthor
            )
                .onStart {
                    if (isLoadMore)
                        changeState.postValue(Pair(FooterAdapter.LoadState.LOADING, null))
                }
                .collect { result ->
                    val feedReplyList = feedReplyData.value?.toMutableList() ?: ArrayList()
                    val reply = result.getOrNull()
                    if (reply?.message != null) {
                        changeState.postValue(
                            Pair(
                                FooterAdapter.LoadState.LOADING_ERROR,
                                reply.message
                            )
                        )
                        return@collect
                    } else if (!reply?.data.isNullOrEmpty()) {
                        if (firstItem == null)
                            firstItem = reply?.data?.first()?.id
                        lastItem = reply?.data?.last()?.id
                        if (isRefreshing) {
                            feedReplyList.clear()
                            if (listType == "lastupdate_desc" && feedTopReplyList.isNotEmpty())
                                feedReplyList.addAll(feedTopReplyList)
                        }
                        if (isRefreshing || isLoadMore) {
                            for (element in reply?.data!!) {
                                if (element.entityType == "feed_reply") {
                                    if (listType == "lastupdate_desc" && topReplyId != null
                                        && element.id == topReplyId
                                    )
                                        continue
                                    if (!BlackListUtil.checkUid(element.uid))
                                        feedReplyList.add(element)
                                }
                            }
                        }
                        changeState.postValue(Pair(FooterAdapter.LoadState.LOADING_COMPLETE, null))
                    } else if (reply?.data?.isEmpty() == true) {
                        if (isRefreshing)
                            feedReplyList.clear()
                        isEnd = true
                        changeState.postValue(Pair(FooterAdapter.LoadState.LOADING_END, null))
                    } else {
                        changeState.postValue(
                            Pair(
                                FooterAdapter.LoadState.LOADING_ERROR,
                                LOADING_FAILED
                            )
                        )
                        result.exceptionOrNull()?.printStackTrace()
                    }
                    feedReplyData.postValue(feedReplyList)
                }
        }
    }

    fun fetchFeedData() {
        viewModelScope.launch {
            Repository.getFeedContent(id.toString(), frid)
                .collect { result ->
                    val feed = result.getOrNull()
                    if (feed?.message != null) {
                        errorMessage = feed.message
                        doNext.postValue(Event(Pair(1, feed.message)))
                        return@collect
                    } else if (feed?.data != null) {
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
                        doNext.postValue(Event(Pair(2, null)))
                    } else {
                        doNext.postValue(Event(Pair(3, null)))
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    val toastText = MutableLiveData<Event<String>>()
    val closeSheet = MutableLiveData<Event<Boolean>>()
    val scroll = MutableLiveData<Event<Boolean>>()
    val notify = MutableLiveData<Event<Boolean>>()
    var replyData = HashMap<String, String>()
    fun onPostReply() {
        viewModelScope.launch {
            postReply(replyData, rid.toString(), type.toString())
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
                                            id.toString(),
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
                                    feedReplyList[position!!].replyRows?.add(
                                        feedReplyList[position!!].replyRows?.size!!,
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
        viewModelScope.launch {
            Repository.getValidateCaptcha("/v6/account/captchaImage?${System.currentTimeMillis() / 1000}&w=270=&h=113")
                .collect { result ->
                    val response = result.getOrNull()
                    response?.let {
                        val responseBody = response.body()
                        val bitmap = BitmapFactory.decodeStream(responseBody!!.byteStream())
                        createDialog.postValue(Event(bitmap))
                    }
                }
        }
    }

    lateinit var requestValidateData: HashMap<String, String?>
    fun onPostRequestValidate() {
        viewModelScope.launch {
            Repository.postRequestValidate(requestValidateData)
                .collect { result ->
                    val response = result.getOrNull()
                    response?.let {
                        if (response.data != null) {
                            response.data.let {
                                toastText.postValue(Event(it))
                            }
                            if (response.data == "验证通过") {
                                onPostReply()
                            }
                        } else if (response.message != null) {
                            response.message.let {
                                toastText.postValue(Event(it))
                            }
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
        viewModelScope.launch {
            postLikeFeed(likeUrl, id)
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
                            feedDataList!![0].likenum = count
                            feedDataList!![0].userAction?.like = isLike

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
        viewModelScope.launch {
            Repository.postDelete(url, id)
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


}