package com.example.c001apk.logic.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.c001apk.logic.model.SearchHistory


@Dao
interface SearchHistoryDao {

    @Insert
    fun insert(keyWord: SearchHistory)

    @Query("select * from SearchHistory ORDER BY id DESC")
    fun loadAllHistory(): List<SearchHistory>

    @Query("SELECT 1 FROM SearchHistory WHERE keyWord = :keyWord LIMIT 1")
    fun isExist(keyWord: String): Boolean

    @Query("DELETE FROM SearchHistory WHERE keyWord = :keyWord")
    fun delete(keyWord: String)

    @Query("DELETE FROM SearchHistory")
    fun deleteAll()

}