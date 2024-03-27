package com.example.c001apk.ui.app

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
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class AppContentViewModel @AssistedInject constructor(
    @Assisted("id") val id: String,
    @Assisted("appCommentSort") val appCommentSort: String,
    @Assisted("appCommentTitle") val appCommentTitle: String,
    blackListRepo: BlackListRepo,
    historyRepo: HistoryFavoriteRepo,
    networkRepo: NetworkRepo
) : BaseAppViewModel(blackListRepo, historyRepo, networkRepo) {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("id") id: String,
            @Assisted("appCommentSort") appCommentSort: String,
            @Assisted("appCommentTitle") appCommentTitle: String,
        ): AppContentViewModel
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun provideFactory(
            assistedFactory: Factory, id: String, appCommentSort: String, appCommentTitle: String,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(id, appCommentSort, appCommentTitle) as T
            }
        }
    }

    private val commentBaseUrl: String = "/page?url=/feed/apkCommentList?id="
    override fun fetchData() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getDataList(
                commentBaseUrl + id + appCommentSort, appCommentTitle, null, lastItem, page
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
                    val appCommentList = dataList.value?.toMutableList() ?: ArrayList()
                    val comment = result.getOrNull()
                    if (!comment?.message.isNullOrEmpty()) {
                        comment?.message?.let {
                            if (listSize <= 0)
                                loadingState.postValue(LoadingState.LoadingError(it))
                            else
                                footerState.postValue(FooterState.LoadingError(it))
                        }
                        return@collect
                    } else if (!comment?.data.isNullOrEmpty()) {
                        lastItem = comment?.data?.last()?.id
                        if (isRefreshing)
                            appCommentList.clear()
                        if (isRefreshing || isLoadMore) {
                            comment?.data?.let { data ->
                                data.forEach {
                                    if (it.entityType == "feed")
                                        if (!blackListRepo.checkUid(
                                                it.userInfo?.uid.toString()
                                            ) && !blackListRepo.checkTopic(
                                                it.tags + it.ttitle + it.relationRows?.getOrNull(0)?.title
                                            )
                                        )
                                            appCommentList.add(it)
                                }
                            }
                        }
                        page++
                        if (listSize <= 0)
                            loadingState.postValue(LoadingState.LoadingDone)
                        else
                            footerState.postValue(FooterState.LoadingDone)
                        dataList.postValue(appCommentList)
                    } else if (comment?.data?.isEmpty() == true) {
                        isEnd = true
                        if (listSize <= 0)
                            loadingState.postValue(LoadingState.LoadingFailed(LOADING_EMPTY))
                        else {
                            if (isRefreshing)
                                dataList.postValue(emptyList())
                            footerState.postValue(FooterState.LoadingEnd(LOADING_END))
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

}