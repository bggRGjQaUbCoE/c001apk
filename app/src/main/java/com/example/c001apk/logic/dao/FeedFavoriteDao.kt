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

    @Query("SELECT 1 FROM FeedFavorite WHERE feedId = :id LIMIT 1")
    fun isFavorite(id: String): Boolean

    @Query("delete from FeedFavorite where feedId = :id")
    fun delete(id: String)

    @Query("select * from FeedFavorite ORDER BY id DESC")
    fun loadAllHistory(): List<FeedFavorite>

    @Query("DELETE FROM FeedFavorite")
    fun deleteAll()

}