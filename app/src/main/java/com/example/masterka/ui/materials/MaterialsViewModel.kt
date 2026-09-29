package com.example.masterka.ui.materials

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.masterka.MasterkaApp
import com.example.masterka.data.NodeType
import com.example.masterka.data.StorageNode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


enum class MaterialsSort {
    NAME_ASC,
    NAME_DESC,
    QTY_ASC,
    QTY_DESC,
    LOCATION
}

data class MaterialRow(
    val item: StorageNode,
    val location: String,
    val categoryIcon: String? = null,
)

class MaterialsViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = (app as MasterkaApp).dao

    private val _query = MutableStateFlow("")

    private val categoryDao = (app as MasterkaApp).categoryDao
    val query = _query.asStateFlow()

    private val _sort = MutableStateFlow(MaterialsSort.NAME_ASC)
    val sort = _sort.asStateFlow()

    // Кэш путей: parentId -> путь
    private val _rows = MutableStateFlow<List<MaterialRow>>(emptyList())
    val rows = _rows.asStateFlow()

    init {
        viewModelScope.launch {
            val categoriesMap = mutableMapOf<String, String>()

            // Подписка на изменения категорий
            launch {
                categoryDao.getAllCategories().collect { categories ->
                    categoriesMap.clear()
                    categories.forEach { categoriesMap[it.name] = it.iconName }
                    // Обогащаем уже загруженные rows
                    _rows.value = _rows.value.map { row ->
                        row.copy(categoryIcon = row.item.category?.let { categoriesMap[it] })
                    }
                }
            }

            // Подписка на вещи
            dao.getAllItems().collect { items ->
                val withPath = items.map { item ->
                    MaterialRow(
                        item = item,
                        location = buildPathFor(item),
                        categoryIcon = item.category?.let { categoriesMap[it] }
                    )
                }
                _rows.value = withPath
            }
        }
    }

    fun setQuery(q: String) { _query.value = q }
    fun setSort(s: MaterialsSort) { _sort.value = s }

    /**
     * Готовый список — с учётом поиска и сортировки.
     * Вызывается из UI через remember(query, sort, rows).
     */
    fun filteredAndSorted(): List<MaterialRow> {
        val q = _query.value.trim().lowercase()
        val s = _sort.value
        val base = _rows.value

        val filtered = if (q.isBlank()) base else base.filter {
            it.item.name.lowercase().contains(q) ||
                    (it.item.note?.lowercase()?.contains(q) == true) ||
                    it.location.lowercase().contains(q) ||
                    (it.item.category?.lowercase()?.contains(q) == true)   // ← добавь эту строку
        }

        return when (s) {
            MaterialsSort.NAME_ASC -> filtered.sortedBy { it.item.name.lowercase() }
            MaterialsSort.NAME_DESC -> filtered.sortedByDescending { it.item.name.lowercase() }
            MaterialsSort.QTY_ASC -> filtered.sortedBy { it.item.quantity }
            MaterialsSort.QTY_DESC -> filtered.sortedByDescending { it.item.quantity }
            MaterialsSort.LOCATION -> filtered.sortedBy { it.location.lowercase() }
        }
    }

    /**
     * Собирает путь от корня до родителя вещи.
     * Возвращает строку вида "Мастерская → Шкаф → Полка 1".
     */
    private suspend fun buildPathFor(item: StorageNode): String {
        val parts = mutableListOf<String>()
        var current: StorageNode? = item.parentId?.let { dao.getById(it) }
        while (current != null) {
            parts.add(0, current.name)
            current = current.parentId?.let { dao.getById(it) }
        }
        return if (parts.isEmpty()) "—" else parts.joinToString(" → ")
    }

    fun updateQuantity(item: StorageNode, newQty: Float) {
        viewModelScope.launch {
            dao.updateQuantity(item.id, newQty, item.unit)
        }
    }

    fun deleteItem(item: StorageNode) {
        viewModelScope.launch {
            dao.delete(item)
        }
    }
}