package com.example.c001apk.logic.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class StringEntity(
    var data: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}