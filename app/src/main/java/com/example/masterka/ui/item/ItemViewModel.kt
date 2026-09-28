package com.example.masterka.ui.item

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.masterka.MasterkaApp
import com.example.masterka.data.StorageNode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class ItemViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = (app as MasterkaApp).dao

    val allCategories = dao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _item = MutableStateFlow<StorageNode?>(null)
    val item = _item.asStateFlow()

    private val _path = MutableStateFlow("")
    val path = _path.asStateFlow()

    fun loadItem(itemId: Long) {
        viewModelScope.launch {
            val node = dao.getById(itemId) ?: return@launch
            _item.value = node
            _path.value = buildPath(node)
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
            dao.updateItemFull(
                id = current.id,
                name = name.trim().ifBlank { current.name },
                qty = qty,
                unit = unit.trim().ifBlank { "шт" },
                note = note.trim().ifBlank { null },
                category = category.trim().ifBlank { null },
                photoPath = current.photoPath
            )
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

    private suspend fun buildPath(node: StorageNode): String {
        val parts = mutableListOf<String>()
        var current: StorageNode? = node.parentId?.let { dao.getById(it) }
        while (current != null) {
            parts.add(0, current.name)
            current = current.parentId?.let { dao.getById(it) }
        }
        return parts.joinToString(" → ")
    }
}