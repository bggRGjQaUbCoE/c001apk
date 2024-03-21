package com.example.c001apk.ui.search

import androidx.lifecycle.ViewModel

class SearchResultViewModel : ViewModel() {
    var pageParam: String? = null
    var pageType: String? = null
    var keyWord: String? = null
    var title: String? = null
    var sort: String = "default" //hot // reply
    var feedType: String = "all"
    var tabList: MutableList<String>? = null
}