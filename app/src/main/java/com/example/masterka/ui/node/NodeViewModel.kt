package com.example.masterka.ui.node

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.masterka.MasterkaApp
import com.example.masterka.data.Category
import com.example.masterka.data.NodeType
import com.example.masterka.data.NormalizedPoint
import com.example.masterka.data.PolygonCodec
import com.example.masterka.data.StorageNode
import com.example.masterka.data.PhotoPathsCodec
import com.example.masterka.data.allPhotoPaths
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NodeViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = (app as MasterkaApp).dao
    private val categoryDao = (app as MasterkaApp).categoryDao

    private val _currentNode = MutableStateFlow<StorageNode?>(null)
    val currentNode = _currentNode.asStateFlow()

    private val _children = MutableStateFlow<List<StorageNode>>(emptyList())
    val children = _children.asStateFlow()

    private val _breadcrumbs = MutableStateFlow<List<StorageNode>>(emptyList())
    val breadcrumbs = _breadcrumbs.asStateFlow()

    val allCategories: StateFlow<List<String>> = combine(
        categoryDao.getAllCategoryNames(),
        dao.getAllCategories()
    ) { fromDb, fromNodes ->
        (fromDb + fromNodes).filter { it.isNotBlank() }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categoriesMap: StateFlow<Map<String, String>> = categoryDao.getAllCategories()
        .map { list -> list.associate { it.name to it.iconName } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun loadNode(nodeId: Long) {
        viewModelScope.launch {
            val node = dao.getById(nodeId) ?: return@launch
            _currentNode.value = node
            _breadcrumbs.value = buildBreadcrumbs(node)
            _children.value = dao.getChildrenOnce(nodeId)
        }
    }

    fun addPhoto(path: String) {
        val node = _currentNode.value ?: return
        viewModelScope.launch {
            val current = node.allPhotoPaths().toMutableList()
            current.add(path)
            val updated = node.copy(photoPathsJson = PhotoPathsCodec.encode(current))
            dao.update(updated)
            _currentNode.value = updated
        }
    }

    fun removePhotoAt(index: Int) {
        val node = _currentNode.value ?: return
        viewModelScope.launch {
            val current = node.allPhotoPaths().toMutableList()
            if (index !in current.indices) return@launch
            current.removeAt(index)
            dao.deleteChildrenAtPhoto(node.id, index)
            val updated = node.copy(photoPathsJson = PhotoPathsCodec.encode(current))
            dao.update(updated)
            _currentNode.value = updated
            _children.value = dao.getChildrenOnce(node.id)
        }
    }

    fun addContainerAtPhoto(
        name: String,
        points: List<NormalizedPoint>,
        photoIndex: Int
    ) {
        val parent = _currentNode.value ?: return
        val cx = points.map { it.x }.average().toFloat()
        val cy = points.map { it.y }.average().toFloat()
        viewModelScope.launch {
            dao.insert(
                StorageNode(
                    parentId = parent.id,
                    spaceId = parent.spaceId,          // ← НОВОЕ
                    name = name.trim(),
                    type = NodeType.CONTAINER,
                    x = cx,
                    y = cy,
                    polygonJson = PolygonCodec.encode(points),
                    photoIndex = photoIndex
                )
            )
            _children.value = dao.getChildrenOnce(parent.id)
        }
    }

    fun addChild(
        name: String,
        type: NodeType,
        x: Float,
        y: Float,
        markerSize: Float = 0.05f
    ) {
        val parent = _currentNode.value ?: return
        viewModelScope.launch {
            dao.insert(
                StorageNode(
                    parentId = parent.id,
                    spaceId = parent.spaceId,          // ← НОВОЕ
                    name = name.trim(),
                    type = type,
                    x = x,
                    y = y,
                    markerSize = markerSize
                )
            )
            _children.value = dao.getChildrenOnce(parent.id)
        }
    }

    fun deleteChild(node: StorageNode) {
        val parent = _currentNode.value ?: return
        viewModelScope.launch {
            dao.delete(node)
            _children.value = dao.getChildrenOnce(parent.id)
        }
    }

    fun deleteItems(ids: Set<Long>) {
        if (ids.isEmpty()) return
        val parent = _currentNode.value ?: return
        viewModelScope.launch {
            ids.forEach { id -> dao.deleteById(id) }
            _children.value = dao.getChildrenOnce(parent.id)
        }
    }

    fun moveItems(ids: Set<Long>, newParentId: Long) {
        if (ids.isEmpty()) return
        val parent = _currentNode.value ?: return
        viewModelScope.launch {
            val newParentSpace = dao.getById(newParentId)?.spaceId
            ids.forEach { id ->
                dao.getById(id)?.let { item ->
                    dao.update(item.copy(parentId = newParentId, spaceId = newParentSpace ?: item.spaceId))
                }
            }
            _children.value = dao.getChildrenOnce(parent.id)
        }
    }

    private suspend fun buildBreadcrumbs(node: StorageNode): List<StorageNode> {
        val path = mutableListOf<StorageNode>()
        var current: StorageNode? = node
        while (current != null) {
            path.add(0, current)
            current = current.parentId?.let { dao.getById(it) }
        }
        return path
    }

    fun addChildWithPolygon(
        name: String,
        type: NodeType,
        points: List<NormalizedPoint>
    ) {
        val parent = _currentNode.value ?: return
        viewModelScope.launch {
            val cx = points.map { it.x }.average().toFloat()
            val cy = points.map { it.y }.average().toFloat()
            dao.insert(
                StorageNode(
                    parentId = parent.id,
                    spaceId = parent.spaceId,          // ← НОВОЕ
                    name = name.trim(),
                    type = type,
                    x = cx,
                    y = cy,
                    polygonJson = PolygonCodec.encode(points)
                )
            )
            _children.value = dao.getChildrenOnce(parent.id)
        }
    }

    fun updatePolygon(nodeId: Long, points: List<NormalizedPoint>) {
        val parent = _currentNode.value ?: return
        viewModelScope.launch {
            dao.updatePolygon(nodeId, PolygonCodec.encode(points))
            _children.value = dao.getChildrenOnce(parent.id)
        }
    }

    fun addItemWithQuantity(
        name: String,
        quantity: Float,
        unit: String,
        note: String? = null
    ) {
        val parent = _currentNode.value ?: return
        viewModelScope.launch {
            dao.insert(
                StorageNode(
                    parentId = parent.id,
                    spaceId = parent.spaceId,          // ← НОВОЕ
                    name = name.trim(),
                    type = NodeType.ITEM,
                    x = 0f,
                    y = 0f,
                    quantity = quantity,
                    unit = unit.trim().ifBlank { "шт" },
                    note = note?.trim()?.ifBlank { null }
                )
            )
            _children.value = dao.getChildrenOnce(parent.id)
        }
    }

    fun updateItem(
        node: StorageNode,
        newName: String,
        newQty: Float,
        newUnit: String,
        newNote: String?,
        newCategory: String?,
        newPhotoPath: String?
    ) {
        viewModelScope.launch {
            dao.updateItemFull(
                id = node.id,
                name = newName.trim(),
                qty = newQty,
                unit = newUnit.trim().ifBlank { "шт" },
                note = newNote?.trim()?.ifBlank { null },
                category = newCategory?.trim()?.ifBlank { null },
                photoPath = newPhotoPath
            )
            ensureCategoriesInDb(listOf(newCategory))
            _children.value = dao.getChildrenOnce(node.parentId ?: return@launch)
        }
    }

    fun setNodePhoto(node: StorageNode, path: String) {
        viewModelScope.launch {
            dao.update(node.copy(photoPath = path))
            _currentNode.value = node.copy(photoPath = path)
        }
    }

    fun addItemFull(
        name: String,
        quantity: Float,
        unit: String,
        note: String?,
        category: String?,
        photoPath: String?
    ) {
        val parent = _currentNode.value ?: return
        viewModelScope.launch {
            dao.insert(
                StorageNode(
                    parentId = parent.id,
                    spaceId = parent.spaceId,          // ← НОВОЕ
                    name = name.trim(),
                    type = NodeType.ITEM,
                    x = 0f,
                    y = 0f,
                    quantity = quantity,
                    unit = unit.trim().ifBlank { "шт" },
                    note = note?.trim()?.ifBlank { null },
                    category = category?.trim()?.ifBlank { null },
                    photoPath = photoPath
                )
            )
            ensureCategoriesInDb(listOf(category))
            _children.value = dao.getChildrenOnce(parent.id)
        }
    }

    fun addCategory(name: String, iconName: String = "Category") {
        viewModelScope.launch {
            val clean = name.trim()
            if (clean.isBlank()) return@launch
            val existing = categoryDao.getByName(clean)
            if (existing == null) {
                categoryDao.insert(Category(name = clean, iconName = iconName))
            }
        }
    }

    private suspend fun ensureCategoriesInDb(categories: List<String?>) {
        val clean = categories
            .mapNotNull { it?.trim() }
            .filter { it.isNotBlank() }
            .distinct()
        if (clean.isEmpty()) return

        val existing = categoryDao.getAllCategoriesOnce().map { it.name }.toSet()
        clean.forEach { name ->
            if (name !in existing) {
                categoryDao.insert(Category(name = name, iconName = "Category"))
            }
        }
    }

    suspend fun loadChildrenOnce(parentId: Long): List<StorageNode> =
        dao.getChildrenOnce(parentId)

    suspend fun loadParentOnce(nodeId: Long): StorageNode? =
        dao.getById(nodeId)
}