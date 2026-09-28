package com.example.masterka.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.masterka.MasterkaApp
import com.example.masterka.data.NodeType
import com.example.masterka.data.StorageNode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SearchResult(
    val node: StorageNode,
    val path: String
)

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = (app as MasterkaApp).dao

    val zones = dao.getRoots()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addZone(name: String, photoPath: String? = null) {
        viewModelScope.launch {
            dao.insert(
                StorageNode(
                    parentId = null,
                    name = name.trim(),
                    type = NodeType.ZONE,
                    photoPath = photoPath
                )
            )
        }
    }

    fun deleteZone(node: StorageNode) {
        viewModelScope.launch {
            dao.delete(node)
        }
    }

    /**
     * Поиск по всем узлам (зоны, контейнеры, вещи).
     * Возвращает список с путём до каждого узла.
     */
    suspend fun searchAll(query: String): List<SearchResult> {
        val q = query.trim()
        if (q.isBlank()) return emptyList()

        val found = dao.search(q)
        val results = found.map { node ->
            SearchResult(node = node, path = buildPath(node))
        }
        return results.sortedByDescending {
            it.node.name.lowercase().startsWith(q.lowercase())
        }
    }

    private suspend fun buildPath(node: StorageNode): String {
        val parts = mutableListOf<String>()
        var current: StorageNode? = node
        while (current != null) {
            parts.add(0, current.name)
            current = current.parentId?.let { dao.getById(it) }
        }
        return parts.joinToString(" → ")
    }
}