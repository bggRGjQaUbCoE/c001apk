package com.example.c001apk.ui.feed.reply

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.logic.model.StringEntity
import com.example.c001apk.logic.model.TotalReplyResponse
import com.example.c001apk.logic.repository.NetworkRepo
import com.example.c001apk.logic.repository.RecentEmojiRepo
import com.example.c001apk.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReplyViewModel @Inject constructor(
    private val recentEmojiRepo: RecentEmojiRepo,
    private val networkRepo: NetworkRepo
) : ViewModel() {

    val recentEmojiLiveData: LiveData<List<StringEntity>> = recentEmojiRepo.loadAllListLive()

    var isInit = true
    var type: String? = null
    var rid: String? = null

    val toastText = MutableLiveData<Event<String?>>()
    var responseData: TotalReplyResponse.Data? = null
    val over = MutableLiveData<Event<Boolean>>()

    var replyData = HashMap<String, String>()
    fun onPostReply() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postReply(replyData, rid.toString(), type.toString())
                .collect { result ->
                    val response = result.getOrNull()
                    response?.let {
                        if (response.data != null) {
                            responseData = response.data
                            over.postValue(Event(true))
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
                                if (type == "createFeed")
                                    onPostCreateFeed()
                                else
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

    lateinit var createFeedData: HashMap<String, String?>
    fun onPostCreateFeed() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postCreateFeed(createFeedData)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.data?.id != null) {
                            over.postValue(Event(true))
                        } else {
                            response.message?.let {
                                toastText.postValue(Event(it))
                            }
                            if (response.messageStatus == "err_request_captcha") {
                                onGetValidateCaptcha()
                            }
                        }
                    } else {
                        toastText.postValue(Event("response is null"))
                    }
                }
        }
    }

    fun updateRecentEmoji(it: String) {
        viewModelScope.launch(Dispatchers.IO) {
            with(StringEntity(it)) {
                if (recentEmojiRepo.checkEmoji(data)) {
                    recentEmojiRepo.updateEmoji(data, System.currentTimeMillis())
                } else {
                    if (recentEmojiLiveData.value?.size == 27)
                        recentEmojiLiveData.value?.last()?.data?.let {
                            recentEmojiRepo.updateEmoji(
                                it,
                                data,
                                System.currentTimeMillis()
                            )
                        }
                    else
                        recentEmojiRepo.insertEmoji(this)
                }
            }

        }
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            recentEmojiRepo.deleteAll()
        }
    }

}