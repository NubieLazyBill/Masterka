package com.example.masterka.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.masterka.data.StorageNode
import androidx.compose.foundation.ExperimentalFoundationApi

enum class MaterialsViewMode {
    LIST,
    GROUPS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialsSheetContent(
    items: List<StorageNode>,
    onItemClick: (StorageNode) -> Unit,
    onItemLongClick: (StorageNode) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(MaterialsViewMode.GROUPS) }

    Column(modifier = modifier.fillMaxSize()) {
        // ==== Заголовок ====
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Материалы (${items.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            // Переключатель вида
            IconButton(
                onClick = {
                    viewMode = if (viewMode == MaterialsViewMode.LIST)
                        MaterialsViewMode.GROUPS
                    else MaterialsViewMode.LIST
                }
            ) {
                Icon(
                    if (viewMode == MaterialsViewMode.LIST) Icons.Default.ViewAgenda
                    else Icons.Default.ViewList,
                    contentDescription = "Переключить вид"
                )
            }
            // Добавить вещь
            IconButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Добавить вещь")
            }
        }

        HorizontalDivider()

        // ==== Список ====
        when (viewMode) {
            MaterialsViewMode.LIST -> {
                MaterialsList(
                    items = items,
                    onItemClick = onItemClick,
                    onItemLongClick = onItemLongClick
                )
            }
            MaterialsViewMode.GROUPS -> {
                MaterialsGrouped(
                    items = items,
                    onItemClick = onItemClick,
                    onItemLongClick = onItemLongClick
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaterialsList(
    items: List<StorageNode>,
    onItemClick: (StorageNode) -> Unit,
    onItemLongClick: (StorageNode) -> Unit
) {
    val sorted = items.sortedBy { it.name.lowercase() }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(sorted, key = { it.id }) { item ->
            MaterialRow(
                item = item,
                onItemClick = onItemClick,
                onItemLongClick = onItemLongClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaterialsGrouped(
    items: List<StorageNode>,
    onItemClick: (StorageNode) -> Unit,
    onItemLongClick: (StorageNode) -> Unit
) {
    // Группируем по категории
    val grouped = items.groupBy { it.category ?: "Без категории" }
    // Сортируем группы по имени, «Без категории» — в конец
    val groups = grouped.toSortedMap(compareBy { if (it == "Без категории") "яяя" else it.lowercase() })

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        groups.forEach { (category, itemsInCategory) ->
            item(key = "header_$category") {
                CategoryHeader(category = category, count = itemsInCategory.size)
            }
            items(
                itemsInCategory.sortedBy { it.name.lowercase() },
                key = { it.id }
            ) { item ->
                MaterialRow(
                    item = item,
                    onItemClick = onItemClick,
                    onItemLongClick = onItemLongClick
                )
            }
        }
    }
}

@Composable
private fun CategoryHeader(category: String, count: Int) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                category.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                "$count",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun MaterialRow(
    item: StorageNode,
    onItemClick: (StorageNode) -> Unit,
    onItemLongClick: (StorageNode) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onItemClick(item) },
                onLongClick = { onItemLongClick(item) }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                item.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!item.note.isNullOrBlank()) {
                Text(
                    item.note!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Text(
            "${formatQty(item.quantity)} ${item.unit}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
    HorizontalDivider()
}

/**
 * Компактная панель снизу — «Материалы (N)»
 */
@Composable
fun MaterialsStrip(
    count: Int,
    onExpandClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
        onClick = onExpandClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "📦  Материалы ($count)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                "Показать ↑",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Контекстное меню вещи (долгий тап)
 */
@Composable
fun MaterialContextMenu(
    item: StorageNode,
    onDismiss: () -> Unit,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "${formatQty(item.quantity)} ${item.unit}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (!item.category.isNullOrBlank()) {
                    Text(
                        "Категория: ${item.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onOpen) { Text("Открыть") }
        },
        dismissButton = {
            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) { Text("Удалить") }
        }
    )
}