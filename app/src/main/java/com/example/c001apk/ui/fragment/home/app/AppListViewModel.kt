package com.example.c001apk.ui.fragment.home.app

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class AppListViewModel : ViewModel() {

    val appList = ArrayList<AppItem>()
    val items: MutableLiveData<ArrayList<AppItem>> = MutableLiveData()

    fun getItems(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val appList = context.packageManager
                .getInstalledApplications(PackageManager.GET_SHARED_LIBRARY_FILES)
            val newItems = ArrayList<AppItem>()

            for (info in appList) {
                if (((info.flags and ApplicationInfo.FLAG_SYSTEM) != ApplicationInfo.FLAG_SYSTEM)) {

                    val appItem = AppItem().apply {
                        icon = info.loadIcon(context.packageManager)
                        appName = info.loadLabel(context.packageManager).toString()
                        packageName = info.packageName
                        val packageInfo = context.packageManager.getPackageInfo(info.packageName, 0)
                        versionName = "${packageInfo.versionName}(${packageInfo.versionCode})"
                    }

                    newItems.add(appItem)
                }
            }

            withContext(Dispatchers.Main) {
                items.value = newItems
            }
        }
    }

}