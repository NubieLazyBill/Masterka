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

    // Все вещи и материалы (ITEM) во всей мастерской — для таблицы
    @Query("SELECT * FROM nodes WHERE type = 'ITEM' ORDER BY name")
    fun getAllItems(): Flow<List<StorageNode>>

    // Поиск по вещам
    @Query("SELECT * FROM nodes WHERE type = 'ITEM' AND name LIKE '%' || :query || '%' ORDER BY name")
    suspend fun searchItems(query: String): List<StorageNode>

    // Обновление количества
    @Query("UPDATE nodes SET quantity = :qty, unit = :unit WHERE id = :id")
    suspend fun updateQuantity(id: Long, qty: Float, unit: String)

    @Query("""
    UPDATE nodes 
    SET name = :name, quantity = :qty, unit = :unit, 
        note = :note, category = :category, photoPath = :photoPath
    WHERE id = :id
""")
    suspend fun updateItemFull(
        id: Long,
        name: String,
        qty: Float,
        unit: String,
        note: String?,
        category: String?,
        photoPath: String?
    )

    @Query("SELECT DISTINCT category FROM nodes WHERE category IS NOT NULL AND category != '' ORDER BY category COLLATE NOCASE")
    fun getAllCategories(): Flow<List<String>>


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