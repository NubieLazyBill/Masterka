package com.example.masterka.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoveDown
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.masterka.data.NodeType
import com.example.masterka.data.StorageNode

/**
 * Полноэкранный оверлей выбора узла для перемещения вещей.
 *
 * Открывается от корня иерархии, позволяет заходить в зоны и контейнеры,
 * внизу — FAB «Переместить сюда».
 *
 * @param rootNode корень иерархии (обычно «Мастерская»)
 * @param loadChildren suspend-функция загрузки детей узла
 * @param onConfirm финальное действие: targetParentId
 * @param onCancel отмена всей операции
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovePickerScreen(
    rootNode: StorageNode,
    loadChildren: suspend (Long) -> List<StorageNode>,
    onConfirm: (Long) -> Unit,
    onCancel: () -> Unit
) {
    // Стек узлов — от корня к текущему. Позволяет «назад».
    val navStack = remember { mutableStateListOf<StorageNode>(rootNode) }
    val current = navStack.last()

    var children by remember { mutableStateOf<List<StorageNode>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(current.id) {
        loading = true
        children = loadChildren(current.id)
            .filter { it.type != NodeType.ITEM } // вещи не могут быть контейнерами
        loading = false
    }

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                current.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (navStack.size > 1) {
                                Text(
                                    navStack.joinToString(" → ") { it.name },
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (navStack.size > 1) {
                                    navStack.removeAt(navStack.lastIndex)
                                } else {
                                    onCancel()
                                }
                            }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Назад"
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { onConfirm(current.id) },
                    icon = {
                        Icon(Icons.Default.MoveDown, contentDescription = null)
                    },
                    text = { Text("Переместить сюда") }
                )
            }
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Подсказка
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Куда положить? Заходи в папки, потом нажми «Переместить сюда».",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                when {
                    loading -> {
                        Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    children.isEmpty() -> {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Warehouse,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "Здесь пусто.\nМожно переместить прямо сюда.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 96.dp)
                        ) {
                            items(children, key = { it.id }) { child ->
                                NodePickerRow(
                                    node = child,
                                    onClick = { navStack.add(child) }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NodePickerRow(
    node: StorageNode,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (node.type) {
                NodeType.ZONE -> Icons.Default.Warehouse
                NodeType.CONTAINER -> Icons.Default.Inventory2
                NodeType.ITEM -> Icons.Default.Inventory2
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                node.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                when (node.type) {
                    NodeType.ZONE -> "Зона"
                    NodeType.CONTAINER -> "Контейнер"
                    NodeType.ITEM -> "Вещь"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Войти",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}