package com.example.c001apk.ui.fragment.home

import androidx.lifecycle.ViewModel
import com.example.c001apk.logic.model.HomeMenu

class HomeViewModel : ViewModel() {
    var isInit = true
    var tabList = ArrayList<String>()
    var position: Int = 0
    val menuList = ArrayList<HomeMenu>()
}