package com.example.masterka.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): Category?

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): Category?

    @Insert
    suspend fun insert(category: Category): Long

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    /**
     * Переименовать категорию во всех вещах.
     */
    @Query("UPDATE nodes SET category = :newName WHERE category = :oldName")
    suspend fun renameCategoryInNodes(oldName: String, newName: String)

    /**
     * Очистить категорию у всех вещей (при удалении категории).
     */
    @Query("UPDATE nodes SET category = NULL WHERE category = :name")
    suspend fun clearCategoryInNodes(name: String)

    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE")
    suspend fun getAllCategoriesOnce(): List<Category>


}