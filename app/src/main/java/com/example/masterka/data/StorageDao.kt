package com.example.masterka.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StorageDao {

    // ===== ROOT (верхний уровень) =====
    @Query("SELECT * FROM nodes WHERE parentId IS NULL ORDER BY createdAt")
    fun getRoots(): Flow<List<StorageNode>>

    // ===== ДЕТИ (поток для UI) =====
    @Query("SELECT * FROM nodes WHERE parentId = :parentId ORDER BY createdAt")
    fun getChildren(parentId: Long): Flow<List<StorageNode>>

    // ===== ДЕТИ (одноразово, для VM) =====
    @Query("SELECT * FROM nodes WHERE parentId = :parentId ORDER BY createdAt")
    suspend fun getChildrenOnce(parentId: Long): List<StorageNode>

    // ===== ПО ID =====
    @Query("SELECT * FROM nodes WHERE id = :id")
    suspend fun getById(id: Long): StorageNode?

    @Query("SELECT * FROM nodes WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<StorageNode?>

    // ===== ПОИСК =====
    @Query("SELECT * FROM nodes WHERE name LIKE '%' || :query || '%' ORDER BY name LIMIT 50")
    suspend fun search(query: String): List<StorageNode>

    @Query("UPDATE nodes SET polygonJson = :json WHERE id = :id")
    suspend fun updatePolygon(id: Long, json: String?)

    // ===== CRUD =====
    @Insert
    suspend fun insert(node: StorageNode): Long

    @Update
    suspend fun update(node: StorageNode)

    @Delete
    suspend fun delete(node: StorageNode)

    @Query("DELETE FROM nodes WHERE id = :id")
    suspend fun deleteById(id: Long)
}