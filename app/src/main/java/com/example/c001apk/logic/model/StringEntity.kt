package com.example.c001apk.logic.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class StringEntity(
    @PrimaryKey(autoGenerate = false)
    var data: String,
    var id: Long = System.currentTimeMillis()
)