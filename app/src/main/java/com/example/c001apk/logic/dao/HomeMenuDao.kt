package com.example.c001apk.logic.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.c001apk.logic.model.HomeMenu
import kotlinx.coroutines.flow.Flow


@Dao
interface HomeMenuDao {

    @Insert
    suspend fun insert(menu: HomeMenu)

    @Insert
    suspend fun insertList(list: List<HomeMenu>)

    @Query("SELECT * FROM HomeMenu ORDER BY position ASC")
    suspend fun loadAllList(): List<HomeMenu>

    @Query("SELECT * FROM HomeMenu ORDER BY position ASC")
    fun loadAllListLive(): LiveData<List<HomeMenu>>

    @Query("SELECT * FROM HomeMenu ORDER BY position ASC")
    fun loadAllListFlow(): Flow<List<HomeMenu>>

    @Query("SELECT 1 FROM HomeMenu WHERE title = :title LIMIT 1")
    suspend fun isExist(title: String): Boolean

    @Query("DELETE FROM HomeMenu WHERE title = :title")
    suspend fun delete(title: String)

    @Query("DELETE FROM HomeMenu")
    suspend fun deleteAll()

    @Update
    suspend fun updateList(list:List<HomeMenu>)

}