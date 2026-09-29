package com.example.masterka.ui.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.masterka.data.BackupManager
import kotlinx.coroutines.delay
import androidx.compose.material3.SnackbarDuration
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onBack: () -> Unit,
    vm: BackupViewModel = viewModel()
) {
    val state by vm.state.collectAsState()

    // Экспорт — создать документ
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) vm.export(uri)
    }

    // Импорт — открыть документ
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) vm.import(uri)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state) {
        when (val s = state) {
            is BackupState.Success -> {
                snackbarHostState.showSnackbar(
                    message = "Данные восстановлены. Приложение сейчас закроется — откройте его заново.",
                    duration = SnackbarDuration.Long
                )
                kotlinx.coroutines.delay(2500)
                android.os.Process.killProcess(android.os.Process.myPid())
                kotlin.system.exitProcess(0)
            }
            is BackupState.Error -> {
                snackbarHostState.showSnackbar(s.message)
                vm.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Резервная копия") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "Резервная копия всей мастерской — БД и все фотографии — в один zip-файл. Сохраните его в облако или на компьютер.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(8.dp))

            // ЭКСПОРТ
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Экспорт", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Сохранить всю мастерскую в файл",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = {
                            exportLauncher.launch(BackupManager.suggestFileName())
                        },
                        enabled = state !is BackupState.Working,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Создать бэкап")
                    }
                }
            }

            // ИМПОРТ
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Импорт", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "⚠️ Восстановление из бэкапа УДАЛИТ все текущие данные и заменит их содержимым архива.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Button(
                        onClick = {
                            importLauncher.launch(
                                arrayOf("application/zip", "application/octet-stream")
                            )
                        },
                        enabled = state !is BackupState.Working,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Восстановить из файла")
                    }
                }
            }

            if (state is BackupState.Working) {
                Box(
                    Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Обработка...")
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Text(
                "После импорта приложение автоматически покажет актуальные данные.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}