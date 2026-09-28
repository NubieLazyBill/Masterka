package com.example.masterka.ui.materials

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.masterka.data.StorageNode
import androidx.compose.material3.HorizontalDivider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialsScreen(
    onBack: () -> Unit,
    onLocationClick: (StorageNode) -> Unit,
    vm: MaterialsViewModel = viewModel()
) {
    val query by vm.query.collectAsState()
    val sort by vm.sort.collectAsState()
    val rowsAll by vm.rows.collectAsState()

    // Пересчёт при изменении любого из трёх
    val rows = remember(query, sort, rowsAll) { vm.filteredAndSorted() }

    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Все материалы") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Сортировка")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortItem("Имя: А→Я", MaterialsSort.NAME_ASC, sort) {
                                vm.setSort(it); showSortMenu = false
                            }
                            SortItem("Имя: Я→А", MaterialsSort.NAME_DESC, sort) {
                                vm.setSort(it); showSortMenu = false
                            }
                            SortItem("Кол-во: ↑", MaterialsSort.QTY_ASC, sort) {
                                vm.setSort(it); showSortMenu = false
                            }
                            SortItem("Кол-во: ↓", MaterialsSort.QTY_DESC, sort) {
                                vm.setSort(it); showSortMenu = false
                            }
                            SortItem("По месту", MaterialsSort.LOCATION, sort) {
                                vm.setSort(it); showSortMenu = false
                            }
                        }
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
            // Поиск
            OutlinedTextField(
                value = query,
                onValueChange = vm::setQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                placeholder = { Text("Поиск по названию, заметке, месту...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { vm.setQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Очистить")
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { /* live-search */ })
            )

            // Счётчик
            Text(
                "${rows.size} позиций",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Заголовок таблицы
            HeaderRow()

            // Строки
            if (rows.isEmpty()) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (query.isBlank())
                            "Пока ничего нет.\nДобавь вещи через экран зоны."
                        else
                            "Ничего не найдено",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(rows, key = { it.item.id }) { row ->
                        MaterialItemRow(
                            row = row,
                            onLocationClick = {
                                // Открыть контейнер, где лежит вещь — это родитель вещи
                                // Если родитель есть, открываем его. Передадим сам item вверх,
                                // а родителя найдём на стороне навигации.
                                onLocationClick(row.item)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SortItem(
    label: String,
    value: MaterialsSort,
    current: MaterialsSort,
    onSelect: (MaterialsSort) -> Unit
) {
    DropdownMenuItem(
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = value == current, onClick = { onSelect(value) })
                Spacer(Modifier.width(4.dp))
                Text(label)
            }
        },
        onClick = { onSelect(value) }
    )
}

@Composable
private fun HeaderRow() {
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Название",
            modifier = Modifier.weight(1.6f),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            "Кол-во",
            modifier = Modifier.weight(0.7f),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            "Ед.",
            modifier = Modifier.weight(0.5f),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            "Где лежит",
            modifier = Modifier.weight(1.6f),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun MaterialItemRow(
    row: MaterialRow,
    onLocationClick: () -> Unit
) {
    val item = row.item
    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { onLocationClick() }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                item.name,
                modifier = Modifier.weight(1.6f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                formatQty(item.quantity),
                modifier = Modifier.weight(0.7f),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                item.unit,
                modifier = Modifier.weight(0.5f),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                row.location,
                modifier = Modifier.weight(1.6f),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (!item.note.isNullOrBlank()) {
            Text(
                item.note,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 6.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        HorizontalDivider()
    }
}

private fun formatQty(q: Float): String {
    return if (q == q.toInt().toFloat()) q.toInt().toString()
    else "%.2f".format(q).trimEnd('0').trimEnd('.')
}