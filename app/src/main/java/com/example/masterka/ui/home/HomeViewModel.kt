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

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = (app as MasterkaApp).dao

    // Список корневых зон (стены, шкафы верхнего уровня)
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
}