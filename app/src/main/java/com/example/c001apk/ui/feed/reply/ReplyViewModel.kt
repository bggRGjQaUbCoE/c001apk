package com.example.c001apk.ui.feed.reply

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.logic.model.LoadUrlResponse
import com.example.c001apk.logic.model.OSSUploadPrepareModel
import com.example.c001apk.logic.model.OSSUploadPrepareResponse
import com.example.c001apk.logic.model.StringEntity
import com.example.c001apk.logic.model.TotalReplyResponse
import com.example.c001apk.logic.repository.NetworkRepo
import com.example.c001apk.logic.repository.RecentEmojiRepo
import com.example.c001apk.util.Event
import com.google.gson.Gson
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

    var replyAndFeedData = HashMap<String, String>()
    fun onPostReply() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postReply(replyAndFeedData, rid.toString(), type.toString())
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

    fun onPostCreateFeed() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postCreateFeed(replyAndFeedData)
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

    fun updateRecentEmoji(data: String) {
        viewModelScope.launch(Dispatchers.IO) {
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
                    recentEmojiRepo.insertEmoji(StringEntity(data))
            }
        }
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            recentEmojiRepo.deleteAll()
        }
    }

    val uploadImage = MutableLiveData<Event<OSSUploadPrepareResponse.Data>>()
    private val ossUploadPrepareData: HashMap<String, String> = HashMap()
    fun onPostOSSUploadPrepare(imageList:List<OSSUploadPrepareModel>) {
        ossUploadPrepareData["uploadBucket"] = "image"
        ossUploadPrepareData["uploadDir"] = "feed"
        ossUploadPrepareData["is_anonymous"] = "0"
        ossUploadPrepareData["uploadFileList"] = Gson().toJson(imageList)
        ossUploadPrepareData["toUid"] = ""

        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postOSSUploadPrepare(ossUploadPrepareData)
                .collect { result ->
                    val data = result.getOrNull()
                    if (data != null) {
                        if (data.message != null) {
                            toastText.postValue(Event("uploadPrepare error: ${data.message}"))
                        } else if (data.data != null) {
                            uploadImage.postValue(Event(data.data))
                        }
                    } else {
                        toastText.postValue(Event("response is null"))
                    }
                }
        }
    }

    val loadShareUrl = MutableLiveData<Event<LoadUrlResponse.Data>>()
    fun loadShareUrl(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.loadShareUrl(url)
                .collect { result ->
                    val data = result.getOrNull()
                    if (data != null) {
                        if (data.message != null) {
                            toastText.postValue(Event(data.message))
                        } else if (data.data != null) {
                            loadShareUrl.postValue(Event(data.data))
                        }
                    } else {
                        toastText.postValue(Event("response is null"))
                    }
                }
        }
    }

}