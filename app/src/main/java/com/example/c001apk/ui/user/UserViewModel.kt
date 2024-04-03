package com.example.c001apk.ui.user

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.c001apk.adapter.FooterState
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.constant.Constants.LOADING_EMPTY
import com.example.c001apk.constant.Constants.LOADING_END
import com.example.c001apk.constant.Constants.LOADING_FAILED
import com.example.c001apk.logic.model.UserProfileResponse
import com.example.c001apk.logic.repository.BlackListRepo
import com.example.c001apk.logic.repository.HistoryFavoriteRepo
import com.example.c001apk.logic.repository.NetworkRepo
import com.example.c001apk.ui.base.BaseAppViewModel
import com.example.c001apk.util.Event
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = UserViewModel.Factory::class)
class UserViewModel @AssistedInject constructor(
    @Assisted var uid: String,
    blackListRepo: BlackListRepo,
    historyRepo: HistoryFavoriteRepo,
    networkRepo: NetworkRepo
) : BaseAppViewModel(blackListRepo, historyRepo, networkRepo) {

    @AssistedFactory
    interface Factory {
        fun create(uid: String): UserViewModel
    }

    // activity
    var isAInit: Boolean = true
    val blockState = MutableLiveData<Event<Boolean>>()
    val followState = MutableLiveData<Event<Int?>>()
    var userData: UserProfileResponse.Data? = null

    fun fetchUser() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getUserSpace(uid)
                .collect { result ->
                    val user = result.getOrNull()
                    if (user?.message != null) {
                        activityState.postValue(LoadingState.LoadingError(user.message))
                        return@collect
                    } else if (user?.data != null) {
                        uid = user.data.uid
                        userData = user.data
                        activityState.postValue(LoadingState.LoadingDone)
                    } else {
                        activityState.postValue(LoadingState.LoadingFailed(LOADING_FAILED))
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    override fun fetchData() {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getUserFeed(uid, page, lastItem)
                .onStart {
                    if (isLoadMore) {
                        if (listSize <= 0)
                            loadingState.postValue(LoadingState.Loading)
                        else
                            footerState.postValue(FooterState.Loading)
                    }
                }
                .collect { result ->
                    val feedList = dataList.value?.toMutableList() ?: ArrayList()
                    val feed = result.getOrNull()
                    if (feed != null) {
                        if (!feed.message.isNullOrEmpty()) {
                            if (listSize <= 0)
                                loadingState.postValue(LoadingState.LoadingError(feed.message))
                            else
                                footerState.postValue(FooterState.LoadingError(feed.message))
                            return@collect
                        } else if (!feed.data.isNullOrEmpty()) {
                            lastItem = feed.data.last().id
                            if (isRefreshing) feedList.clear()
                            if (isRefreshing || isLoadMore) {
                                feed.data.forEach {
                                    // noMoreDataCard
                                    if (it.entityTemplate == "noMoreDataCard") {
                                        isEnd = true
                                        isRefreshing = false
                                        isLoadMore = false
                                        dataList.postValue(feedList)
                                        if (listSize <= 0 && feedList.isEmpty())
                                            loadingState.postValue(
                                                LoadingState.LoadingError(it.title ?: "")
                                            )
                                        else {
                                            footerState.postValue(
                                                FooterState.LoadingEnd(it.title ?: "")
                                            )
                                            loadingState.postValue(LoadingState.LoadingDone)
                                        }
                                        return@collect
                                    } else if (it.entityType == "feed")
                                        if (!blackListRepo.checkUid(it.userInfo?.uid.toString())
                                            && !blackListRepo.checkTopic(
                                                it.tags + it.ttitle + it.relationRows?.getOrNull(
                                                    0
                                                )?.title
                                            )
                                        )
                                            feedList.add(it)
                                }
                            }
                            page++
                            if (listSize <= 0)
                                loadingState.postValue(LoadingState.LoadingDone)
                            else
                                footerState.postValue(FooterState.LoadingDone)
                            dataList.postValue(feedList)
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

    fun onPostFollowUnFollow(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postFollowUnFollow(url, uid)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.message != null) {
                            toastText.postValue(Event(response.message))
                        } else {
                            userData?.isFollow = if (userData?.isFollow == 1) 0 else 1
                            checkFollow()
                            toastText.postValue(
                                Event(
                                    if (userData?.isFollow == 1) "关注成功"
                                    else "取消关注成功"
                                )
                            )
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

    fun deleteUid(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            blackListRepo.deleteUid(uid)
        }
    }

    fun checkMenuState() {
        checkUid(uid)
        checkFollow()
    }

    private fun checkUid(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            blockState.postValue(Event(blackListRepo.checkUid(uid)))
        }
    }

    private fun checkFollow() {
        viewModelScope.launch(Dispatchers.IO) {
            followState.postValue(Event(userData?.isFollow ?: 0))
        }
    }

}