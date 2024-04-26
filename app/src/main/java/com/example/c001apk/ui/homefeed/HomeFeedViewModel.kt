package com.example.c001apk.ui.homefeed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.c001apk.adapter.FooterState
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.constant.Constants.LOADING_EMPTY
import com.example.c001apk.constant.Constants.LOADING_END
import com.example.c001apk.constant.Constants.LOADING_FAILED
import com.example.c001apk.logic.repository.BlackListRepo
import com.example.c001apk.logic.repository.HistoryFavoriteRepo
import com.example.c001apk.logic.repository.NetworkRepo
import com.example.c001apk.ui.base.BaseAppViewModel
import com.example.c001apk.util.Event
import com.example.c001apk.util.PrefManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class HomeFeedViewModel @AssistedInject constructor(
    @Assisted private val installTime: String,
    blackListRepo: BlackListRepo,
    historyRepo: HistoryFavoriteRepo,
    networkRepo: NetworkRepo
) : BaseAppViewModel(blackListRepo, historyRepo, networkRepo) {

    @AssistedFactory
    interface Factory {
        fun create(installTime: String): HomeFeedViewModel
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun provideFactory(
            assistedFactory: Factory,
            installTime: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(installTime) as T
            }
        }
    }

    var position: Int? = null
    var changeFirstItem: Boolean = false
    var type: String? = null
    private var firstLaunch = 1
    private var firstItem: String? = null

    override fun fetchData() {
        when (type) {
            "feed" -> fetchHomeFeed()
            "rank", "follow", "coolPic" -> fetchDataList()
        }
    }

    private fun fetchHomeFeed() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getHomeFeed(page, firstLaunch, installTime, firstItem, lastItem)
                .onStart {
                    if (firstLaunch == 1)
                        firstLaunch = 0
                    if (isLoadMore) {
                        if (listSize <= 0)
                            loadingState.postValue(LoadingState.Loading)
                        else
                            footerState.postValue(FooterState.Loading)
                    }
                }
                .collect { result ->
                    val feed = result.getOrNull()
                    val currentList = dataList.value?.toMutableList() ?: ArrayList()
                    if (feed != null) {
                        if (!feed.message.isNullOrEmpty()) {
                            feed.message.let {
                                if (listSize <= 0)
                                    loadingState.postValue(LoadingState.LoadingError(it))
                                else
                                    footerState.postValue(FooterState.LoadingError(it))
                            }
                            return@collect
                        } else if (!feed.data.isNullOrEmpty()) {
                            lastItem = feed.data.last().id
                            if (isRefreshing) {
                                if (feed.data.size <= 4
                                    && feed.data.last().entityTemplate == "refreshCard"
                                ) {
                                    toastText.postValue(Event(feed.data.last().title))
                                    firstItem = null
                                    lastItem = null
                                    firstLaunch = 1
                                    /*val index = if (PrefManager.isIconMiniCard) 4
                                    else 3
                                    if (listSize >= index) {
                                        if (currentList[index - 1].entityTemplate != "refreshCard") {
                                            currentList.add(index - 1, feed.data.last())
                                            dataList.postValue(currentList)
                                        }
                                    }*/
                                    page++
                                    if (listSize <= 0)
                                        loadingState.postValue(LoadingState.LoadingDone)
                                    else
                                        footerState.postValue(FooterState.LoadingDone)
                                    isRefreshing = false
                                    return@collect
                                } else {
                                    currentList.clear()
                                }
                            }
                            if (isRefreshing || isLoadMore) {
                                feed.data.forEach {
                                    when (it.entityType) {
                                        "card" -> when (it.entityTemplate) {
                                            "iconLinkGridCard" -> currentList.add(it)

                                            "imageCarouselCard_1" -> {
                                                it.entities = it.entities?.filterNot { item ->
                                                    item.url.startsWith("http")
                                                }?.toMutableList()
                                                if (!it.entities.isNullOrEmpty()) {
                                                    if ((it.entities?.size ?: 0) > 1) {
                                                        it.entities?.last()?.let { item ->
                                                            it.entities?.add(0, item)
                                                        }
                                                        it.entities?.getOrNull(1)?.let { item ->
                                                            it.entities?.add(
                                                                it.entities?.size ?: 0, item
                                                            )
                                                        }
                                                    }
                                                    currentList.add(it)
                                                }
                                            }

                                            "iconMiniScrollCard" -> {
                                                if (!PrefManager.isIconMiniCard)
                                                    return@forEach
                                                else {
                                                    it.entities = it.entities?.filter { item ->
                                                        (item.entityType in listOf(
                                                            "topic", "product"
                                                        ))
                                                                && !blackListRepo.checkTopic(item.title)
                                                    }?.toMutableList()
                                                    if (!it.entities.isNullOrEmpty())
                                                        currentList.add(it)
                                                }
                                            }

                                            "imageTextScrollCard" -> {
                                                it.entities = it.entities?.filter { item ->
                                                    item.entityType == "feed"
                                                            && !blackListRepo.checkUid(item.userInfo.uid)
                                                }?.toMutableList()
                                                if (!it.entities.isNullOrEmpty())
                                                    currentList.add(it)
                                            }

                                            else -> return@forEach
                                        }

                                        "feed" -> {
                                            if (changeFirstItem) {
                                                changeFirstItem = false
                                                firstItem = it.id
                                            }

                                            if (!blackListRepo.checkUid(it.userInfo?.uid.toString())
                                                && !blackListRepo.checkTopic(
                                                    it.tags + it.ttitle +
                                                            it.relationRows?.getOrNull(0)?.title
                                                )
                                            ) {
                                                // hot reply
                                                if (!it.replyRows.isNullOrEmpty()) {
                                                    if (!blackListRepo.checkUid(
                                                            it.replyRows?.getOrNull(0)?.uid.toString()
                                                        )
                                                    ) {
                                                        it.replyRows?.getOrNull(0)?.let { reply ->
                                                            val pic =
                                                                if (reply.picArr.isNullOrEmpty()) ""
                                                                else " ${if (reply.message == "[图片]") "" else "[图片] "}<a class=\"feed-forward-pic\" href=${reply.pic}>查看图片(${reply.picArr.size})</a>"

                                                            reply.message =
                                                                "<a class=\"feed-link-uname\" href=\"/u/${reply.uid}\">${reply.userInfo?.username}</a>: ${reply.message}$pic"
                                                        }
                                                    } else it.replyRows = null
                                                }
                                                currentList.add(it)
                                            }
                                        }

                                        else -> return@forEach
                                    }
                                }
                            }
                            page++
                            if (listSize <= 0)
                                loadingState.postValue(LoadingState.LoadingDone)
                            else
                                footerState.postValue(FooterState.LoadingDone)
                            dataList.postValue(currentList)
                        } else if (feed.data?.isEmpty() == true) {
                            isEnd = true
                            if (listSize <= 0)
                                loadingState.postValue(LoadingState.LoadingFailed(LOADING_EMPTY))
                            else {
                                if (isRefreshing)
                                    dataList.postValue(emptyList())
                                footerState.postValue(FooterState.LoadingEnd(LOADING_END))
                            }
                        }
                    } else {
                        isEnd = true
                        if (listSize <= 0)
                            loadingState.postValue(LoadingState.LoadingFailed(LOADING_FAILED))
                        else
                            footerState.postValue(FooterState.LoadingError(LOADING_FAILED))
                        result.exceptionOrNull()?.printStackTrace()
                    }
                    isRefreshing = false
                    isLoadMore = false
                }
        }
    }


    var dataListUrl: String? = null
    var dataListTitle: String? = null
    private fun fetchDataList() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getDataList(
                dataListUrl.toString(),
                dataListTitle.toString(),
                null,
                lastItem,
                page
            )
                .onStart {
                    if (isLoadMore) {
                        if (listSize <= 0)
                            loadingState.postValue(LoadingState.Loading)
                        else
                            footerState.postValue(FooterState.Loading)
                    }
                }
                .collect { result ->
                    val feed = result.getOrNull()
                    val currentList = dataList.value?.toMutableList() ?: ArrayList()
                    if (feed != null) {
                        if (!feed.message.isNullOrEmpty()) {
                            feed.message.let {
                                if (listSize <= 0)
                                    loadingState.postValue(LoadingState.LoadingError(it))
                                else
                                    footerState.postValue(FooterState.LoadingError(it))
                            }
                            return@collect
                        } else if (!feed.data.isNullOrEmpty()) {
                            lastItem = feed.data.last().id
                            if (isRefreshing)
                                currentList.clear()
                            if (isRefreshing || isLoadMore) {
                                feed.data.forEach {
                                    when (it.entityType) {
                                        "card" -> when (it.entityTemplate) {
                                            "iconLinkGridCard" -> currentList.add(it)

                                            "imageSquareScrollCard" -> {
                                                it.entities = it.entities?.filter { item ->
                                                    item.entityType == "picCategory"
                                                }?.toMutableList()
                                                if (!it.entities.isNullOrEmpty())
                                                    currentList.add(it)
                                            }

                                            "iconMiniGridCard" -> {
                                                if (!PrefManager.isIconMiniCard)
                                                    return@forEach
                                                else {
                                                    it.entities = it.entities?.filter { item ->
                                                        (item.entityType in listOf(
                                                            "topic", "product"
                                                        ))
                                                                && !blackListRepo.checkTopic(item.title)
                                                    }?.toMutableList()
                                                    if (!it.entities.isNullOrEmpty())
                                                        currentList.add(it)
                                                }
                                            }

                                            else -> return@forEach
                                        }

                                        "feed" -> {
                                            if (changeFirstItem) {
                                                changeFirstItem = false
                                                firstItem = it.id
                                            }

                                            if (!blackListRepo.checkUid(it.userInfo?.uid.toString())
                                                && !blackListRepo.checkTopic(
                                                    it.tags + it.ttitle +
                                                            it.relationRows?.getOrNull(0)?.title
                                                )
                                            )
                                                currentList.add(it)
                                        }

                                        else -> return@forEach
                                    }
                                }
                            }
                            page++
                            if (listSize <= 0)
                                loadingState.postValue(LoadingState.LoadingDone)
                            else
                                footerState.postValue(FooterState.LoadingDone)
                            dataList.postValue(currentList)
                        } else if (feed.data?.isEmpty() == true) {
                            isEnd = true
                            if (listSize <= 0)
                                loadingState.postValue(LoadingState.LoadingFailed(LOADING_EMPTY))
                            else {
                                if (isRefreshing)
                                    dataList.postValue(emptyList())
                                footerState.postValue(FooterState.LoadingEnd(LOADING_END))
                            }
                        }
                    } else {
                        isEnd = true
                        if (listSize <= 0)
                            loadingState.postValue(LoadingState.LoadingFailed(LOADING_FAILED))
                        else
                            footerState.postValue(FooterState.LoadingError(LOADING_FAILED))
                        result.exceptionOrNull()?.printStackTrace()
                    }
                    isRefreshing = false
                    isLoadMore = false
                }
        }
    }

    fun saveUid(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            blackListRepo.saveUid(uid)
        }
    }

}