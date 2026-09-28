package com.example.masterka.ui.node

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.masterka.MasterkaApp
import com.example.masterka.data.NodeType
import com.example.masterka.data.NormalizedPoint
import com.example.masterka.data.PolygonCodec
import com.example.masterka.data.StorageNode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NodeViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = (app as MasterkaApp).dao

    private val _currentNode = MutableStateFlow<StorageNode?>(null)
    val currentNode = _currentNode.asStateFlow()

    private val _children = MutableStateFlow<List<StorageNode>>(emptyList())
    val children = _children.asStateFlow()

    private val _breadcrumbs = MutableStateFlow<List<StorageNode>>(emptyList())
    val breadcrumbs = _breadcrumbs.asStateFlow()

    fun loadNode(nodeId: Long) {
        viewModelScope.launch {
            val node = dao.getById(nodeId) ?: return@launch
            _currentNode.value = node
            _breadcrumbs.value = buildBreadcrumbs(node)
            _children.value = dao.getChildrenOnce(nodeId)
        }
    }

    fun setPhoto(path: String) {
        val node = _currentNode.value ?: return
        viewModelScope.launch {
            val updated = node.copy(photoPath = path)
            dao.update(updated)
            _currentNode.value = updated
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
        points: List<com.example.masterka.data.NormalizedPoint>
    ) {
        val parent = _currentNode.value ?: return
        viewModelScope.launch {
            // Центр полигона — как x,y (для совместимости и для случаев без фото)
            val cx = points.map { it.x }.average().toFloat()
            val cy = points.map { it.y }.average().toFloat()

            dao.insert(
                StorageNode(
                    parentId = parent.id,
                    name = name.trim(),
                    type = type,
                    x = cx,
                    y = cy,
                    polygonJson = com.example.masterka.data.PolygonCodec.encode(points)
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
}