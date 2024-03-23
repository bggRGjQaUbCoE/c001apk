package com.example.c001apk.ui.applist

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.c001apk.adapter.LoadingState
import com.example.c001apk.logic.model.AppItem
import com.example.c001apk.logic.model.UpdateCheckResponse
import com.example.c001apk.logic.repository.NetworkRepo
import com.example.c001apk.ui.base.BaseViewModel
import com.example.c001apk.util.Utils
import com.example.c001apk.util.Utils.getBase64
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import rikka.core.content.pm.longVersionCodeCompat

class AppListViewModel @AssistedInject constructor(
    @Assisted val packageManager: PackageManager,
    private val networkRepo: NetworkRepo
) : BaseViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(packageManager: PackageManager): AppListViewModel
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun provideFactory(
            assistedFactory: Factory,
            packageManager: PackageManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(packageManager) as T
            }
        }
    }

    val setFab: MutableLiveData<Boolean> = MutableLiveData()
    val items: MutableLiveData<List<AppItem>> = MutableLiveData()
    val appsUpdate = ArrayList<UpdateCheckResponse.Data>()

    override fun fetchData() {}

    private fun fetchAppsUpdate(pkg: String) {
        viewModelScope.launch(Dispatchers.IO) {
            networkRepo.getAppsUpdate(pkg)
                .collect { result ->
                    result.getOrNull()?.let {
                        appsUpdate.clear()
                        appsUpdate.addAll(it)
                        setFab.postValue(true)
                    }
                }
        }

    }

    fun getItems(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val appList = packageManager
                .getInstalledApplications(PackageManager.GET_SHARED_LIBRARY_FILES)
            val newItems = ArrayList<AppItem>()
            val updateCheckJsonObject = JSONObject()

            appList.forEach { info ->
                if (((info.flags and ApplicationInfo.FLAG_SYSTEM) != ApplicationInfo.FLAG_SYSTEM)) {
                    val packageInfo = packageManager.getPackageInfo(info.packageName, 0)

                    val appItem = AppItem().apply {
                        packageName = info.packageName
                        versionName =
                            "${packageInfo.versionName}(${packageInfo.longVersionCodeCompat})"
                        lastUpdateTime = packageInfo.lastUpdateTime
                    }

                    if (appItem.packageName != "com.example.c001apk")
                        newItems.add(appItem)

                    if (info.packageName != "com.example.c001apk")
                        updateCheckJsonObject.put(
                            info.packageName,
                            "0,${packageInfo.longVersionCodeCompat},${Utils.getInstalledAppMd5(info)}"
                        )
                }
            }

            isEnd = true
            items.postValue(newItems.sortedByDescending { it.lastUpdateTime })
            fetchAppsUpdate(updateCheckJsonObject.toString().getBase64(false))
            loadingState.postValue(LoadingState.LoadingDone)
        }
    }

}