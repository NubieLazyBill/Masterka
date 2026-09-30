package com.example.masterka.ui.materials

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.masterka.MasterkaApp
import com.example.masterka.data.SpacePreferences
import com.example.masterka.data.StorageNode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


enum class MaterialsSort {
    NAME_ASC, NAME_DESC, QTY_ASC, QTY_DESC, LOCATION
}

data class MaterialRow(
    val item: StorageNode,
    val location: String,
    val categoryIcon: String? = null,
)

class MaterialsViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = (app as MasterkaApp).dao
    private val categoryDao = (app as MasterkaApp).categoryDao

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    private val _sort = MutableStateFlow(MaterialsSort.NAME_ASC)
    val sort = _sort.asStateFlow()

    // ==== Активное помещение ====
    private val _activeSpaceId = MutableStateFlow(0L)
    val activeSpaceId = _activeSpaceId.asStateFlow()

    // true = показывать только активное помещение, false = всё
    private val _onlyActiveSpace = MutableStateFlow(true)
    val onlyActiveSpace = _onlyActiveSpace.asStateFlow()

    private val _rows = MutableStateFlow<List<MaterialRow>>(emptyList())
    val rows = _rows.asStateFlow()

    init {
        viewModelScope.launch {
            _activeSpaceId.value = SpacePreferences.getActiveSpaceIdOnce(getApplication())
        }

        viewModelScope.launch {
            val categoriesMap = mutableMapOf<String, String>()

            launch {
                categoryDao.getAllCategories().collect { categories ->
                    categoriesMap.clear()
                    categories.forEach { categoriesMap[it.name] = it.iconName }
                    _rows.value = _rows.value.map { row ->
                        row.copy(categoryIcon = row.item.category?.let { categoriesMap[it] })
                    }
                }
            }

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
    fun setOnlyActiveSpace(value: Boolean) { _onlyActiveSpace.value = value }

    /**
     * Готовый список с учётом поиска, сортировки и фильтра по помещению.
     */
    fun filteredAndSorted(): List<MaterialRow> {
        val q = _query.value.trim().lowercase()
        val s = _sort.value
        val base = _rows.value

        // ==== Фильтр по помещению ====
        val filtered = base
            .filter { row ->
                !_onlyActiveSpace.value || row.item.spaceId == _activeSpaceId.value
            }
            .filter { row ->
                if (q.isBlank()) true
                else row.item.name.lowercase().contains(q) ||
                        (row.item.note?.lowercase()?.contains(q) == true) ||
                        row.location.lowercase().contains(q) ||
                        (row.item.category?.lowercase()?.contains(q) == true)
            }

        return when (s) {
            MaterialsSort.NAME_ASC -> filtered.sortedBy { it.item.name.lowercase() }
            MaterialsSort.NAME_DESC -> filtered.sortedByDescending { it.item.name.lowercase() }
            MaterialsSort.QTY_ASC -> filtered.sortedBy { it.item.quantity }
            MaterialsSort.QTY_DESC -> filtered.sortedByDescending { it.item.quantity }
            MaterialsSort.LOCATION -> filtered.sortedBy { it.location.lowercase() }
        }
    }

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