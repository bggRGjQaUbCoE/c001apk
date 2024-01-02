package com.example.c001apk.logic.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.c001apk.logic.model.BrowseHistory


@Dao
interface BrowseHistoryDao {

    @Insert
    fun insert(browseHistory: BrowseHistory)

    @Query("select * from BrowseHistory ORDER BY id DESC")
    fun loadAllHistory(): List<BrowseHistory>

    @Query("SELECT 1 FROM BrowseHistory WHERE fid = :fid LIMIT 1")
    fun isExist(fid: String): Boolean

    @Query("DELETE FROM BrowseHistory WHERE fid = :fid")
    fun delete(fid: String)

    @Query("DELETE FROM BrowseHistory")
    fun deleteAll()

}