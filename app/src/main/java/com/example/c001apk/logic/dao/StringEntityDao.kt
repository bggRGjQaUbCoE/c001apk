package com.example.c001apk.logic.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.c001apk.logic.model.StringEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface StringEntityDao {

    @Insert
    suspend fun insert(data: StringEntity)

    @Insert
    suspend fun insertList(list: List<StringEntity>)

    @Query("SELECT * FROM StringEntity ORDER BY id DESC")
    suspend fun loadAllList(): List<StringEntity>

    @Query("SELECT * FROM StringEntity ORDER BY id DESC")
    fun loadAllListLive(): LiveData<List<StringEntity>>

    @Query("SELECT * FROM StringEntity ORDER BY id DESC")
    fun loadAllListFlow(): Flow<List<StringEntity>>

    @Query("SELECT 1 FROM StringEntity WHERE data = :data LIMIT 1")
    suspend fun isExist(data: String): Boolean

    @Transaction
    @Query("SELECT 1 FROM StringEntity WHERE :data LIKE '%' || data || '%' LIMIT 1")
    suspend fun isContain(data: String): Boolean

    @Query("DELETE FROM StringEntity WHERE data = :data")
    suspend fun delete(data: String)

    @Delete
    suspend fun delete(data: StringEntity)

    @Query("DELETE FROM StringEntity")
    suspend fun deleteAll()

    @Query("UPDATE StringEntity SET id = :newId WHERE data = :data")
    suspend fun updateHistory(data: String, newId: Long)

    @Query("UPDATE StringEntity SET id = :newId , data = :newData WHERE data = :oldData")
    suspend fun updateEmoji(oldData: String, newData: String, newId: Long)

}