package com.example.masterka.ui.categories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.masterka.MasterkaApp
import com.example.masterka.data.Category
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoriesViewModel(app: Application) : AndroidViewModel(app) {

    private val categoryDao = (app as MasterkaApp).categoryDao

    val categories = categoryDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCategory(name: String, iconName: String) {
        viewModelScope.launch {
            categoryDao.insert(
                Category(
                    name = name.trim(),
                    iconName = iconName
                )
            )
        }
    }

    fun renameCategory(category: Category, newName: String, newIcon: String) {
        viewModelScope.launch {
            val oldName = category.name
            val cleanNewName = newName.trim()
            if (cleanNewName.isBlank()) return@launch

            // 1. Обновляем в таблице categories
            categoryDao.update(
                category.copy(
                    name = cleanNewName,
                    iconName = newIcon
                )
            )

            // 2. Если имя изменилось — переименовываем во всех вещах
            if (oldName != cleanNewName) {
                categoryDao.renameCategoryInNodes(oldName, cleanNewName)
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            // Сначала очищаем категорию у всех вещей
            categoryDao.clearCategoryInNodes(category.name)
            // Потом удаляем саму категорию
            categoryDao.delete(category)
        }
    }
}