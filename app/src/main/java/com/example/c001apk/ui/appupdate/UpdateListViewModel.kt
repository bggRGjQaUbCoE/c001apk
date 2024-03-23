package com.example.c001apk.ui.appupdate

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.c001apk.logic.repository.NetworkRepo
import com.example.c001apk.ui.base.BaseViewModel
import com.example.c001apk.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateListViewModel @Inject constructor(
    private val networkRepo: NetworkRepo
) : BaseViewModel() {

    var appName: String? = null
    var versionName: String? = null
    var versionCode: String? = null
    var packageName: String? = null
    private var appId: String? = null
    val download = MutableLiveData<Event<Boolean>>()
    var urlMap: HashMap<String, String> = HashMap()

    fun onGetDownloadLink() {
        if (urlMap[packageName].isNullOrEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                networkRepo.getAppDownloadLink(
                    packageName.toString(),
                    appId.toString(),
                    versionCode.toString()
                )
                    .collect { result ->
                        val link = result.getOrNull()
                        if (link != null) {
                            urlMap[packageName.toString()] = link
                            download.postValue(Event(true))
                        } else {
                            result.exceptionOrNull()?.printStackTrace()
                        }
                    }
            }
        } else
            download.postValue(Event(true))
    }

    override fun fetchData() {}

}