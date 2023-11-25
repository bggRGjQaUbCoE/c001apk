package com.example.c001apk.logic.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class HomeMenu(
    var title: String,
    var isEnable: Boolean
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}