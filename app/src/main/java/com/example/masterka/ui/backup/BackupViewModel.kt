package com.example.masterka.ui.backup

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.masterka.data.BackupManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BackupState {
    data object Idle : BackupState()
    data object Working : BackupState()
    data class Success(val message: String) : BackupState()
    data class Error(val message: String) : BackupState()
}

class BackupViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow<BackupState>(BackupState.Idle)
    val state = _state.asStateFlow()

    fun export(uri: Uri) {
        viewModelScope.launch {
            _state.value = BackupState.Working
            val result = BackupManager.export(getApplication(), uri)
            _state.value = result.fold(
                onSuccess = { BackupState.Success("Бэкап сохранён") },
                onFailure = { BackupState.Error("Ошибка экспорта: ${it.message}") }
            )
        }
    }

    fun import(uri: Uri) {
        viewModelScope.launch {
            _state.value = BackupState.Working
            val result = BackupManager.import(getApplication(), uri)
            _state.value = result.fold(
                onSuccess = { BackupState.Success("Данные восстановлены") },
                onFailure = { BackupState.Error("Ошибка импорта: ${it.message}") }
            )
        }
    }

    fun resetState() {
        _state.value = BackupState.Idle
    }
}