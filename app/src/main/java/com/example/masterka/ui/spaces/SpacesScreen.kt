package com.example.masterka.ui.spaces

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.masterka.data.Space

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpacesScreen(
    onBack: () -> Unit,
    vm: SpaceViewModel = viewModel()
) {
    val spaces by vm.spaces.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingSpace by remember { mutableStateOf<Space?>(null) }
    var deletingSpace by remember { mutableStateOf<Space?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Помещения") },
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
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить помещение")
            }
        }
    ) { padding ->
        if (spaces.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Пока нет помещений.\nДобавь первое кнопкой +",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(spaces, key = { it.id }) { space ->
                    SpaceRow(
                        space = space,
                        onEditClick = { editingSpace = space },
                        onDeleteClick = { deletingSpace = space }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        SpaceEditDialog(
            title = "Новое помещение",
            initialName = "",
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                vm.addSpace(name)
                showAddDialog = false
            }
        )
    }

    val edit = editingSpace
    if (edit != null) {
        SpaceEditDialog(
            title = "Редактировать",
            initialName = edit.name,
            onDismiss = { editingSpace = null },
            onConfirm = { name ->
                vm.renameSpace(edit, name, edit.iconName)
                editingSpace = null
            }
        )
    }

    val del = deletingSpace
    if (del != null) {
        AlertDialog(
            onDismissRequest = { deletingSpace = null },
            title = { Text("Удалить помещение?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("«${del.name}» и всё содержимое будут удалены.")
                    Text(
                        "⚠️ Все зоны, контейнеры и вещи внутри этого помещения исчезнут. Отменить будет нельзя.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deleteSpace(del)
                        deletingSpace = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { deletingSpace = null }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun SpaceRow(
    space: Space,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onEditClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = CircleShape,
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Warehouse,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Text(
            space.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onEditClick) {
            Icon(Icons.Default.Edit, contentDescription = "Редактировать")
        }
        IconButton(onClick = onDeleteClick) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Удалить",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
    HorizontalDivider()
}

@Composable
private fun SpaceEditDialog(
    title: String,
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название") },
                placeholder = { Text("Мастерская, Гараж, Кладовка…") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank()
            ) { Text("Сохранить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}