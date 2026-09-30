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

    @Query("SELECT * FROM nodes WHERE parentId IS NULL ORDER BY createdAt LIMIT 1")
    suspend fun getRootNode(): StorageNode?

    @Query("UPDATE nodes SET photoPathsJson = :json WHERE id = :id")
    suspend fun updatePhotoPaths(id: Long, json: String?)

    @Query("DELETE FROM nodes WHERE parentId = :parentId AND photoIndex = :photoIndex AND type != 'ITEM'")
    suspend fun deleteChildrenAtPhoto(parentId: Long, photoIndex: Int)

    @Query("""
    UPDATE nodes 
    SET lentTo = :lentTo, lentAt = :lentAt, returnBy = :returnBy
    WHERE id = :id
""")
    suspend fun lendItem(
        id: Long,
        lentTo: String,
        lentAt: Long,
        returnBy: Long?
    )

    @Query("""
    UPDATE nodes 
    SET lentTo = NULL, lentAt = NULL, returnBy = NULL
    WHERE id = :id
""")
    suspend fun returnItem(id: Long)

    @Query("""
    UPDATE nodes 
    SET note = :note, noteUpdatedAt = :updatedAt
    WHERE id = :id
""")
    suspend fun updateNoteWithTimestamp(id: Long, note: String?, updatedAt: Long)


    // ===== CRUD =====
    @Insert
    suspend fun insert(node: StorageNode): Long

    @Update
    suspend fun update(node: StorageNode)

    @Delete
    suspend fun delete(node: StorageNode)

    @Query("DELETE FROM nodes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT DISTINCT category FROM nodes WHERE category IS NOT NULL AND category != ''")
    suspend fun getAllCategoriesOnce(): List<String>

    // ==== Пространства ====

    @Query("SELECT * FROM nodes WHERE parentId IS NULL AND spaceId = :spaceId ORDER BY createdAt")
    suspend fun getRootsBySpaceOnce(spaceId: Long): List<StorageNode>

    @Query("SELECT * FROM nodes WHERE parentId IS NULL AND spaceId = :spaceId ORDER BY createdAt LIMIT 1")
    fun getRootBySpace(spaceId: Long): Flow<StorageNode?>

    @Query("SELECT * FROM nodes WHERE type = 'ITEM' AND spaceId = :spaceId ORDER BY name")
    fun getAllItemsBySpace(spaceId: Long): Flow<List<StorageNode>>

    @Query("SELECT * FROM nodes WHERE type = 'ITEM' AND spaceId = :spaceId ORDER BY name")
    suspend fun getAllItemsBySpaceOnce(spaceId: Long): List<StorageNode>

    @Query("DELETE FROM nodes WHERE spaceId = :spaceId")
    suspend fun deleteAllNodesInSpace(spaceId: Long)
}