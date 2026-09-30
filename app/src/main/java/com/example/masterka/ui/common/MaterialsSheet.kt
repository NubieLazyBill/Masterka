package com.example.masterka.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.ExperimentalFoundationApi
import com.example.masterka.data.StorageNode
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Share

enum class MaterialsViewMode {
    LIST,
    GROUPS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialsSheetContent(
    items: List<StorageNode>,
    onItemClick: (StorageNode) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    categoriesMap: Map<String, String> = emptyMap(),
    // ==== Мультивыбор ====
    selectionMode: Boolean = false,
    selectedIds: Set<Long> = emptySet(),
    onEnterSelection: (StorageNode) -> Unit = {},
    onToggleSelection: (StorageNode) -> Unit = {},
    onExitSelection: () -> Unit = {},
    onMoveSelected: () -> Unit = {},
    onDeleteSelected: () -> Unit = {}
) {
    var viewMode by remember { mutableStateOf(MaterialsViewMode.GROUPS) }

    Column(modifier = modifier.fillMaxSize()) {
        // ==== Заголовок ====
        if (selectionMode) {
            // Режим выделения
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onExitSelection) {
                    Icon(Icons.Default.Close, contentDescription = "Отмена")
                }
                Text(
                    "Выбрано: ${selectedIds.size}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        if (selectedIds.isNotEmpty()) onMoveSelected()
                    },
                    enabled = selectedIds.isNotEmpty()
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Переместить")
                }
                IconButton(
                    onClick = onDeleteSelected,
                    enabled = selectedIds.isNotEmpty()
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        } else {
            // Обычный режим
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
                IconButton(onClick = onAddClick) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить вещь")
                }
            }
        }

        HorizontalDivider()

        when (viewMode) {
            MaterialsViewMode.LIST -> {
                MaterialsList(
                    items = items,
                    categoriesMap = categoriesMap,
                    onItemClick = onItemClick,
                    selectionMode = selectionMode,
                    selectedIds = selectedIds,
                    onEnterSelection = onEnterSelection,
                    onToggleSelection = onToggleSelection
                )
            }
            MaterialsViewMode.GROUPS -> {
                MaterialsGrouped(
                    items = items,
                    categoriesMap = categoriesMap,
                    onItemClick = onItemClick,
                    selectionMode = selectionMode,
                    selectedIds = selectedIds,
                    onEnterSelection = onEnterSelection,
                    onToggleSelection = onToggleSelection
                )
            }
        }
    }
}

@Composable
private fun MaterialsList(
    items: List<StorageNode>,
    categoriesMap: Map<String, String>,
    onItemClick: (StorageNode) -> Unit,
    selectionMode: Boolean,
    selectedIds: Set<Long>,
    onEnterSelection: (StorageNode) -> Unit,
    onToggleSelection: (StorageNode) -> Unit
) {
    val sorted = items.sortedBy { it.name.lowercase() }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(sorted, key = { it.id }) { item ->
            MaterialRow(
                item = item,
                categoriesMap = categoriesMap,
                onItemClick = onItemClick,
                selectionMode = selectionMode,
                isSelected = item.id in selectedIds,
                onEnterSelection = onEnterSelection,
                onToggleSelection = onToggleSelection
            )
        }
    }
}

@Composable
private fun MaterialsGrouped(
    items: List<StorageNode>,
    categoriesMap: Map<String, String>,
    onItemClick: (StorageNode) -> Unit,
    selectionMode: Boolean,
    selectedIds: Set<Long>,
    onEnterSelection: (StorageNode) -> Unit,
    onToggleSelection: (StorageNode) -> Unit
) {
    val grouped = items.groupBy { it.category ?: "Без категории" }
    val groups = grouped.toSortedMap(compareBy { if (it == "Без категории") "яяя" else it.lowercase() })

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        groups.forEach { (category, itemsInCategory) ->
            item(key = "header_$category") {
                val iconName = itemsInCategory.firstOrNull()?.category?.let { categoriesMap[it] }
                CategoryHeader(
                    category = category,
                    count = itemsInCategory.size,
                    iconName = iconName
                )
            }
            items(
                itemsInCategory.sortedBy { it.name.lowercase() },
                key = { it.id }
            ) { item ->
                MaterialRow(
                    item = item,
                    categoriesMap = categoriesMap,
                    onItemClick = onItemClick,
                    selectionMode = selectionMode,
                    isSelected = item.id in selectedIds,
                    onEnterSelection = onEnterSelection,
                    onToggleSelection = onToggleSelection
                )
            }
        }
    }
}

@Composable
private fun CategoryHeader(
    category: String,
    count: Int,
    iconName: String?      // ← НОВОЕ
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ==== Иконка категории ====
            if (iconName != null) {
                Icon(
                    IconRegistry.get(iconName),
                    contentDescription = null,
                    tint = IconRegistry.getColor(iconName),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
            }

            Text(
                category.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                "$count",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MaterialRow(
    item: StorageNode,
    categoriesMap: Map<String, String>,
    onItemClick: (StorageNode) -> Unit,
    selectionMode: Boolean,
    isSelected: Boolean,
    onEnterSelection: (StorageNode) -> Unit,
    onToggleSelection: (StorageNode) -> Unit
) {
    val containerColor = if (isSelected)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surface

    Surface(
        color = containerColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {
                        if (selectionMode) onToggleSelection(item)
                        else onItemClick(item)
                    },
                    onLongClick = {
                        if (!selectionMode) onEnterSelection(item)
                    }
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ==== Чекбокс в режиме выделения ====
            if (selectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelection(item) }
                )
                Spacer(Modifier.width(4.dp))
            }

            // ==== Миниатюра вещи ====
            Box(
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!item.photoPath.isNullOrBlank()) {
                    AsyncImage(
                        model = java.io.File(item.photoPath!!),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))

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
                // ==== Бейдж «Отдано» ====
                if (item.lentTo != null) {
                    val overdue = isOverdue(item.returnBy)
                    Surface(
                        color = if (overdue)
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.tertiaryContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            if (overdue) "⚠️ Пора вернуть: ${item.lentTo}"
                            else "📤 Отдано: ${item.lentTo}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (overdue)
                                MaterialTheme.colorScheme.onErrorContainer
                            else
                                MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
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
                // ==== Аренда ====
                if (item.lentTo != null) {
                    val overdue = isOverdue(item.returnBy)
                    Text(
                        if (overdue) "⚠️ Пора вернуть: ${item.lentTo}"
                        else "📤 Отдано: ${item.lentTo}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (overdue)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.tertiary
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