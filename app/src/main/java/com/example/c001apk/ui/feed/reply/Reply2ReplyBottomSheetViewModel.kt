package com.example.c001apk.ui.feed.reply

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.adapter.FooterState
import com.example.c001apk.constant.Constants.LOADING_FAILED
import com.example.c001apk.logic.model.Like
import com.example.c001apk.logic.model.TotalReplyResponse
import com.example.c001apk.logic.repository.BlackListRepo
import com.example.c001apk.logic.repository.HistoryFavoriteRepo
import com.example.c001apk.logic.repository.NetworkRepo
import com.example.c001apk.util.Event
import com.example.c001apk.util.PrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.net.URLDecoder
import javax.inject.Inject

@HiltViewModel
class Reply2ReplyBottomSheetViewModel @Inject constructor(
    private val repository: BlackListRepo,
    private val historyFavoriteRepo: HistoryFavoriteRepo,
    private val networkRepo: NetworkRepo
) : ViewModel() {

    var uname: String? = null
    var ruid: String? = null
    var rid: String? = null
    var position: Int? = null
    var fuid: String? = null
    var listSize: Int = -1
    var page = 1
    var lastItem: String? = null
    var id: String? = null
    var uid: String? = null
    var isInit: Boolean = true
    var isRefreshing: Boolean = false
    var isLoadMore: Boolean = false
    var isEnd: Boolean = false
    var lastVisibleItemPosition: Int = 0

    val footerState = MutableLiveData<FooterState>()
    val totalReplyData = MutableLiveData<List<TotalReplyResponse.Data>>()
    var oriReply: ArrayList<TotalReplyResponse.Data> = ArrayList()

    fun fetchReplyTotal() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getReply2Reply(id.toString(), page, lastItem)
                .onStart {
                    if (isLoadMore)
                        footerState.postValue(FooterState.Loading)
                }
                .collect { result ->
                    val replyTotalList = totalReplyData.value?.toMutableList() ?: ArrayList()
                    val reply = result.getOrNull()
                    if (reply != null) {
                        if (reply.message != null) {
                            footerState.postValue(FooterState.LoadingError(reply.message))
                            return@collect
                        } else if (!reply.data.isNullOrEmpty()) {
                            lastItem = reply.data.last().id
                            if (!isLoadMore) {
                                replyTotalList.clear()
                                replyTotalList.addAll(oriReply)
                            }
                            listSize = replyTotalList.size
                            reply.data.forEach {
                                if (it.entityType == "feed_reply")
                                    if (!repository.checkUid(it.uid))
                                        replyTotalList.add(it)
                            }
                            page++
                            totalReplyData.postValue(replyTotalList)
                            footerState.postValue(FooterState.LoadingDone)
                        } else if (reply.data?.isEmpty() == true) {
                            isEnd = true
                            if (replyTotalList.isEmpty())
                                totalReplyData.postValue(oriReply)
                            footerState.postValue(FooterState.LoadingEnd)
                        }
                    } else {
                        isEnd = true
                        if (replyTotalList.isEmpty())
                            totalReplyData.postValue(oriReply)
                        footerState.postValue(FooterState.LoadingError(LOADING_FAILED))
                        result.exceptionOrNull()?.printStackTrace()
                    }
                    isRefreshing = false
                    isLoadMore = false
                }
        }

    }

    val closeSheet = MutableLiveData<Event<Boolean>>()
    var replyData = HashMap<String, String>()
    fun onPostReply() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postReply(replyData, rid.toString(), "reply")
                .collect { result ->
                    val replyTotalList = totalReplyData.value?.toMutableList() ?: ArrayList()
                    val response = result.getOrNull()
                    response?.let {
                        if (response.data != null) {
                            if (response.data.id != null) {
                                toastText.postValue(Event("回复成功"))
                                closeSheet.postValue(Event(true))
                                replyTotalList.add(
                                    (position ?: 0) + 1,
                                    TotalReplyResponse.Data(
                                        null,
                                        "feed_reply",
                                        (12345678..87654321).random().toString(),
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
                                totalReplyData.postValue(replyTotalList)
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

    val toastText = MutableLiveData<Event<String>>()
    fun postDeleteFeedReply(url: String, id: String, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postDelete(url, id)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.data == "删除成功") {
                            toastText.postValue(Event("删除成功"))
                            val replyList =
                                totalReplyData.value?.toMutableList() ?: ArrayList()
                            replyList.removeAt(position)
                            totalReplyData.postValue(replyList)
                        } else if (!response.message.isNullOrEmpty()) {
                            toastText.postValue(Event(response.message))
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
                            val currentList = totalReplyData.value?.toMutableList() ?: ArrayList()
                            currentList[position].likenum = count
                            currentList[position].userAction?.like = isLike
                            totalReplyData.postValue(currentList)
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

    fun saveUid(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveUid(uid)
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
            historyFavoriteRepo.saveHistory(
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

}