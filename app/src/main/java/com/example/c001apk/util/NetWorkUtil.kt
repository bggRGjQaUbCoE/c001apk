package com.example.c001apk.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.example.c001apk.MyApplication.Companion.context


object NetWorkUtil {

    fun isWifiConnected(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network: Network? = cm.activeNetwork
        if (network != null) {
            val nc = cm.getNetworkCapabilities(network)
            nc?.let {
                if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return true
                } else if (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return false
                }
            }
        }
        return false
    }

}