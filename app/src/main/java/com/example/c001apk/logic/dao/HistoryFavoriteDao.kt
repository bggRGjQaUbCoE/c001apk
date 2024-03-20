package com.example.c001apk.logic.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.c001apk.logic.model.FeedEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface HistoryFavoriteDao {

    @Insert
    suspend fun insert(data: FeedEntity)

    @Query("SELECT * FROM FeedEntity ORDER BY id DESC")
    suspend fun loadAllList(): List<FeedEntity>

    @Query("SELECT * FROM FeedEntity ORDER BY id DESC")
    fun loadAllListLive(): LiveData<List<FeedEntity>>

    @Query("SELECT * FROM FeedEntity ORDER BY id DESC")
    fun loadAllListFlow(): Flow<List<FeedEntity>>

    @Query("SELECT 1 FROM FeedEntity WHERE fid = :fid LIMIT 1")
    suspend fun isExist(fid: String): Boolean

    @Query("DELETE FROM FeedEntity WHERE fid = :fid")
    suspend fun delete(fid: String)

    @Query("DELETE FROM FeedEntity")
    suspend fun deleteAll()

}