package com.example.c001apk.ui.fragment.search.result

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.model.SearchTopicResponse
import com.example.c001apk.logic.model.SearchUserResponse
import com.example.c001apk.logic.network.Repository

class SearchContentViewModel : ViewModel() {

    val searchFeedList = ArrayList<HomeFeedResponse.Data>()
    val searchUserList = ArrayList<SearchUserResponse.Data>()
    val searchTopicList = ArrayList<SearchTopicResponse.Data>()

    var isRefreshing = true
    var isLoadMore = false
    var isEnd = false

    var type: String = ""
    var feedType: String = "all"
    var sort: String = "default"
    var keyWord: String = ""
    var page = 1

    private val getSearchData = MutableLiveData<String>()

    val searchData = getSearchData.switchMap {
        Repository.getSearchFeed(type, feedType, sort, keyWord, page)
    }

    fun getSearchFeed() {
        getSearchData.value = getSearchData.value
    }

    private val getSearchUserData = MutableLiveData<String>()

    val searchUserData = getSearchUserData.switchMap {
        Repository.getSearchUser(keyWord, page)
    }

    fun getSearchUser() {
        getSearchUserData.value = getSearchUserData.value
    }

    private val getSearchTopicData = MutableLiveData<String>()

    val searchTopicData = getSearchTopicData.switchMap {
        Repository.getSearchTopic(type, keyWord, page)
    }

    fun getSearchTopic() {
        getSearchTopicData.value = getSearchTopicData.value
    }

}