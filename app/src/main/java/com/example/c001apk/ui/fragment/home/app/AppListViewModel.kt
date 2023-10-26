package com.example.c001apk.ui.fragment.home.app

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AppListViewModel : ViewModel() {

    var isInit = true
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
                        versionName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                            "${packageInfo.versionName}(${packageInfo.longVersionCode})"
                        else "${packageInfo.versionName}(${packageInfo.versionCode})"


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