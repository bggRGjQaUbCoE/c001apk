package com.example.coolapk.ui.fragment.search.result

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.coolapk.logic.model.HomeFeedResponse
import com.example.coolapk.logic.network.Repository

class SearchContentViewModel : ViewModel() {

    val searchList = ArrayList<HomeFeedResponse.Data>()

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
        Repository.getSearch(type, feedType, sort, keyWord, page)
    }

    fun getSearch() {
        getSearchData.value = getSearchData.value
    }
}