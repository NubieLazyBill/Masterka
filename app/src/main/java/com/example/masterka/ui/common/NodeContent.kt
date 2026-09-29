package com.example.masterka.ui.common

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.masterka.data.AppTheme
import com.example.masterka.data.NodeType
import com.example.masterka.data.NormalizedPoint
import com.example.masterka.data.PolygonCodec
import com.example.masterka.data.StorageNode
import com.example.masterka.data.allPhotoPaths
import java.io.File
import androidx.compose.material.icons.filled.CloudUpload

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodeContent(
    node: StorageNode,
    children: List<StorageNode>,
    developerMode: Boolean,
    allCategories: List<String> = emptyList(),
    onThemeChange: (AppTheme) -> Unit = {},
    currentTheme: AppTheme = AppTheme.WORKSHOP,
    onDeveloperModeChange: (Boolean) -> Unit,
    onBackupClick: () -> Unit = {},
    onAddPhoto: (String) -> Unit,
    onRemovePhoto: (Int) -> Unit,
    onAddContainerAtPhoto: (String, List<NormalizedPoint>, Int) -> Unit,
    onUpdatePolygon: (Long, List<NormalizedPoint>) -> Unit,
    onDeleteNode: (StorageNode) -> Unit,
    onChildClick: (StorageNode) -> Unit,
    onItemClick: (StorageNode) -> Unit,
    showMaterialsButton: Boolean = false,
    onMaterialsClick: () -> Unit = {},
    showAddItem: Boolean = true,
    onAddItemWithCategory: (
        name: String,
        qty: Float,
        unit: String,
        note: String?,
        category: String?,
        photoPath: String?
    ) -> Unit = { _, _, _, _, _, _ -> }
) {
    val context = LocalContext.current

    val photos = node.allPhotoPaths()
    val itemsOnly = children.filter { it.type == NodeType.ITEM }

    var currentZoom by remember { mutableStateOf(1f) }
    var currentPage by rememberSaveable { mutableStateOf(0) }

    val pagerState = rememberPagerState(
        initialPage = currentPage,
        pageCount = { photos.size.coerceAtLeast(1) }
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { currentPage = it }
    }

    LaunchedEffect(currentPage) {
        currentZoom = 1f
    }

    // ==== Галерея для фото узла ====
    val pickPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val path = copyPhotoToInternal(context, uri)
            onAddPhoto(path)
        }
    }

    // ==== Камера для фото узла — правильный порядок ====
    var mainCameraFile by remember { mutableStateOf<File?>(null) }

    val mainTakePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val file = mainCameraFile
        if (success && file != null && file.exists()) {
            onAddPhoto(file.absolutePath)
        }
        mainCameraFile = null
    }

    val mainPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val file = createTempCameraFile(context)
            mainCameraFile = file
            val uri = getUriForFile(context, file)
            mainTakePictureLauncher.launch(uri)
        }
    }

    fun launchMainCamera() {
        if (hasCameraPermission(context)) {
            val file = createTempCameraFile(context)
            mainCameraFile = file
            val uri = getUriForFile(context, file)
            mainTakePictureLauncher.launch(uri)
        } else {
            mainPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    var showMainPhotoSource by remember { mutableStateOf(false) }
    var selectedMarker by remember { mutableStateOf<StorageNode?>(null) }

    // ==== Режим рисования ====
    var drawingMode by remember { mutableStateOf(false) }
    var drawingShape by remember { mutableStateOf(DrawingShape.POLYGON) }
    var showShapePickerDialog by remember { mutableStateOf(false) }
    var showDeletePhotoConfirm by remember { mutableStateOf(false) }
    val drawingPoints = remember { mutableStateListOf<NormalizedPoint>() }
    var showSavePolygonDialog by remember { mutableStateOf(false) }

    var editingPolygonId by remember { mutableStateOf<Long?>(null) }
    var editingPolygonName by remember { mutableStateOf<String?>(null) }

    var showAddItemDialog by remember { mutableStateOf(false) }
    var showMaterialsSheet by remember { mutableStateOf(false) }
    var sheetContextItem by remember { mutableStateOf<StorageNode?>(null) }
    var showThemeDialog by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        if (photos.isNotEmpty()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = !drawingMode && currentZoom <= 1.01f
            ) { page ->
                val photoPath = photos[page]
                val pageMarkers = children.filter {
                    it.type != NodeType.ITEM && it.photoIndex == page
                }

                PhotoWithMarkers(
                    photoPath = photoPath,
                    markers = pageMarkers,
                    developerMode = developerMode,
                    drawingMode = drawingMode && currentPage == page,
                    drawingShape = drawingShape,
                    drawingPoints = if (currentPage == page) drawingPoints.toList() else emptyList(),
                    onAddDrawingPoint = { p ->
                        if (currentPage == page) drawingPoints.add(p)
                    },
                    onSetDrawingPoints = { list ->
                        if (currentPage == page) {
                            drawingPoints.clear()
                            drawingPoints.addAll(list)
                        }
                    },
                    hiddenMarkerId = editingPolygonId,
                    onMarkerClick = { marker ->
                        if (developerMode) selectedMarker = marker else onChildClick(marker)
                    },
                    onTapEmpty = { selectedMarker = null },
                    onZoomChanged = { zoom ->
                        if (currentPage == page) currentZoom = zoom
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Фото не выбрано", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { showMainPhotoSource = true }) {
                        Icon(Icons.Default.Image, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Выбрать фото")
                    }
                }
            }
        }

        if (photos.size > 1 && !drawingMode) {
            Row(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                photos.indices.forEach { i ->
                    val active = i == currentPage
                    Box(
                        Modifier
                            .size(if (active) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (active) Color.White else Color.White.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showMaterialsButton) {
                FilledTonalButton(onClick = onMaterialsClick) { Text("Моё добро") }
            } else {
                Spacer(Modifier.width(0.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (developerMode) {
                    IconButton(onClick = { showMainPhotoSource = true }) {
                        Icon(Icons.Default.Image, contentDescription = "Добавить фото")
                    }
                    if (photos.size > 1) {
                        IconButton(onClick = { showDeletePhotoConfirm = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Удалить фото",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    IconButton(onClick = { showThemeDialog = true }) {
                        Icon(Icons.Default.Palette, contentDescription = "Тема")
                    }
                    IconButton(onClick = onBackupClick) {
                        Icon(Icons.Default.CloudUpload, contentDescription = "Бэкап")
                    }
                }
                FilterChip(
                    selected = developerMode,
                    onClick = { onDeveloperModeChange(!developerMode) },
                    label = { Text("Dev") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Build,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }

        if (developerMode && !drawingMode && selectedMarker == null) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(top = 60.dp)
            ) {
                Text(
                    if (photos.isEmpty()) "Dev: добавь фото кнопкой сверху справа"
                    else "Dev: тапни по фото для пометки контейнера, или нажми «Нарисовать»",
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        if (drawingMode) {
            Surface(
                color = if (editingPolygonId != null)
                    MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(top = 60.dp)
            ) {
                Text(
                    text = when {
                        editingPolygonId != null && drawingShape == DrawingShape.RECTANGLE ->
                            "Перерисовка «${editingPolygonName ?: "?"}»: тяни пальцем прямоугольник"
                        editingPolygonId != null ->
                            "Перерисовка «${editingPolygonName ?: "?"}»: тапай по углам. Минимум 3 точки."
                        drawingShape == DrawingShape.RECTANGLE ->
                            "Прямоугольник: тяни пальцем от угла до угла"
                        else ->
                            "Полигон: тапай по углам контейнера. Минимум 3 точки."
                    },
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        val hasBottomStrip = itemsOnly.isNotEmpty() && !drawingMode
        val bottomPadding = if (hasBottomStrip) 84.dp else 16.dp

        Column(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = bottomPadding),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (developerMode && photos.isNotEmpty() && !drawingMode) {
                ExtendedFloatingActionButton(
                    onClick = {
                        editingPolygonId = null
                        editingPolygonName = null
                        drawingPoints.clear()
                        showShapePickerDialog = true
                    },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    text = { Text("Нарисовать") }
                )
            }
            if (showAddItem && !drawingMode) {
                ExtendedFloatingActionButton(
                    onClick = { showAddItemDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Вещь") }
                )
            }
        }

        if (itemsOnly.isNotEmpty() && !drawingMode) {
            MaterialsStrip(
                count = itemsOnly.size,
                onExpandClick = { showMaterialsSheet = true },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        if (drawingMode) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (drawingShape == DrawingShape.RECTANGLE) "Тяни пальцем по фото"
                        else "Точек: ${drawingPoints.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = {
                            drawingPoints.clear()
                            drawingMode = false
                            editingPolygonId = null
                            editingPolygonName = null
                        }) { Text("Отмена") }
                        if (drawingShape == DrawingShape.POLYGON) {
                            TextButton(
                                onClick = {
                                    if (drawingPoints.isNotEmpty()) {
                                        drawingPoints.removeAt(drawingPoints.lastIndex)
                                    }
                                },
                                enabled = drawingPoints.isNotEmpty()
                            ) { Text("Убрать") }
                        }
                        Button(
                            onClick = { showSavePolygonDialog = true },
                            enabled = when (drawingShape) {
                                DrawingShape.POLYGON -> drawingPoints.size >= 3
                                DrawingShape.RECTANGLE -> drawingPoints.size == 4
                            }
                        ) {
                            Text(if (editingPolygonId != null) "Обновить" else "Сохранить")
                        }
                    }
                }
            }
        }
    }

    // ==== Диалог выбора формы ====
    if (showShapePickerDialog) {
        AlertDialog(
            onDismissRequest = {
                showShapePickerDialog = false
                editingPolygonId = null
                editingPolygonName = null
                drawingPoints.clear()
            },
            title = { Text("Как нарисовать?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            drawingShape = DrawingShape.POLYGON
                            drawingPoints.clear()
                            drawingMode = true
                            showShapePickerDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Полигон — тапай по углам") }
                    OutlinedButton(
                        onClick = {
                            drawingShape = DrawingShape.RECTANGLE
                            drawingPoints.clear()
                            drawingMode = true
                            showShapePickerDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Прямоугольник — тяни пальцем") }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = {
                    showShapePickerDialog = false
                    editingPolygonId = null
                    editingPolygonName = null
                    drawingPoints.clear()
                }) { Text("Отмена") }
            }
        )
    }

    // ==== Плашка маркера ====
    val marker = selectedMarker
    if (marker != null && !drawingMode && developerMode) {
        AlertDialog(
            onDismissRequest = { selectedMarker = null },
            title = { Text(marker.name) },
            text = { Text("Тип: контейнер", style = MaterialTheme.typography.bodySmall) },
            confirmButton = {
                TextButton(onClick = {
                    selectedMarker = null
                    onChildClick(marker)
                }) { Text("Открыть") }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (marker.polygonJson != null) {
                        TextButton(onClick = {
                            editingPolygonId = marker.id
                            editingPolygonName = marker.name
                            drawingPoints.clear()
                            selectedMarker = null
                            showShapePickerDialog = true
                        }) { Text("Контур") }
                    }
                    TextButton(onClick = {
                        onDeleteNode(marker)
                        selectedMarker = null
                    }) { Text("Удалить") }
                }
            }
        )
    }

    if (showAddItemDialog) {
        AddItemDialog(
            existingCategories = allCategories,
            onDismiss = { showAddItemDialog = false },
            onConfirm = { name, qty, unit, note, category, photoPath ->
                onAddItemWithCategory(name, qty, unit, note, category, photoPath)
                showAddItemDialog = false
            }
        )
    }

    // ==== Диалог сохранения контейнера ====
    if (showSavePolygonDialog) {
        val isEditing = editingPolygonId != null
        var name by remember { mutableStateOf(editingPolygonName ?: "") }
        AlertDialog(
            onDismissRequest = { showSavePolygonDialog = false },
            title = { Text(if (isEditing) "Обновить контейнер" else "Новый контейнер") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Название") },
                        singleLine = true,
                        enabled = !isEditing
                    )
                    Text(
                        text = if (drawingShape == DrawingShape.RECTANGLE) "Форма: прямоугольник"
                        else "Точек: ${drawingPoints.size}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val canSave = name.isNotBlank() && when (drawingShape) {
                            DrawingShape.POLYGON -> drawingPoints.size >= 3
                            DrawingShape.RECTANGLE -> drawingPoints.size == 4
                        }
                        if (canSave) {
                            if (isEditing) {
                                onUpdatePolygon(editingPolygonId!!, drawingPoints.toList())
                            } else {
                                onAddContainerAtPhoto(name, drawingPoints.toList(), currentPage)
                            }
                            drawingPoints.clear()
                            drawingMode = false
                            editingPolygonId = null
                            editingPolygonName = null
                            showSavePolygonDialog = false
                        }
                    },
                    enabled = name.isNotBlank()
                ) { Text(if (isEditing) "Обновить" else "Сохранить") }
            },
            dismissButton = {
                TextButton(onClick = { showSavePolygonDialog = false }) { Text("Отмена") }
            }
        )
    }

    // ==== Диалог удаления зоны/контейнера ====
    if (showDeletePhotoConfirm) {
        // Определяем тип текущего узла для текста
        val typeName = when (node.type) {
            NodeType.ZONE -> "зону"
            NodeType.CONTAINER -> "контейнер"
            NodeType.ITEM -> "вещь"
        }
        val typeNameTitle = when (node.type) {
            NodeType.ZONE -> "зону"
            NodeType.CONTAINER -> "контейнер"
            NodeType.ITEM -> "вещь"
        }

        AlertDialog(
            onDismissRequest = { showDeletePhotoConfirm = false },
            title = { Text("Удалить $typeNameTitle?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "«${node.name}» будет удалён${if (node.type == NodeType.ZONE) "а" else ""} вместе с фото и всем содержимым.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "⚠️ Все вложенные контейнеры и вещи тоже удалятся. Отменить это будет невозможно.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemovePhoto(currentPage)
                        showDeletePhotoConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeletePhotoConfirm = false }) { Text("Отмена") }
            }
        )
    }

    // ==== BottomSheet ====
    if (showMaterialsSheet && itemsOnly.isNotEmpty()) {
        ModalBottomSheet(
            onDismissRequest = { showMaterialsSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        ) {
            MaterialsSheetContent(
                items = itemsOnly,
                onItemClick = { item ->
                    showMaterialsSheet = false
                    onItemClick(item)
                },
                onItemLongClick = { item -> sheetContextItem = item },
                onAddClick = {
                    showMaterialsSheet = false
                    showAddItemDialog = true
                },
                modifier = Modifier.fillMaxHeight(0.7f)
            )
        }
    }

    val ctxItem = sheetContextItem
    if (ctxItem != null) {
        MaterialContextMenu(
            item = ctxItem,
            onDismiss = { sheetContextItem = null },
            onOpen = {
                sheetContextItem = null
                showMaterialsSheet = false
                onItemClick(ctxItem)
            },
            onDelete = {
                sheetContextItem = null
                onDeleteNode(ctxItem)
            }
        )
    }

    // ==== Диалог темы ====
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Тема оформления") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeOption("Мастерская", "Янтарь + кожа. Тёплая, уютная.",
                        currentTheme == AppTheme.WORKSHOP,
                        { onThemeChange(AppTheme.WORKSHOP); showThemeDialog = false })
                    ThemeOption("Современная", "Индиго + бирюза. Чистая, минималистичная.",
                        currentTheme == AppTheme.MODERN,
                        { onThemeChange(AppTheme.MODERN); showThemeDialog = false })
                    ThemeOption("Индустриальная", "Серый + оранжевый. Техничная.",
                        currentTheme == AppTheme.INDUSTRIAL,
                        { onThemeChange(AppTheme.INDUSTRIAL); showThemeDialog = false })
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Отмена") }
            }
        )
    }

    // ==== Диалог источника фото для узла ====
    if (showMainPhotoSource) {
        PhotoSourceDialog(
            onDismiss = { showMainPhotoSource = false },
            onCameraClick = {
                showMainPhotoSource = false
                launchMainCamera()
            },
            onGalleryClick = {
                showMainPhotoSource = false
                pickPhotoLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )
    }
}

@Composable
private fun PhotoWithMarkers(
    photoPath: String,
    markers: List<StorageNode>,
    developerMode: Boolean,
    drawingMode: Boolean,
    drawingShape: DrawingShape,
    drawingPoints: List<NormalizedPoint>,
    onAddDrawingPoint: (NormalizedPoint) -> Unit,
    onSetDrawingPoints: (List<NormalizedPoint>) -> Unit,
    hiddenMarkerId: Long?,
    onMarkerClick: (StorageNode) -> Unit,
    onTapEmpty: () -> Unit,
    onZoomChanged: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(scale) { onZoomChanged(scale) }

    fun screenToNormalized(tap: Offset): Offset {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val realX = (tap.x - cx - offset.x) / scale + cx
        val realY = (tap.y - cy - offset.y) / scale + cy
        return Offset(
            (realX / size.width).coerceIn(0f, 1f),
            (realY / size.height).coerceIn(0f, 1f)
        )
    }

    val gestureModifier = when {
        drawingMode && drawingShape == DrawingShape.POLYGON ->
            modifier.pointerInput(size.width, size.height) {
                detectTapGestures(
                    onTap = { tapOffset ->
                        if (size.width == 0 || size.height == 0) return@detectTapGestures
                        val n = screenToNormalized(tapOffset)
                        onAddDrawingPoint(NormalizedPoint(n.x, n.y))
                    }
                )
            }
        drawingMode && drawingShape == DrawingShape.RECTANGLE ->
            modifier.pointerInput(size.width, size.height) {
                var startPoint: Offset? = null
                detectDragGestures(
                    onDragStart = { pos ->
                        if (size.width == 0 || size.height == 0) return@detectDragGestures
                        startPoint = screenToNormalized(pos)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val start = startPoint ?: return@detectDragGestures
                        val current = screenToNormalized(change.position)
                        val left = minOf(start.x, current.x)
                        val right = maxOf(start.x, current.x)
                        val top = minOf(start.y, current.y)
                        val bottom = maxOf(start.y, current.y)
                        val rect = listOf(
                            NormalizedPoint(left, top),
                            NormalizedPoint(right, top),
                            NormalizedPoint(right, bottom),
                            NormalizedPoint(left, bottom)
                        )
                        onSetDrawingPoints(rect)
                    },
                    onDragEnd = { startPoint = null },
                    onDragCancel = { startPoint = null }
                )
            }
        else -> modifier.pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                var isGestureHandled = false
                while (true) {
                    val event = awaitPointerEvent()
                    val pressedChanges = event.changes.filter { it.pressed }
                    if (pressedChanges.isEmpty()) break
                    val pointerCount = pressedChanges.size
                    val zoomChange = event.calculateZoom()
                    val panChange = event.calculatePan()
                    if (pointerCount >= 2) {
                        if (zoomChange != 1f || panChange != Offset.Zero) {
                            scale = (scale * zoomChange).coerceIn(1f, 5f)
                            offset += panChange
                            val maxX = (size.width * (scale - 1f)) / 2f
                            val maxY = (size.height * (scale - 1f)) / 2f
                            offset = Offset(offset.x.coerceIn(-maxX, maxX), offset.y.coerceIn(-maxY, maxY))
                            event.changes.forEach { it.consume() }
                            isGestureHandled = true
                        }
                    } else if (pointerCount == 1 && scale > 1.01f) {
                        if (panChange != Offset.Zero) {
                            offset += panChange
                            val maxX = (size.width * (scale - 1f)) / 2f
                            val maxY = (size.height * (scale - 1f)) / 2f
                            offset = Offset(offset.x.coerceIn(-maxX, maxX), offset.y.coerceIn(-maxY, maxY))
                            event.changes.forEach { it.consume() }
                            isGestureHandled = true
                        }
                    }
                }
                if (!isGestureHandled && scale <= 1.01f) onTapEmpty()
            }
        }
    }

    Box(modifier = gestureModifier) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer(scaleX = scale, scaleY = scale,
                    translationX = offset.x, translationY = offset.y)
                .onSizeChanged { size = it }
        ) {
            AsyncImage(
                model = File(photoPath),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            if (size.width > 0) {
                markers.forEach { marker ->
                    if (hiddenMarkerId != null && marker.id == hiddenMarkerId) return@forEach
                    val points = PolygonCodec.decode(marker.polygonJson)
                    if (points.size >= 3) {
                        PolygonShape(
                            points = points, size = size,
                            color = markerColor(marker.type).copy(alpha = if (developerMode) 0.18f else 0.1f),
                            strokeColor = markerColor(marker.type).copy(alpha = if (developerMode) 0.85f else 0.55f)
                        )
                    }
                }
            }

            if (drawingMode && drawingPoints.isNotEmpty() && size.width > 0) {
                PolygonShape(
                    points = drawingPoints, size = size,
                    color = Color(0x552196F3), strokeColor = Color(0xFF2196F3),
                    dashed = drawingShape == DrawingShape.POLYGON,
                    showVertices = drawingShape == DrawingShape.POLYGON
                )
            }

            if (!drawingMode && size.width > 0) {
                val density = LocalDensity.current
                markers.forEach { marker ->
                    if (hiddenMarkerId != null && marker.id == hiddenMarkerId) return@forEach
                    val pts = PolygonCodec.decode(marker.polygonJson)
                    if (pts.size >= 3) {
                        val pathPoints = pts.map { Offset(it.x * size.width, it.y * size.height) }
                        val minX = pathPoints.minOf { it.x }
                        val minY = pathPoints.minOf { it.y }
                        val maxX = pathPoints.maxOf { it.x }
                        val maxY = pathPoints.maxOf { it.y }
                        val bboxW = with(density) { (maxX - minX).toDp() }
                        val bboxH = with(density) { (maxY - minY).toDp() }
                        val offX = with(density) { minX.toDp() }
                        val offY = with(density) { minY.toDp() }

                        Box(
                            modifier = Modifier
                                .offset(x = offX, y = offY)
                                .size(width = bboxW, height = bboxH)
                                .pointerInput(marker.id, size.width, size.height) {
                                    detectTapGestures(
                                        onTap = { local ->
                                            val abs = Offset(minX + local.x, minY + local.y)
                                            if (isPointInPolygon(abs, pathPoints)) onMarkerClick(marker)
                                        }
                                    )
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PolygonShape(
    points: List<NormalizedPoint>,
    size: IntSize,
    color: Color,
    strokeColor: Color,
    dashed: Boolean = false,
    showVertices: Boolean = false
) {
    if (points.isEmpty() || size.width == 0) return
    val density = LocalDensity.current
    val pathPoints = points.map { Offset(it.x * size.width, it.y * size.height) }

    Canvas(modifier = Modifier.fillMaxSize()) {
        if (pathPoints.size >= 2) {
            val path = Path().apply {
                moveTo(pathPoints.first().x, pathPoints.first().y)
                for (i in 1 until pathPoints.size) lineTo(pathPoints[i].x, pathPoints[i].y)
                if (!dashed) close()
            }
            if (!dashed) drawPath(path, color = color)
            drawPath(
                path = path, color = strokeColor,
                style = Stroke(
                    width = with(density) { 1.5.dp.toPx() },
                    pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(20f, 15f), 0f) else null
                )
            )
            if (dashed && pathPoints.size >= 2) {
                drawLine(
                    color = strokeColor, start = pathPoints.last(), end = pathPoints.first(),
                    strokeWidth = with(density) { 2.dp.toPx() },
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f), 0f)
                )
            }
        }
        if (showVertices) {
            pathPoints.forEach { pt ->
                drawCircle(color = strokeColor, radius = with(density) { 6.dp.toPx() }, center = pt)
                drawCircle(color = Color.White, radius = with(density) { 3.dp.toPx() }, center = pt)
            }
        }
    }
}

private fun isPointInPolygon(point: Offset, polygon: List<Offset>): Boolean {
    if (polygon.size < 3) return false
    var inside = false
    var j = polygon.size - 1
    for (i in polygon.indices) {
        val pi = polygon[i]
        val pj = polygon[j]
        if (((pi.y > point.y) != (pj.y > point.y)) &&
            (point.x < (pj.x - pi.x) * (point.y - pi.y) / (pj.y - pi.y) + pi.x)) inside = !inside
        j = i
    }
    return inside
}

private fun markerColor(type: NodeType): Color = when (type) {
    NodeType.ZONE -> Color(0xFF2196F3)
    NodeType.CONTAINER -> Color(0xFFFFC107)
    NodeType.ITEM -> Color(0xFF4CAF50)
}

@Composable
private fun AddItemDialog(
    existingCategories: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, quantity: Float, unit: String, note: String?,
                category: String?, photoPath: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("шт") }
    var note by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var showNewCategoryDialog by remember { mutableStateOf(false) }

    val quantity = parseQty(quantityText)

    var photoPath by remember { mutableStateOf<String?>(null) }
    var showPhotoSource by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val pickGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) photoPath = copyPhotoToInternal(context, uri)
    }

    var cameraFile by remember { mutableStateOf<File?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val file = cameraFile
        if (success && file != null && file.exists()) photoPath = file.absolutePath
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая вещь") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Название *") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (photoPath != null) {
                    AsyncImage(
                        model = File(photoPath!!),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(160.dp).clip(MaterialTheme.shapes.medium),
                        contentScale = ContentScale.Crop
                    )
                }
                OutlinedButton(
                    onClick = { showPhotoSource = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (photoPath == null) "Добавить фото" else "Сменить фото")
                }

                Box(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = category, onValueChange = { category = it },
                        label = { Text("Категория") },
                        placeholder = { Text("инструмент / расходник / метизы") },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { categoryMenuExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Выбрать")
                            }
                        }
                    )
                    Box(Modifier.matchParentSize().clickable { categoryMenuExpanded = true })
                    DropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false }
                    ) {
                        if (existingCategories.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Пока нет категорий", style = MaterialTheme.typography.bodySmall) },
                                onClick = { }, enabled = false
                            )
                        } else {
                            existingCategories.forEach { cat ->
                                DropdownMenuItem(text = { Text(cat) }, onClick = {
                                    category = cat; categoryMenuExpanded = false
                                })
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
                            onClick = { categoryMenuExpanded = false; showNewCategoryDialog = true }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantityText, onValueChange = { quantityText = it },
                        label = { Text("Кол-во") }, singleLine = true,
                        modifier = Modifier.weight(1f),
                        isError = quantity == null && quantityText.isNotBlank()
                    )
                    OutlinedTextField(
                        value = unit, onValueChange = { unit = it },
                        label = { Text("Ед. изм.") }, singleLine = true,
                        modifier = Modifier.weight(1f), placeholder = { Text("шт") }
                    )
                }

                Text("Быстрый выбор:", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("1", "5", "10", "100").forEach { preset ->
                        OutlinedButton(
                            onClick = { quantityText = preset },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                        ) { Text(preset, style = MaterialTheme.typography.labelMedium) }
                    }
                    OutlinedButton(
                        onClick = { quantityText = "много" },
                        modifier = Modifier.weight(1.2f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                    ) { Text("много", style = MaterialTheme.typography.labelMedium) }
                }

                OutlinedTextField(
                    value = note, onValueChange = { note = it },
                    label = { Text("Заметка (необязательно)") },
                    singleLine = false, minLines = 2, modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && quantity != null) {
                        onConfirm(name, quantity, unit.ifBlank { "шт" },
                            note.ifBlank { null }, category.ifBlank { null }, photoPath)
                    }
                },
                enabled = name.isNotBlank() && quantity != null
            ) { Text("Добавить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )

    if (showNewCategoryDialog) {
        var newCategory by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewCategoryDialog = false },
            title = { Text("Новая категория") },
            text = {
                OutlinedTextField(
                    value = newCategory, onValueChange = { newCategory = it },
                    label = { Text("Название") }, placeholder = { Text("например, крепёж") },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val v = newCategory.trim()
                    if (v.isNotBlank()) category = v
                    showNewCategoryDialog = false
                }, enabled = newCategory.isNotBlank()) { Text("Добавить") }
            },
            dismissButton = {
                TextButton(onClick = { showNewCategoryDialog = false }) { Text("Отмена") }
            }
        )
    }

    if (showPhotoSource) {
        PhotoSourceDialog(
            onDismiss = { showPhotoSource = false },
            onCameraClick = { showPhotoSource = false; launchCamera() },
            onGalleryClick = {
                showPhotoSource = false
                pickGalleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )
    }
}

@Composable
private fun ThemeOption(
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleSmall)
                Text(description, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (selected) {
                Icon(Icons.Default.Check, contentDescription = "Выбрано",
                    tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}