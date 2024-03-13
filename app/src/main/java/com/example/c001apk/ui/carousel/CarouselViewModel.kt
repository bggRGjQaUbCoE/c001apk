package com.example.c001apk.ui.carousel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.adapter.Event
import com.example.c001apk.adapter.FooterAdapter
import com.example.c001apk.adapter.ItemListener
import com.example.c001apk.constant.Constants
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.model.Like
import com.example.c001apk.logic.model.TopicBean
import com.example.c001apk.logic.network.Repository
import com.example.c001apk.logic.network.Repository.getDataList
import com.example.c001apk.util.BlackListUtil
import com.example.c001apk.util.PrefManager
import com.example.c001apk.util.TopicBlackListUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class CarouselViewModel : ViewModel() {

    var barTitle: String? = null
    var isResume: Boolean = true
    val tabList = ArrayList<String>()
    var url: String? = null
    var type: String? = null
    var isFollow: Boolean? = null
    var fid: String? = null
    var title: String? = null
    var isInit: Boolean = true
    var uid: String? = null
    var errorMessage: String? = null
    var uname: String? = null
    var lastVisibleItemPosition: Int = 0
    var isRefreshing: Boolean = true
    var isLoadMore: Boolean = false
    var isEnd: Boolean = false
    var page = 1
    var listSize: Int = -1
    var avatar: String? = null
    var cover: String? = null
    var level: String? = null
    var like: String? = null
    var follow: String? = null
    var fans: String? = null
    var packageName: String? = null

    val changeState = MutableLiveData<Pair<FooterAdapter.LoadState, String?>>()
    val carouselData = MutableLiveData<List<HomeFeedResponse.Data>>()
    val doNext = MutableLiveData<Event<Boolean>>()
    val topicList: MutableList<TopicBean> = ArrayList()
    val initBar = MutableLiveData<Event<Boolean>>()
    val showView = MutableLiveData<Event<Boolean>>()
    val initView = MutableLiveData<Event<Boolean>>()
    val initRvView = MutableLiveData<Event<Boolean>>()
    val error = MutableLiveData<Event<Boolean>>()
    val finish = MutableLiveData<Event<Boolean>>()


    fun fetchCarouselList() {
        viewModelScope.launch(Dispatchers.IO) {
            getDataList(url.toString(), title.toString(), null, null, page)
                .onStart {
                    if (isLoadMore)
                        changeState.postValue(Pair(FooterAdapter.LoadState.LOADING, null))
                }
                .collect { result ->
                    val carouselList = carouselData.value?.toMutableList() ?: ArrayList()
                    val response = result.getOrNull()
                    if (!response?.data.isNullOrEmpty()) {
                        if (isInit) {
                            isInit = false

                            barTitle =
                                if (response?.data!![response.data.size - 1].extraDataArr == null)
                                    title
                                else
                                    response.data[response.data.size - 1].extraDataArr?.pageTitle.toString()
                            initBar.postValue(Event(true))

                            var index = 0
                            var isIconTabLinkGridCard = false
                            for (element in response.data) {
                                if (element.entityTemplate == "iconTabLinkGridCard") {
                                    showView.postValue(Event(true))
                                    isIconTabLinkGridCard = true
                                    break
                                } else index++
                            }

                            if (isIconTabLinkGridCard) {
                                if (!response.data[index].entities.isNullOrEmpty()) {
                                    response.data[index].entities?.forEach {
                                        tabList.add(it.title)
                                        topicList.add(TopicBean(it.url, it.title))
                                        initView.postValue(Event(true))
                                    }
                                }

                            } else {
                                initRvView.postValue(Event(true))
                                for (element in response.data)
                                    if (element.entityType == "feed" && element.feedType != "vote")
                                        if (!BlackListUtil.checkUid(element.userInfo?.uid.toString())
                                            && !TopicBlackListUtil.checkTopic(
                                                element.tags + element.ttitle
                                            )
                                        )
                                            carouselList.add(element)
                                carouselData.postValue(carouselList)
                            }
                        } else {
                            if (isRefreshing)
                                carouselList.clear()
                            if (isRefreshing || isLoadMore) {
                                for (element in response?.data!!)
                                    if (element.entityType == "feed" && element.feedType != "vote")
                                        if (!BlackListUtil.checkUid(element.userInfo?.uid.toString())
                                            && !TopicBlackListUtil.checkTopic(
                                                element.tags + element.ttitle
                                            )
                                        )
                                            carouselList.add(element)
                            }
                            carouselData.postValue(carouselList)
                        }
                    } else if (response?.data?.isEmpty() == true) {
                        if (isRefreshing)
                            carouselList.clear()
                        changeState.postValue(Pair(FooterAdapter.LoadState.LOADING_END, null))
                        isEnd = true
                    } else {
                        error.postValue(Event(true))
                        isEnd = true
                        result.exceptionOrNull()?.printStackTrace()
                    }
                    finish.postValue(Event(true))
                    isLoadMore = false
                    isRefreshing = false
                }
        }
    }

    val toastText = MutableLiveData<Event<String>>()

    inner class ItemClickListener : ItemListener {
        override fun onLikeClick(type: String, id: String, position: Int, likeData: Like) {
            if (PrefManager.isLogin) {
                if (PrefManager.SZLMID.isEmpty())
                    toastText.postValue(Event(Constants.SZLM_ID))
                else onPostLikeFeed(id, position, likeData)
            }
        }

        override fun onBlockUser(id: String, uid: String, position: Int) {
            super.onBlockUser(id, uid, position)
            val currentList = carouselData.value!!.toMutableList()
            currentList.removeAt(position)
            carouselData.postValue(currentList)
        }

        override fun onDeleteClicked(entityType: String, id: String, position: Int) {
            onDeleteFeed("/v6/feed/deleteFeed", id, position)
        }
    }

    fun onDeleteFeed(url: String, id: String, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            Repository.postDelete(url, id)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.data == "删除成功") {
                            toastText.postValue(Event("删除成功"))
                            val updateList = carouselData.value!!.toMutableList()
                            updateList.removeAt(position)
                            carouselData.postValue(updateList)
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

    fun onPostLikeFeed(id: String, position: Int, likeData: Like) {
        val likeType = if (likeData.isLike.get() == 1) "unlike" else "like"
        val likeUrl = "/v6/feed/$likeType"
        viewModelScope.launch(Dispatchers.IO) {
            Repository.postLikeFeed(likeUrl, id)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.data != null) {
                            val count = response.data.count
                            val isLike = if (likeData.isLike.get() == 1) 0 else 1
                            likeData.likeNum.set(count)
                            likeData.isLike.set(isLike)
                            val currentList = carouselData.value!!.toMutableList()
                            currentList[position].likenum = count
                            currentList[position].userAction?.like = isLike
                            carouselData.postValue(currentList)
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

}