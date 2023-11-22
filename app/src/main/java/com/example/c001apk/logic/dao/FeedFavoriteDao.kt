package com.example.c001apk.logic.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.c001apk.logic.model.FeedFavorite

@Dao
interface FeedFavoriteDao {
    @Insert
    fun insert(feedFavorite: FeedFavorite)

    @Query("SELECT COUNT(*) AS count FROM FeedFavorite WHERE feedId = :id")
    fun queryFavoriteCount(id: String): Long

    fun isFavorite(id: String) = queryFavoriteCount(id) > 0

    @Query("delete from FeedFavorite where feedId = :id")
    fun delete(id: String)
}