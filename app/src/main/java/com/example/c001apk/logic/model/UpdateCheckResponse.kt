package com.example.c001apk.logic.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parceler
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
    ) : Parcelable {

        constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readLong(),
            parcel.readString().toString(),
            parcel.readLong(),
            parcel.readString().toString(),
            parcel.readString().toString(),
            parcel.readInt(),
            parcel.readString().toString(),
            parcel.readLong(),
            parcel.readInt() == 1,
        )

        companion object : Parceler<Data> {
            override fun Data.write(parcel: Parcel, flags: Int) {
                parcel.writeInt(id)
                parcel.writeString(title)
                parcel.writeString(shorttitle)
                parcel.writeString(logo)
                parcel.writeString(apkversionname)
                parcel.writeLong(apkversioncode)
                parcel.writeString(apksize)
                parcel.writeLong(lastupdate)
                parcel.writeString(packageName)
                parcel.writeString(changelog)
                parcel.writeInt(pkg_bit_type)
                parcel.writeString(localVersionName)
                parcel.writeLong(localVersionCode ?: -1)
                parcel.writeInt(if (expand) 1 else 0)
            }

            override fun create(parcel: Parcel): Data {
                return Data(parcel)
            }
        }

    }

}