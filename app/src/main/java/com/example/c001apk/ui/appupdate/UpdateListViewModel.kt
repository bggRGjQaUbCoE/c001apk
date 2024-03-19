package com.example.c001apk.ui.appupdate

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.c001apk.logic.network.Repository.getAppDownloadLink
import com.example.c001apk.util.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UpdateListViewModel : ViewModel() {

    var appName: String? = null
    var isInit: Boolean = true
    var versionName: String? = null
    var versionCode: String? = null
    var packageName: String? = null
    private var appId: String? = null
    val doNext = MutableLiveData<Event<Boolean>>()
    var url: String? = null

    fun onGetDownloadLink() {
        if (url.isNullOrEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                getAppDownloadLink(packageName.toString(), appId.toString(), versionCode.toString())
                    .collect { result ->
                        val link = result.getOrNull()
                        if (link != null) {
                            url = link
                            doNext.postValue(Event(true))
                        } else {
                            result.exceptionOrNull()?.printStackTrace()
                        }
                    }
            }
        } else
            doNext.postValue(Event(true))
    }

}