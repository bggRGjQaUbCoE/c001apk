package com.example.c001apk.logic.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FeedEntity(
    val fid: String,
    val uid: String,
    val uname: String,
    val avatar: String,
    val device: String,
    val message: String,
    val pubDate: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}