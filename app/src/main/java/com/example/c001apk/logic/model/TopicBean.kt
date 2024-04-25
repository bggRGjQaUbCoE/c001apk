package com.example.c001apk.logic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TopicBean(
    val url: String,
    val title: String
): Parcelable