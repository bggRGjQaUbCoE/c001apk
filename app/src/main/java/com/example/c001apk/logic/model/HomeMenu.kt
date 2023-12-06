package com.example.c001apk.logic.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class HomeMenu(
    @PrimaryKey
    var position: Int,
    var title: String,
    var isEnable: Boolean
)