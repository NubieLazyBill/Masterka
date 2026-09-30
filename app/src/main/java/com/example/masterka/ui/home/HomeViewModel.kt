package com.example.masterka.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.masterka.MasterkaApp
import com.example.masterka.data.NodeType
import com.example.masterka.data.NormalizedPoint
import com.example.masterka.data.PhotoPathsCodec
import com.example.masterka.data.PolygonCodec
import com.example.masterka.data.StorageNode
import com.example.masterka.data.allPhotoPaths
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.masterka.data.Category

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = (app as MasterkaApp).dao
    private val categoryDao = (app as MasterkaApp).categoryDao

    private val _rootNode = MutableStateFlow<StorageNode?>(null)
    val rootNode = _rootNode.asStateFlow()

    private val _children = MutableStateFlow<List<StorageNode>>(emptyList())
    val children = _children.asStateFlow()

    val allCategories = dao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categoriesMap: StateFlow<Map<String, String>> = categoryDao.getAllCategories()
        .map { list -> list.associate { it.name to it.iconName } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    init {
        viewModelScope.launch {
            var root = dao.getRootNode()
            if (root == null) {
                val id = dao.insert(
                    StorageNode(
                        parentId = null,
                        name = "Мастерская",
                        type = NodeType.ZONE
                    )
                )
                root = dao.getById(id)
            }
            _rootNode.value = root
            if (root != null) {
                _children.value = dao.getChildrenOnce(root.id)
            }
        }
    }

    fun addPhoto(path: String) {
        val node = _rootNode.value ?: return
        viewModelScope.launch {
            val current = node.allPhotoPaths().toMutableList()
            current.add(path)
            val updated = node.copy(photoPathsJson = PhotoPathsCodec.encode(current))
            dao.update(updated)
            _rootNode.value = updated
        }
    }

    fun removePhotoAt(index: Int) {
        val node = _rootNode.value ?: return
        viewModelScope.launch {
            val current = node.allPhotoPaths().toMutableList()
            if (index !in current.indices) return@launch
            current.removeAt(index)
            dao.deleteChildrenAtPhoto(node.id, index)
            val updated = node.copy(photoPathsJson = PhotoPathsCodec.encode(current))
            dao.update(updated)
            _rootNode.value = updated
            _children.value = dao.getChildrenOnce(node.id)
        }
    }

    fun addContainerAtPhoto(
        name: String,
        points: List<NormalizedPoint>,
        photoIndex: Int
    ) {
        val parent = _rootNode.value ?: return
        val cx = points.map { it.x }.average().toFloat()
        val cy = points.map { it.y }.average().toFloat()
        viewModelScope.launch {
            dao.insert(
                StorageNode(
                    parentId = parent.id,
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

    fun updatePolygon(nodeId: Long, points: List<NormalizedPoint>) {
        val parent = _rootNode.value ?: return
        viewModelScope.launch {
            dao.updatePolygon(nodeId, PolygonCodec.encode(points))
            _children.value = dao.getChildrenOnce(parent.id)
        }
    }

    fun deleteNode(node: StorageNode) {
        val parent = _rootNode.value ?: return
        viewModelScope.launch {
            dao.delete(node)
            _children.value = dao.getChildrenOnce(parent.id)
        }
    }

    // ==== Мультивыбор (Шаг 2) ====
    fun deleteItems(ids: Set<Long>) {
        if (ids.isEmpty()) return
        val parent = _rootNode.value ?: return
        viewModelScope.launch {
            ids.forEach { id -> dao.deleteById(id) }
            _children.value = dao.getChildrenOnce(parent.id)
        }
    }

    fun moveItems(ids: Set<Long>, newParentId: Long) {
        if (ids.isEmpty()) return
        val parent = _rootNode.value ?: return
        viewModelScope.launch {
            ids.forEach { id ->
                dao.getById(id)?.let { item ->
                    dao.update(item.copy(parentId = newParentId))
                }
            }
            _children.value = dao.getChildrenOnce(parent.id)
        }
    }

    fun addCategory(name: String, iconName: String = "Category") {
        viewModelScope.launch {
            val clean = name.trim()
            if (clean.isBlank()) return@launch
            val existing = categoryDao.getByName(clean)
            if (existing == null) {
                categoryDao.insert(
                    Category(
                        name = clean,
                        iconName = iconName
                    )
                )
            }
        }
    }

    suspend fun loadChildrenOnce(parentId: Long): List<StorageNode> =
        dao.getChildrenOnce(parentId)

    suspend fun loadParentOnce(nodeId: Long): StorageNode? =
        dao.getById(nodeId)
}