package com.example.masterka.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SpaceDao {

    @Query("SELECT * FROM spaces ORDER BY createdAt")
    fun getAll(): Flow<List<Space>>

    @Query("SELECT * FROM spaces ORDER BY createdAt")
    suspend fun getAllOnce(): List<Space>

    @Query("SELECT * FROM spaces WHERE id = :id")
    suspend fun getById(id: Long): Space?

    @Insert
    suspend fun insert(space: Space): Long

    @Update
    suspend fun update(space: Space)

    @Delete
    suspend fun delete(space: Space)
}