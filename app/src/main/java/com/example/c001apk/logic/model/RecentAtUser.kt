package com.example.c001apk.logic.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RecentAtUser(
    val group: String = "recent",
    val avatar: String,
    val username: String,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = System.currentTimeMillis()
}