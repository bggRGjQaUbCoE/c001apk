package com.example.c001apk.logic.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SearchHistory(
    var keyWord: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}