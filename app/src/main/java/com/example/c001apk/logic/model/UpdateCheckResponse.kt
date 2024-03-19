package com.example.c001apk.logic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UpdateCheckResponse(val data: List<Data>) : Parcelable {
    @Parcelize
    data class Data(
        val id: Int,
        val title: String,
        val shorttitle: String,
        val logo: String,
        val apkversionname: String,
        val apkversioncode: Long,
        val apksize: String,
        val lastupdate: Long,
        val packageName: String,
        val changelog: String,
        val pkg_bit_type: Int,
        var localVersionName: String?,
        var localVersionCode: Long?,
        var expand: Boolean = false,
    ) : Parcelable

}