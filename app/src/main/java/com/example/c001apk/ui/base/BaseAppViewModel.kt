package com.example.c001apk.ui.base

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.c001apk.adapter.FooterState
import com.example.c001apk.adapter.ItemListener
import com.example.c001apk.constant.Constants
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.model.Like
import com.example.c001apk.logic.repository.BlackListRepo
import com.example.c001apk.logic.repository.HistoryFavoriteRepo
import com.example.c001apk.logic.repository.NetworkRepo
import com.example.c001apk.util.Event
import com.example.c001apk.util.PrefManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BaseAppViewModel(
    val blackListRepo: BlackListRepo,
    val historyRepo: HistoryFavoriteRepo,
    val networkRepo: NetworkRepo
) : BaseViewModel() {

    val dataList = MutableLiveData<List<HomeFeedResponse.Data>>()

    var followUserState = MutableLiveData<Event<Int?>>() // position of user/feed detail
    val footerState = MutableLiveData<FooterState>()
    val toastText = MutableLiveData<Event<String?>>()

    open fun showCollection(id: String, title: String) {}

    inner class ItemClickListener : ItemListener {
        override fun onShowCollection(id: String, title: String) {
            showCollection(id, title)
        }

        override fun onViewFeed(
            view: View,
            id: String?,
            uid: String?,
            username: String?,
            userAvatar: String?,
            deviceTitle: String?,
            message: String?,
            dateline: String?,
            rid: Any?,
            isViewReply: Any?
        ) {
            super.onViewFeed(
                view,
                id,
                uid,
                username,
                userAvatar,
                deviceTitle,
                message,
                dateline,
                rid,
                isViewReply
            )
            viewModelScope.launch(Dispatchers.IO) {
                if (!uid.isNullOrEmpty() && PrefManager.isRecordHistory)
                    historyRepo.saveHistory(
                        id.toString(), uid.toString(), username.toString(), userAvatar.toString(),
                        deviceTitle.toString(), message.toString(), dateline.toString()
                    )
            }
        }

        override fun onFollowUser(uid: String, followAuthor: Int, position: Int) {
            if (PrefManager.isLogin)
                if (followAuthor == 1) {
                    onPostFollowUnFollow("/v6/user/unfollow", uid, followAuthor, position)
                } else {
                    onPostFollowUnFollow("/v6/user/follow", uid, followAuthor, position)
                }
        }

        override fun onLikeClick(type: String, id: String, position: Int, likeData: Like) {
            if (PrefManager.isLogin) {
                if (PrefManager.SZLMID.isEmpty())
                    toastText.postValue(Event(Constants.SZLM_ID))
                else onPostLikeFeed(id, position, likeData)
            }
        }

        override fun onBlockUser(id: String, uid: String, position: Int) {
            viewModelScope.launch(Dispatchers.IO) {
                blackListRepo.saveUid(uid)
            }
            val currentList = dataList.value?.toMutableList() ?: ArrayList()
            currentList.removeAt(position)
            dataList.postValue(currentList)
        }

        override fun onDeleteClicked(entityType: String, id: String, position: Int) {
            val url = if (entityType == "feed") "/v6/feed/deleteFeed"
            else "/v6/feed/deleteReply"
            onDeleteFeed(url, id, position)
        }
    }

    fun onDeleteFeed(url: String, id: String, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postDelete(url, id)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.data == "删除成功") {
                            toastText.postValue(Event("删除成功"))
                            val updateList = dataList.value?.toMutableList() ?: ArrayList()
                            updateList.removeAt(position)
                            dataList.postValue(updateList)
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
            networkRepo.postLikeFeed(likeUrl, id)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.data != null) {
                            val count = response.data.count
                            val isLike = if (likeData.isLike.get() == 1) 0 else 1
                            likeData.likeNum.set(count)
                            likeData.isLike.set(isLike)
                            val currentList = dataList.value?.toMutableList() ?: ArrayList()
                            currentList[position].likenum = count
                            currentList[position].userAction?.like = isLike
                            dataList.postValue(currentList)
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

    // follow user
    fun onPostFollowUnFollow(url: String, uid: String, followAuthor: Int, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.postFollowUnFollow(url, uid)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.message != null) {
                            toastText.postValue(Event(response.message))
                        } else {
                            val isFollow = if (followAuthor == 1) 0
                            else 1
                            val userList = dataList.value?.toMutableList() ?: ArrayList()
                            userList[position].isFollow = isFollow
                            dataList.postValue(userList)
                            followUserState.postValue(Event(position))
                            toastText.postValue(
                                Event(
                                    if (isFollow == 1) "关注成功"
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

    fun saveTopic(title: String) {
        viewModelScope.launch {
            blackListRepo.saveTopic(title)
        }
    }

    fun deleteTopic(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            blackListRepo.deleteTopic(title)
        }
    }

}
