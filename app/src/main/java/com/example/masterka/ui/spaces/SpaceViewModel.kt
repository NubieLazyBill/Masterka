package com.example.masterka.ui.spaces

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.masterka.MasterkaApp
import com.example.masterka.data.Space
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SpaceViewModel(app: Application) : AndroidViewModel(app) {

    private val spaceDao = (app as MasterkaApp).spaceDao
    private val dao = (app as MasterkaApp).dao

    val spaces = spaceDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSpace(name: String, iconName: String = "Warehouse") {
        viewModelScope.launch {
            val clean = name.trim()
            if (clean.isBlank()) return@launch
            spaceDao.insert(Space(name = clean, iconName = iconName))
        }
    }

    fun renameSpace(space: Space, newName: String, newIcon: String) {
        viewModelScope.launch {
            val clean = newName.trim()
            if (clean.isBlank()) return@launch
            spaceDao.update(space.copy(name = clean, iconName = newIcon))
        }
    }

    /**
     * Удаление помещения.
     * ⚠️ Все корневые зоны этого помещения (и всё вложенное) удалятся через CASCADE-подобную логику.
     * Мы это делаем явно: сначала удаляем все корневые узлы, потом — само помещение.
     */
    fun deleteSpace(space: Space) {
        viewModelScope.launch {
            val all = spaceDao.getAllOnce()
            if (all.size <= 1) return@launch   // нельзя удалить последнее
            dao.deleteAllNodesInSpace(space.id)
            spaceDao.delete(space)
        }
    }
}