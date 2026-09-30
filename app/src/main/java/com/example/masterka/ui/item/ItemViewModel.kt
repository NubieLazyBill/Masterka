package com.example.masterka.ui.item

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.masterka.MasterkaApp
import com.example.masterka.data.Category
import com.example.masterka.data.StorageNode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ItemViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = (app as MasterkaApp).dao
    private val categoryDao = (app as MasterkaApp).categoryDao

    // Объединение: справочник + категории из вещей.
    val allCategories: StateFlow<List<String>> = combine(
        categoryDao.getAllCategoryNames(),
        dao.getAllCategories()
    ) { fromDb, fromNodes ->
        (fromDb + fromNodes)
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _item = MutableStateFlow<StorageNode?>(null)
    val item = _item.asStateFlow()

    // Цепочка предков вещи: [Мастерская, Шкаф, Полка 1]. Без самой вещи.
    private val _pathNodes = MutableStateFlow<List<StorageNode>>(emptyList())
    val pathNodes = _pathNodes.asStateFlow()

    // Удобная строка для отладки/старых мест
    private val _path = MutableStateFlow("")
    val path = _path.asStateFlow()

    fun loadItem(itemId: Long) {
        viewModelScope.launch {
            val node = dao.getById(itemId) ?: return@launch
            _item.value = node
            val chain = buildPathNodes(node)
            _pathNodes.value = chain
            _path.value = chain.joinToString(" → ") { it.name }
        }
    }

    fun save(
        name: String,
        qty: Float,
        unit: String,
        category: String,
        note: String
    ) {
        val current = _item.value ?: return
        viewModelScope.launch {
            val oldNote = current.note ?: ""
            val newNote = note.trim()
            val noteChanged = oldNote != newNote

            dao.updateItemFull(
                id = current.id,
                name = name.trim().ifBlank { current.name },
                qty = qty,
                unit = unit.trim().ifBlank { "шт" },
                note = newNote.ifBlank { null },
                category = category.trim().ifBlank { null },
                photoPath = current.photoPath
            )

            ensureCategoryInDb(category)

            if (noteChanged) {
                dao.updateNoteWithTimestamp(
                    id = current.id,
                    note = newNote.ifBlank { null },
                    updatedAt = System.currentTimeMillis()
                )
            }

            _item.value = dao.getById(current.id)
        }
    }

    fun setPhoto(path: String) {
        val current = _item.value ?: return
        viewModelScope.launch {
            dao.updateItemFull(
                id = current.id,
                name = current.name,
                qty = current.quantity,
                unit = current.unit,
                note = current.note,
                category = current.category,
                photoPath = path
            )
            _item.value = dao.getById(current.id)
        }
    }

    fun deleteItem(onDone: () -> Unit) {
        val current = _item.value ?: return
        viewModelScope.launch {
            dao.delete(current)
            onDone()
        }
    }

    // ==== Аренда/одолжение ====

    fun lendItem(lentTo: String, returnBy: Long?) {
        val current = _item.value ?: return
        viewModelScope.launch {
            dao.lendItem(
                id = current.id,
                lentTo = lentTo.trim(),
                lentAt = System.currentTimeMillis(),
                returnBy = returnBy
            )
            _item.value = dao.getById(current.id)
        }
    }

    fun returnItem() {
        val current = _item.value ?: return
        viewModelScope.launch {
            dao.returnItem(current.id)
            _item.value = dao.getById(current.id)
        }
    }

    /**
     * Строит цепочку предков (от корня к родителю), БЕЗ самой вещи.
     */
    private suspend fun buildPathNodes(node: StorageNode): List<StorageNode> {
        val path = mutableListOf<StorageNode>()
        var current: StorageNode? = node.parentId?.let { dao.getById(it) }
        while (current != null) {
            path.add(0, current)
            current = current.parentId?.let { dao.getById(it) }
        }
        return path
    }

    /**
     * Гарантирует, что категория есть в таблице `categories`.
     */
    private suspend fun ensureCategoryInDb(name: String?) {
        val clean = name?.trim()?.takeIf { it.isNotBlank() } ?: return
        val existing = categoryDao.getByName(clean)
        if (existing == null) {
            categoryDao.insert(
                Category(name = clean, iconName = "Category")
            )
        }
    }
}