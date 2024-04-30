package com.example.c001apk.logic.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RecentAtUser(
    var id: Long = System.currentTimeMillis(),
    val group: String = "recent",
    val avatar: String,
    @PrimaryKey(autoGenerate = false)
    val username: String,
)