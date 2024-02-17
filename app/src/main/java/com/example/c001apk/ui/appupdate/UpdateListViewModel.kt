package com.example.c001apk.ui.appupdate

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.adapter.Event
import com.example.c001apk.logic.network.Repository.getAppDownloadLink
import kotlinx.coroutines.launch

class UpdateListViewModel : ViewModel() {

    var isInit: Boolean = true
    var versionCode: String? = null
    var packageName: String? = null
    private var appId: String? = null
    val doNext = MutableLiveData<Event<String>>()

    fun onGetDownloadLink() {
        viewModelScope.launch {
            getAppDownloadLink( packageName.toString(), appId.toString(), versionCode.toString())
                .collect{result->
                    val link = result.getOrNull()
                    if (link != null) {
                        doNext.postValue(Event(link))
                    } else {
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }

    }

}