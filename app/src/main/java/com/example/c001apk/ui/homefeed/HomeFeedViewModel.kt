package com.example.c001apk.ui.homefeed

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.constant.Constants.LOADING_FAILED
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.model.Like
import com.example.c001apk.logic.network.Repository.getDataList
import com.example.c001apk.logic.network.Repository.getHomeFeed
import com.example.c001apk.logic.network.Repository.getValidateCaptcha
import com.example.c001apk.logic.network.Repository.postCreateFeed
import com.example.c001apk.logic.network.Repository.postDelete
import com.example.c001apk.logic.network.Repository.postLikeFeed
import com.example.c001apk.logic.network.Repository.postRequestValidate
import com.example.c001apk.logic.repository.BlackListRepository
import com.example.c001apk.logic.repository.HistoryFavoriteRepository
import com.example.c001apk.util.Event
import com.example.c001apk.util.PrefManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeFeedViewModel @Inject constructor(
    val repository: BlackListRepository,
    private val historyFavoriteRepository: HistoryFavoriteRepository
) : ViewModel() {

    var installTime: String = System.currentTimeMillis().toString()
    val dataList = ArrayList<HomeFeedResponse.Data>()
    var position: Int? = null
    var lastVisibleItemPosition: Int = 0
    var changeFirstItem: Boolean = false
    var listSize: Int = -1
    var type: String? = null
    var isInit: Boolean = true
    var isRefreshing: Boolean = true
    var isLoadMore: Boolean = false
    var isEnd: Boolean = false
    var page = 1
    private var firstLaunch = 1
    private var firstItem: String? = null
    private var lastItem: String? = null

    val closeSheet = MutableLiveData<Event<Boolean>>()
    val createDialog = MutableLiveData<Event<Bitmap>>()
    val toastText = MutableLiveData<Event<String>>()
    val changeState = MutableLiveData<Pair<String, String?>>()
    val homeFeedData = MutableLiveData<List<HomeFeedResponse.Data>>()

    fun fetchHomeFeed() {
        viewModelScope.launch(Dispatchers.IO) {
            getHomeFeed(page, firstLaunch, installTime, firstItem, lastItem)
                .onStart {
                    if (firstLaunch == 1) {
                        firstLaunch = 0
                    }
                }
                .catch { err ->
                    err.message?.let {
                        changeState.postValue(Pair("error", it))
                    }
                }
                .collect { result ->
                    val feed = result.getOrNull()
                    val currentList = homeFeedData.value?.toMutableList() ?: ArrayList()
                    if (!feed?.message.isNullOrEmpty()) {
                        changeState.postValue(Pair("error", feed?.message))
                        return@collect
                    } else if (!feed?.data.isNullOrEmpty()) {
                        lastItem = feed?.data?.last()?.id
                        if (isRefreshing) {
                            if (feed?.data!!.size <= 4 && feed.data.last().entityTemplate == "refreshCard") {
                                toastText.postValue(Event(feed.data.last().title))
                                firstItem = null
                                lastItem = null
                                firstLaunch = 1
                                /*val index = if (PrefManager.isIconMiniCard) 4
                                else 3
                                if (listSize >= index) {
                                    if (currentList[index - 1].entityTemplate != "refreshCard") {
                                        currentList.add(index - 1, feed.data.last())
                                        homeFeedData.postValue(currentList)
                                    }
                                }*/
                                changeState.postValue(Pair("done", null))
                                return@collect
                            } else {
                                currentList.clear()
                            }
                        }
                        if (isRefreshing || isLoadMore) {
                            for (element in feed?.data!!) {
                                if (!PrefManager.isIconMiniCard && element.entityTemplate == "iconMiniScrollCard")
                                    continue
                                else if (element.entityType == "feed" && element.feedType != "vote"
                                    || element.entityTemplate == "iconMiniScrollCard"
                                    || element.entityTemplate == "iconLinkGridCard"
                                    || element.entityTemplate == "imageCarouselCard_1"
                                    || element.entityTemplate == "imageTextScrollCard"
                                ) {
                                    if (element.entityType == "feed" && changeFirstItem) {
                                        changeFirstItem = false
                                        firstItem = element.id
                                    }
                                    if (!repository.checkUid(element.userInfo?.uid.toString())
                                        && !repository.checkTopic(
                                            element.tags + element.ttitle
                                        )
                                    )
                                        currentList.add(element)
                                }
                            }
                        }
                        homeFeedData.postValue(currentList)
                        changeState.postValue(Pair("done", null))
                    } else if (feed?.data?.isEmpty() == true) {
                        if (isRefreshing)
                            currentList.clear()
                        isEnd = true
                        changeState.postValue(Pair("end", null))
                    } else {
                        changeState.postValue(Pair("error", LOADING_FAILED))
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    fun onPostLikeFeed(id: String, position: Int, likeData: Like) {
        val likeType = if (likeData.isLike.get() == 1) "unlike" else "like"
        val likeUrl = "/v6/feed/$likeType"
        viewModelScope.launch(Dispatchers.IO) {
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
                            val currentList = homeFeedData.value!!.toMutableList()
                            currentList[position].likenum = count
                            currentList[position].userAction?.like = isLike
                            homeFeedData.postValue(currentList)
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

    var dataListUrl: String? = null
    var dataListTitle: String? = null
    fun fetchDataList() {
        viewModelScope.launch(Dispatchers.IO) {
            getDataList(dataListUrl.toString(), dataListTitle.toString(), null, lastItem, page)
                .onStart {
                    if (isInit)
                        isInit = false
                }
                .catch { err ->
                    err.message?.let {
                        changeState.postValue(Pair("error", it))
                    }
                }
                .collect { result ->
                    val feed = result.getOrNull()
                    val currentList = homeFeedData.value?.toMutableList() ?: ArrayList()
                    if (!feed?.message.isNullOrEmpty()) {
                        changeState.postValue(Pair("error", feed?.message))
                        return@collect
                    } else if (!feed?.data.isNullOrEmpty()) {
                        lastItem = feed?.data?.last()?.id
                        if (isRefreshing)
                            currentList.clear()
                        if (isRefreshing || isLoadMore) {
                            feed?.data!!.forEach {
                                if (!PrefManager.isIconMiniCard
                                    && it.entityTemplate == "iconMiniGridCard"
                                )
                                    return@forEach
                                else if (it.entityType == "feed"
                                    || it.entityTemplate == "iconMiniGridCard"
                                    || it.entityTemplate == "iconLinkGridCard"
                                    || it.entityTemplate == "imageSquareScrollCard"
                                ) {
                                    if (!repository.checkUid(it.userInfo?.uid.toString())
                                        && !repository.checkTopic(
                                            it.tags + it.ttitle
                                        )
                                    )
                                        currentList.add(it)
                                }
                            }
                        }
                        changeState.postValue(Pair("done", null))
                    } else if (feed?.data?.isEmpty() == true) {
                        if (isRefreshing)
                            currentList.clear()
                        isEnd = true
                        changeState.postValue(Pair("end", null))
                    } else {
                        isEnd = true
                        changeState.postValue(Pair("error", LOADING_FAILED))
                        result.exceptionOrNull()?.printStackTrace()
                    }
                    homeFeedData.postValue(currentList)
                }

        }
    }

    fun onDeleteFeed(url: String, id: String, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            postDelete(url, id)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.data == "删除成功") {
                            toastText.postValue(Event("删除成功"))
                            val updateList = homeFeedData.value!!.toMutableList()
                            updateList.removeAt(position)
                            homeFeedData.postValue(updateList)
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


    lateinit var createFeedData: HashMap<String, String?>
    fun onPostCreateFeed() {
        viewModelScope.launch(Dispatchers.IO) {
            postCreateFeed(createFeedData)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.data?.id != null) {
                            toastText.postValue(Event("发布成功"))
                            closeSheet.postValue(Event(true))
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

    private fun onGetValidateCaptcha() {
        viewModelScope.launch(Dispatchers.IO) {
            getValidateCaptcha("/v6/account/captchaImage?${System.currentTimeMillis() / 1000}&w=270=&h=113")
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
        viewModelScope.launch(Dispatchers.IO) {
            postRequestValidate(requestValidateData)
                .collect { result ->
                    val response = result.getOrNull()
                    response?.let {
                        if (response.data != null) {
                            response.data.let {
                                toastText.postValue(Event(it))
                            }
                            if (response.data == "验证通过") {
                                onPostCreateFeed()
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
            historyFavoriteRepository.saveHistory(
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