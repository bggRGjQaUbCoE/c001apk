package com.example.c001apk.logic.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.c001apk.logic.model.HomeMenu


@Dao
interface HomeMenuDao {

    @Insert
    fun insert(menu: HomeMenu)

    @Query("select * from HomeMenu")
    fun loadAll(): List<HomeMenu>

    @Query("SELECT 1 FROM HomeMenu WHERE title = :title LIMIT 1")
    fun isExist(title: String): Boolean

    @Query("DELETE FROM HomeMenu WHERE title = :title")
    fun delete(title: String)

    @Query("DELETE FROM HomeMenu")
    fun deleteAll()

}