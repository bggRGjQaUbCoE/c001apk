package com.example.c001apk.ui.fragment.home.ranking

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.example.c001apk.logic.model.HomeFeedResponse
import com.example.c001apk.logic.network.Repository

class HomeRankingViewModel : ViewModel() {

    var isInit = true
    val homeRankingList = ArrayList<HomeFeedResponse.Data>()

    var isRefreshing = true
    var isLoadMore = false

    var page = 1
    var lastItem = ""

    private val getHomeRankingData = MutableLiveData<String>()

    val homeRankingData = getHomeRankingData.switchMap {
        Repository.getHomeRanking(page, lastItem)
    }

    fun getHomeRanking() {
        getHomeRankingData.value = getHomeRankingData.value
    }
}