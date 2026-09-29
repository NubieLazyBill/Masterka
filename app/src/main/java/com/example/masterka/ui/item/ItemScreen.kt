package com.example.masterka.ui.item

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.masterka.ui.common.PhotoSourceDialog
import com.example.masterka.ui.common.copyPhotoToInternal
import com.example.masterka.ui.common.createTempCameraFile
import com.example.masterka.ui.common.formatDate
import com.example.masterka.ui.common.formatQty
import com.example.masterka.ui.common.formatRelativeDate
import com.example.masterka.ui.common.getUriForFile
import com.example.masterka.ui.common.hasCameraPermission
import com.example.masterka.ui.common.isOverdue
import com.example.masterka.ui.common.oneMonthFromNow
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
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var fullScreenPhoto by remember { mutableStateOf(false) }

    // ==== Аренда ====
    var showLendDialog by remember { mutableStateOf(false) }

    LaunchedEffect(item, editMode) {
        if (editMode && item != null) {
            editName = item!!.name
            editQty = formatQty(item!!.quantity)
            editUnit = item!!.unit
            editCategory = item!!.category ?: ""
            editNote = item!!.note ?: ""
        }
    }

    var showPhotoSource by remember { mutableStateOf(false) }

    val pickGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val photoPath = copyPhotoToInternal(context, uri)
            vm.setPhoto(photoPath)
        }
    }

    var cameraFile by remember { mutableStateOf<File?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val file = cameraFile
        if (success && file != null && file.exists()) {
            vm.setPhoto(file.absolutePath)
        }
        cameraFile = null
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val file = createTempCameraFile(context)
            cameraFile = file
            val uri = getUriForFile(context, file)
            takePictureLauncher.launch(uri)
        }
    }

    fun launchCamera() {
        if (hasCameraPermission(context)) {
            val file = createTempCameraFile(context)
            cameraFile = file
            val uri = getUriForFile(context, file)
            takePictureLauncher.launch(uri)
        } else {
            permissionLauncher.launch(android.Manifest.permission.CAMERA)
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
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    if (!editMode) {
                        if (current.lentTo == null) {
                            IconButton(onClick = { showLendDialog = true }) {
                                Icon(Icons.Default.Send, contentDescription = "Отдать")
                            }
                        }
                        IconButton(onClick = { editMode = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Удалить",
                                tint = MaterialTheme.colorScheme.error
                            )
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
                        .clickable { fullScreenPhoto = true },
                    contentScale = ContentScale.FillWidth
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

            if (fullScreenPhoto && current.photoPath != null) {
                FullScreenPhotoDialog(
                    photoPath = current.photoPath!!,
                    onDismiss = { fullScreenPhoto = false }
                )
            }

            if (editMode) {
                OutlinedButton(
                    onClick = { showPhotoSource = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (current.photoPath == null) "Добавить фото" else "Сменить фото")
                }
            }

            // ==== Блок «Отдано» (в просмотре) ====
            if (!editMode && current.lentTo != null) {
                LendCard(
                    lentTo = current.lentTo!!,
                    lentAt = current.lentAt,
                    returnBy = current.returnBy,
                    onReturnClick = { vm.returnItem() }
                )
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

            // ==== Количество ====
            if (editMode) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("1", "5", "10", "100").forEach { preset ->
                            OutlinedButton(
                                onClick = { editQty = preset },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                            ) { Text(preset, style = MaterialTheme.typography.labelMedium) }
                        }
                        OutlinedButton(
                            onClick = { editQty = "много" },
                            modifier = Modifier.weight(1.2f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                        ) { Text("много", style = MaterialTheme.typography.labelMedium) }
                    }
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
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = editNote,
                        onValueChange = { editNote = it },
                        label = { Text("Заметка") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (current.noteUpdatedAt != null) {
                        Text(
                            "Изменено: ${formatRelativeDate(current.noteUpdatedAt)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (!current.note.isNullOrBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Заметка",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (current.noteUpdatedAt != null) {
                            Text(
                                formatRelativeDate(current.noteUpdatedAt),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
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

    // ==== Диалог удаления ====
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Удалить вещь?") },
            text = { Text("«${current.name}» будет удалена без возможности восстановления.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        vm.deleteItem { onBack() }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Отмена") }
            }
        )
    }

    // ==== Диалог источника фото ====
    if (showPhotoSource) {
        PhotoSourceDialog(
            onDismiss = { showPhotoSource = false },
            onCameraClick = {
                showPhotoSource = false
                launchCamera()
            },
            onGalleryClick = {
                showPhotoSource = false
                pickGalleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )
    }

    // ==== Диалог «Отдать» ====
    if (showLendDialog) {
        LendDialog(
            itemName = current.name,
            onDismiss = { showLendDialog = false },
            onConfirm = { lentTo, returnBy ->
                vm.lendItem(lentTo, returnBy)
                showLendDialog = false
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

@Composable
private fun LendCard(
    lentTo: String,
    lentAt: Long?,
    returnBy: Long?,
    onReturnClick: () -> Unit
) {
    val overdue = isOverdue(returnBy)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (overdue)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.tertiaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = null,
                    tint = if (overdue)
                        MaterialTheme.colorScheme.onErrorContainer
                    else
                        MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (overdue) "⚠️ Пора вернуть" else "Отдано",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (overdue)
                        MaterialTheme.colorScheme.onErrorContainer
                    else
                        MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Text(
                "Кому: $lentTo",
                style = MaterialTheme.typography.bodyMedium,
                color = if (overdue)
                    MaterialTheme.colorScheme.onErrorContainer
                else
                    MaterialTheme.colorScheme.onTertiaryContainer
            )

            if (lentAt != null) {
                Text(
                    "Отдано: ${formatDate(lentAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (overdue)
                        MaterialTheme.colorScheme.onErrorContainer
                    else
                        MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            if (returnBy != null) {
                Text(
                    "Вернуть до: ${formatDate(returnBy)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (overdue)
                        MaterialTheme.colorScheme.onErrorContainer
                    else
                        MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Button(
                onClick = onReturnClick,
                modifier = Modifier.fillMaxWidth(),
                colors = if (overdue)
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                else
                    ButtonDefaults.buttonColors()
            ) {
                Text("Вернули")
            }
        }
    }
}

@Composable
private fun LendDialog(
    itemName: String,
    onDismiss: () -> Unit,
    onConfirm: (lentTo: String, returnBy: Long?) -> Unit
) {
    var lentTo by remember { mutableStateOf("") }
    var withDeadline by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Отдать «$itemName»") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = lentTo,
                    onValueChange = { lentTo = it },
                    label = { Text("Кому *") },
                    placeholder = { Text("Имя, сосед, брат...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = withDeadline,
                        onCheckedChange = { withDeadline = it }
                    )
                    Text(
                        "Срок возврата — через месяц",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(
                    "Можно вернуть в любой момент через кнопку «Вернули».",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val deadline = if (withDeadline) oneMonthFromNow() else null
                    onConfirm(lentTo.trim(), deadline)
                },
                enabled = lentTo.isNotBlank()
            ) { Text("Отдать") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@Composable
private fun FullScreenPhotoDialog(
    photoPath: String,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AsyncImage(
                model = File(photoPath),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
                    .clickable(onClick = onDismiss),
                contentScale = ContentScale.Fit
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Закрыть",
                    tint = Color.White
                )
            }
        }
    }
}