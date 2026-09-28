package com.example.masterka.ui.item

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.masterka.ui.common.copyPhotoToInternal
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemScreen(
    itemId: Long,
    onBack: () -> Unit,
    vm: ItemViewModel = viewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(itemId) { vm.loadItem(itemId) }

    val item by vm.item.collectAsState()
    val path by vm.path.collectAsState()

    var editMode by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editQty by remember { mutableStateOf("1") }
    var editUnit by remember { mutableStateOf("шт") }
    var editCategory by remember { mutableStateOf("") }
    var editNote by remember { mutableStateOf("") }

    val categories by vm.allCategories.collectAsState()
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var showNewCategoryDialog by remember { mutableStateOf(false) }

    // Инициализация полей при входе в режим редактирования
    LaunchedEffect(item, editMode) {
        if (editMode && item != null) {
            editName = item!!.name
            editQty = formatQty(item!!.quantity)
            editUnit = item!!.unit
            editCategory = item!!.category ?: ""
            editNote = item!!.note ?: ""
        }
    }

    val pickPhoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val photoPath = copyPhotoToInternal(context, uri)
            vm.setPhoto(photoPath)
        }
    }

    val current = item ?: run {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(current.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (path.isNotBlank()) {
                            Text(
                                path,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (editMode) editMode = false else onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (!editMode) {
                        IconButton(onClick = { editMode = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (editMode) {
                ExtendedFloatingActionButton(
                    onClick = {
                        vm.save(
                            name = editName,
                            qty = editQty.replace(',', '.').toFloatOrNull() ?: current.quantity,
                            unit = editUnit,
                            category = editCategory,
                            note = editNote
                        )
                        editMode = false
                    },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    text = { Text("Сохранить") }
                )
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ==== Фото ====
            if (current.photoPath != null) {
                AsyncImage(
                    model = File(current.photoPath),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Фото нет",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (editMode) {
                OutlinedButton(
                    onClick = {
                        pickPhoto.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (current.photoPath == null) "Добавить фото" else "Сменить фото")
                }
            }

            // ==== Название ====
            if (editMode) {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Название") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                InfoRow("Название", current.name)
            }

            // ==== Количество и единица ====
            if (editMode) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editQty,
                        onValueChange = { editQty = it },
                        label = { Text("Кол-во") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = editUnit,
                        onValueChange = { editUnit = it },
                        label = { Text("Ед. изм.") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                InfoRow("Количество", "${formatQty(current.quantity)} ${current.unit}")
            }

            // ==== Категория ====
            if (editMode) {
                Box(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = editCategory,
                        onValueChange = { editCategory = it },
                        label = { Text("Категория") },
                        placeholder = { Text("инструмент / расходник / метизы") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { categoryMenuExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Выбрать")
                            }
                        }
                    )
                    // Оверлей на поле, чтобы клик по нему открывал меню
                    Box(
                        Modifier
                            .matchParentSize()
                            .clickable { categoryMenuExpanded = true }
                    )
                    DropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false }
                    ) {
                        if (categories.isEmpty()) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Пока нет категорий",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                onClick = { },
                                enabled = false
                            )
                        } else {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        editCategory = cat
                                        categoryMenuExpanded = false
                                    }
                                )
                            }
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Добавить новую")
                                }
                            },
                            onClick = {
                                categoryMenuExpanded = false
                                showNewCategoryDialog = true
                            }
                        )
                    }
                }
            } else if (!current.category.isNullOrBlank()) {
                InfoRow("Категория", current.category!!)
            }

            // ==== Заметка ====
            if (editMode) {
                OutlinedTextField(
                    value = editNote,
                    onValueChange = { editNote = it },
                    label = { Text("Заметка") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (!current.note.isNullOrBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Заметка",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        current.note!!,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    // ==== Диалог новой категории ====
    if (showNewCategoryDialog) {
        var newCategory by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewCategoryDialog = false },
            title = { Text("Новая категория") },
            text = {
                OutlinedTextField(
                    value = newCategory,
                    onValueChange = { newCategory = it },
                    label = { Text("Название") },
                    placeholder = { Text("например, крепёж") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val v = newCategory.trim()
                        if (v.isNotBlank()) {
                            editCategory = v
                        }
                        showNewCategoryDialog = false
                    },
                    enabled = newCategory.isNotBlank()
                ) { Text("Добавить") }
            },
            dismissButton = {
                TextButton(onClick = { showNewCategoryDialog = false }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatQty(q: Float): String {
    return if (q == q.toInt().toFloat()) q.toInt().toString()
    else "%.2f".format(q).trimEnd('0').trimEnd('.')
}