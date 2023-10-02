package com.example.coolapk.ui.fragment.search.result

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.coolapk.logic.model.HomeFeedResponse
import com.example.coolapk.logic.model.SearchUserResponse
import com.example.coolapk.logic.network.Repository

class SearchContentViewModel : ViewModel() {

    val searchFeedList = ArrayList<HomeFeedResponse.Data>()
    val searchUserList = ArrayList<SearchUserResponse.Data>()

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

}