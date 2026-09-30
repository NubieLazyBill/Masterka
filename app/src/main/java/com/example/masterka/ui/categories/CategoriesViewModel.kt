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
    private val dao = (app as MasterkaApp).dao

    val categories = categoryDao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // ==== Досоздание категорий из вещей (одноразово при заходе) ====
        viewModelScope.launch {
            val existingNames = categoryDao.getAllCategoriesOnce().map { it.name }.toSet()
            val categoriesFromNodes = dao.getAllCategoriesOnce()

            categoriesFromNodes.forEach { name ->
                if (name.isNotBlank() && name !in existingNames) {
                    categoryDao.insert(
                        Category(name = name, iconName = "Category")
                    )
                }
            }
        }
    }

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

            categoryDao.update(
                category.copy(
                    name = cleanNewName,
                    iconName = newIcon
                )
            )
            if (oldName != cleanNewName) {
                categoryDao.renameCategoryInNodes(oldName, cleanNewName)
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            categoryDao.clearCategoryInNodes(category.name)
            categoryDao.delete(category)
        }
    }
}