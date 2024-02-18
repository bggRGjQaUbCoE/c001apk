package com.example.c001apk.logic.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.c001apk.logic.model.SearchHistory


@Dao
interface BlackListDao {

    @Insert
    fun insert(uid: SearchHistory)

    @Insert
    fun insertAll(list: List<SearchHistory>)

    @Query("select * from SearchHistory ORDER BY id DESC")
    fun loadAllList(): List<SearchHistory>

    @Query("SELECT 1 FROM SearchHistory WHERE keyWord = :uid LIMIT 1")
    fun isExist(uid: String): Boolean

    @Query("DELETE FROM SearchHistory WHERE keyWord = :uid")
    fun delete(uid: String)

    @Query("DELETE FROM SearchHistory")
    fun deleteAll()

}