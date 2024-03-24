package com.example.c001apk.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.logic.model.HomeMenu
import com.example.c001apk.logic.repository.HomeMenuRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeMenuRepo: HomeMenuRepo
) : ViewModel() {

    var isInit = true
    var position: Int = 0

    val tabListLiveData: LiveData<List<HomeMenu>> = homeMenuRepo.loadAllListLive()
    val restart = MutableLiveData<Boolean>()

    val defaultList by lazy {
        listOf(
            HomeMenu(0, "关注", true),
            HomeMenu(1, "应用", true),
            HomeMenu(2, "头条", true),
            HomeMenu(3, "热榜", true),
            HomeMenu(4, "话题", true),
            HomeMenu(5, "数码", true),
            HomeMenu(6, "酷图", true)
        )
    }

    fun initTab() {
        viewModelScope.launch(Dispatchers.IO) {
            homeMenuRepo.insertList(defaultList)
        }
    }

    fun updateTab(menuList: List<HomeMenu>) {
        viewModelScope.launch(Dispatchers.IO) {
            homeMenuRepo.updateList(menuList)
            restart.postValue(true)
        }
    }

}