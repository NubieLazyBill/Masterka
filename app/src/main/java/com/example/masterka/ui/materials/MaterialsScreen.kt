package com.example.masterka.ui.materials

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.masterka.data.StorageNode
import com.example.masterka.ui.common.IconRegistry
import com.example.masterka.ui.common.formatQty
import com.example.masterka.ui.common.isOverdue
import java.io.File

enum class MaterialsViewMode {
    LIST,
    GROUPS
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MaterialsScreen(
    onBack: () -> Unit,
    onItemClick: (StorageNode) -> Unit,       // ← открыть вещь
    onLocationClick: (StorageNode) -> Unit,   // ← открыть родителя
    vm: MaterialsViewModel = viewModel()
) {
    val query by vm.query.collectAsState()
    val sort by vm.sort.collectAsState()
    val rowsAll by vm.rows.collectAsState()

    val rows = remember(query, sort, rowsAll) { vm.filteredAndSorted() }

    var viewMode by remember { mutableStateOf(MaterialsViewMode.GROUPS) }
    var contextItem by remember { mutableStateOf<StorageNode?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Моё добро") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewMode = if (viewMode == MaterialsViewMode.LIST)
                            MaterialsViewMode.GROUPS
                        else MaterialsViewMode.LIST
                    }) {
                        Icon(
                            if (viewMode == MaterialsViewMode.LIST) Icons.Default.ViewAgenda
                            else Icons.Default.ViewList,
                            contentDescription = "Переключить вид"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ==== Поиск ====
            OutlinedTextField(
                value = query,
                onValueChange = vm::setQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                placeholder = { Text("Поиск...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { vm.setQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Очистить")
                        }
                    }
                },
                singleLine = true
            )

            // ==== Чипсы категорий ====
            val allCategories = remember(rowsAll) {
                rowsAll.mapNotNull { it.item.category }
                    .distinct()
                    .sorted()
            }
            if (allCategories.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = query.isEmpty(),
                            onClick = { vm.setQuery("") },
                            label = { Text("Все") }
                        )
                    }
                    items(allCategories) { cat ->
                        FilterChip(
                            selected = query == cat,
                            onClick = { vm.setQuery(cat) },
                            label = { Text(cat) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ==== Сводка ====
            Text(
                "Всего: ${rows.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Spacer(Modifier.height(4.dp))

            // ==== Список ====
            if (rows.isEmpty()) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Inventory2,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (query.isBlank())
                                "Пока ничего нет.\nДобавь вещи через экран зоны."
                            else
                                "Ничего не найдено",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                when (viewMode) {
                    MaterialsViewMode.LIST -> {
                        MaterialsListContent(
                            rows = rows,
                            onItemClick = onItemClick,           // ← НОВОЕ
                            onLocationClick = onLocationClick,
                            onItemLongClick = { contextItem = it.item }
                        )
                    }
                    MaterialsViewMode.GROUPS -> {
                        MaterialsGroupedContent(
                            rows = rows,
                            onItemClick = onItemClick,           // ← НОВОЕ
                            onLocationClick = onLocationClick,
                            onItemLongClick = { contextItem = it.item }
                        )
                    }
                }
            }
        }
    }

    // ==== Контекстное меню ====
    val ctxItem = contextItem
    if (ctxItem != null) {
        AlertDialog(
            onDismissRequest = { contextItem = null },
            title = { Text(ctxItem.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "${formatQty(ctxItem.quantity)} ${ctxItem.unit}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (!ctxItem.category.isNullOrBlank()) {
                        Text(
                            "Категория: ${ctxItem.category}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val item = ctxItem
                    contextItem = null
                    onLocationClick(item)
                }) { Text("Открыть") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        vm.deleteItem(ctxItem)
                        contextItem = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Удалить") }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MaterialsListContent(
    rows: List<MaterialRow>,
    onItemClick: (StorageNode) -> Unit,
    onLocationClick: (StorageNode) -> Unit,
    onItemLongClick: (MaterialRow) -> Unit
) {
    val sorted = rows.sortedBy { it.item.name.lowercase() }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(sorted, key = { it.item.id }) { row ->
            MaterialRowItem(
                row = row,
                onItemClick = onItemClick,
                onLocationClick = onLocationClick,
                onItemLongClick = onItemLongClick
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MaterialsGroupedContent(
    rows: List<MaterialRow>,
    onItemClick: (StorageNode) -> Unit,
    onLocationClick: (StorageNode) -> Unit,
    onItemLongClick: (MaterialRow) -> Unit
) {
    val grouped = rows.groupBy { it.item.category ?: "Без категории" }
    val groups = grouped.toSortedMap(
        compareBy { if (it == "Без категории") "яяя" else it.lowercase() }
    )

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        groups.forEach { (category, itemsInCategory) ->
            item(key = "header_$category") {
                val iconName = itemsInCategory.firstOrNull()?.categoryIcon
                CategoryHeader(
                    category = category,
                    count = itemsInCategory.size,
                    iconName = iconName
                )
            }
            items(
                itemsInCategory.sortedBy { it.item.name.lowercase() },
                key = { it.item.id }
            ) { row ->
                MaterialRowItem(
                    row = row,
                    onItemClick = onItemClick,
                    onLocationClick = onLocationClick,
                    onItemLongClick = onItemLongClick
                )
            }
        }
    }
}

@Composable
private fun CategoryHeader(
    category: String,
    count: Int,
    iconName: String?
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
private fun MaterialRowItem(
    row: MaterialRow,
    onItemClick: (StorageNode) -> Unit,      // ← открыть вещь
    onLocationClick: (StorageNode) -> Unit,  // ← открыть родителя
    onItemLongClick: (MaterialRow) -> Unit
) {
    val item = row.item

    Row(
        Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onLocationClick(item) },   // ← тап по строке → к родителю
                onLongClick = { onItemLongClick(row) }
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ==== Миниатюра (тап → открыть вещь) ====
        Box(
            Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onItemClick(item) },   // ← тап по фото → к вещи
            contentAlignment = Alignment.Center
        ) {
            if (!item.photoPath.isNullOrBlank()) {
                AsyncImage(
                    model = File(item.photoPath!!),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        // ==== Имя, путь, заметка ====
        Column(Modifier.weight(1f)) {
            Text(
                item.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (row.location.isNotBlank() && row.location != "—") {
                Text(
                    row.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (!item.note.isNullOrBlank()) {
                Text(
                    item.note!!,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Бейдж «Отдано»
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

        Spacer(Modifier.width(8.dp))

        // ==== Количество ====
        Column(horizontalAlignment = Alignment.End) {
            Text(
                formatQty(item.quantity),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                item.unit,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    HorizontalDivider()
}