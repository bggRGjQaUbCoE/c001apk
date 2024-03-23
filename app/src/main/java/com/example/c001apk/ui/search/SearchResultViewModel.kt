package com.example.c001apk.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject


class SearchResultViewModel @AssistedInject constructor(
    @Assisted("keyWord") var keyWord: String,
    @Assisted("pageType") var pageType: String,
    @Assisted("pageParam") val pageParam: String,
    @Assisted("title") var title: String
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("keyWord") keyWord: String,
            @Assisted("pageType") pageType: String,
            @Assisted("pageParam") pageParam: String,
            @Assisted("title") title: String,
        ): SearchResultViewModel
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun provideFactory(
            assistedFactory: Factory,
            keyWord: String,
            pageType: String,
            pageParam: String,
            title: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(keyWord, pageType, pageParam, title) as T
            }
        }
    }

    var sort: String = "default" //hot // reply
    var feedType: String = "all"

}