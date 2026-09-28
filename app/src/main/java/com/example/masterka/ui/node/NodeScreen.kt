package com.example.masterka.ui.node

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.masterka.data.NodeType
import com.example.masterka.data.NormalizedPoint
import com.example.masterka.data.PolygonCodec
import com.example.masterka.data.StorageNode
import com.example.masterka.ui.common.copyPhotoToInternal
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodeScreen(
    nodeId: Long,
    onBack: () -> Unit,
    onChildClick: (StorageNode) -> Unit,
    vm: NodeViewModel = viewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(nodeId) { vm.loadNode(nodeId) }

    val node by vm.currentNode.collectAsState()
    val children by vm.children.collectAsState()
    val breadcrumbs by vm.breadcrumbs.collectAsState()

    var developerMode by remember { mutableStateOf(false) }
    var showAddItemDialog by remember { mutableStateOf(false) }

    // ==== Выбранный маркер (для плашки) ====
    var selectedMarker by remember { mutableStateOf<StorageNode?>(null) }

    // ==== Состояние рисования ====
    var drawingMode by remember { mutableStateOf(false) }
    val drawingPoints = remember { mutableStateListOf<NormalizedPoint>() }
    var showSavePolygonDialog by remember { mutableStateOf(false) }

    // ==== Какой узел перерисовываем (null = создаём новый) ====
    var editingPolygonId by remember { mutableStateOf<Long?>(null) }
    var editingPolygonName by remember { mutableStateOf<String?>(null) }

    val pickPhoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val path = copyPhotoToInternal(context, uri)
            vm.setPhoto(path)
        }
    }

    val current = node ?: run {
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
                        if (breadcrumbs.size > 1) {
                            Text(
                                breadcrumbs.joinToString(" → ") { it.name },
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconToggleButton(
                        checked = developerMode,
                        onCheckedChange = { developerMode = it }
                    ) {
                        Icon(
                            Icons.Default.Build,
                            contentDescription = "Режим разработчика",
                            tint = if (developerMode) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Кнопка "Нарисовать контур" — только dev-mode, только когда есть фото
                if (developerMode && current.photoPath != null && !drawingMode) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            editingPolygonId = null
                            editingPolygonName = null
                            drawingPoints.clear()
                            drawingMode = true
                        },
                        icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        text = { Text("Нарисовать контур") }
                    )
                }
                // Кнопка "Добавить вещь" — видна вне режима рисования
                if (!drawingMode) {
                    ExtendedFloatingActionButton(
                        onClick = { showAddItemDialog = true },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Вещь") }
                    )
                }
                // Кнопка "Выбрать фото" — если фото нет
                if (current.photoPath == null) {
                    FloatingActionButton(onClick = {
                        pickPhoto.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }) {
                        Icon(Icons.Default.Image, contentDescription = "Выбрать фото")
                    }
                }
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // === Фото занимает ВСЁ пространство и не зависит от панелей ===
            if (current.photoPath != null) {
                PhotoWithMarkers(
                    photoPath = current.photoPath!!,
                    markers = children,
                    developerMode = developerMode,
                    drawingMode = drawingMode,
                    drawingPoints = drawingPoints.toList(),
                    hiddenMarkerId = editingPolygonId,
                    onMarkerClick = { marker -> selectedMarker = marker },
                    onDrawingTap = { x, y ->
                        drawingPoints.add(NormalizedPoint(x, y))
                    },
                    onTapEmpty = { selectedMarker = null },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Нет фото. Нажми кнопку ниже, чтобы выбрать.")
                }
            }

            // === Панель сверху — overlay ===
            Column(
                Modifier
                    .fillMaxSize()
                    .align(Alignment.TopCenter),
                verticalArrangement = Arrangement.Top
            ) {
                if (developerMode && !drawingMode && selectedMarker == null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Режим разработчика: нажми «Нарисовать контур» и обведи шкаф/полку по точкам.",
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
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (editingPolygonId != null)
                                "Перерисовка «${editingPolygonName ?: "?"}»: старый контур скрыт. Обведи заново (мин. 3 точки)."
                            else
                                "Режим рисования: тапай по фото — ставь точки. Минимум 3 точки.",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            // === Список вещей (без фото) — overlay внизу ===
            if (children.isNotEmpty() && current.photoPath == null) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(children, key = { it.id }) { child ->
                        AssistChip(
                            onClick = { onChildClick(child) },
                            label = { Text(child.name) }
                        )
                    }
                }
            }

            // === Панель управления рисованием — overlay внизу ===
            if (drawingMode) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Точек: ${drawingPoints.size}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = {
                                drawingPoints.clear()
                                drawingMode = false
                                editingPolygonId = null
                                editingPolygonName = null
                            }) { Text("Отмена") }
                            TextButton(
                                onClick = {
                                    if (drawingPoints.isNotEmpty()) {
                                        drawingPoints.removeAt(drawingPoints.lastIndex)
                                    }
                                },
                                enabled = drawingPoints.isNotEmpty()
                            ) { Text("Убрать точку") }
                            Button(
                                onClick = { showSavePolygonDialog = true },
                                enabled = drawingPoints.size >= 3
                            ) {
                                Text(if (editingPolygonId != null) "Обновить" else "Сохранить")
                            }
                        }
                    }
                }
            }
        }
    }

    // ==== Плашка выбранного маркера ====
    val marker = selectedMarker
    if (marker != null && !drawingMode) {
        AlertDialog(
            onDismissRequest = { selectedMarker = null },
            title = { Text(marker.name) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!marker.note.isNullOrBlank()) {
                        Text(
                            marker.note!!,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Text(
                        "Тип: ${
                            when (marker.type) {
                                NodeType.ZONE -> "Зона"
                                NodeType.CONTAINER -> "Контейнер"
                                NodeType.ITEM -> "Вещь"
                            }
                        }",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedMarker = null
                    onChildClick(marker)
                }) { Text("Открыть") }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = {
                        editingPolygonId = marker.id
                        editingPolygonName = marker.name
                        drawingPoints.clear()
                        drawingMode = true
                        selectedMarker = null
                    }) { Text("Изменить") }

                    TextButton(onClick = {
                        vm.deleteChild(marker)
                        selectedMarker = null
                    }) { Text("Удалить") }
                }
            }
        )
    }

    // Диалог вещи
    if (showAddItemDialog) {
        AddItemDialog(
            onDismiss = { showAddItemDialog = false },
            onConfirm = { name ->
                vm.addChild(name, NodeType.ITEM, 0f, 0f, 0.05f)
                showAddItemDialog = false
            }
        )
    }

    // Диалог сохранения/обновления полигона
    if (showSavePolygonDialog) {
        val isEditing = editingPolygonId != null
        var name by remember {
            mutableStateOf(editingPolygonName ?: "")
        }
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
                        "Точек: ${drawingPoints.size}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (name.isNotBlank() && drawingPoints.size >= 3) {
                            if (isEditing) {
                                vm.updatePolygon(editingPolygonId!!, drawingPoints.toList())
                            } else {
                                vm.addChildWithPolygon(
                                    name = name,
                                    type = NodeType.CONTAINER,
                                    points = drawingPoints.toList()
                                )
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
}

@Composable
private fun PhotoWithMarkers(
    photoPath: String,
    markers: List<StorageNode>,
    developerMode: Boolean,
    drawingMode: Boolean,
    drawingPoints: List<NormalizedPoint>,
    hiddenMarkerId: Long?,
    onMarkerClick: (StorageNode) -> Unit,
    onDrawingTap: (Float, Float) -> Unit,
    onTapEmpty: () -> Unit,
    modifier: Modifier = Modifier
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        if (drawingMode) return@rememberTransformableState
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        offset += offsetChange
        val maxX = (size.width * (scale - 1f)) / 2f
        val maxY = (size.height * (scale - 1f)) / 2f
        offset = Offset(
            offset.x.coerceIn(-maxX, maxX),
            offset.y.coerceIn(-maxY, maxY)
        )
    }

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

    Box(
        modifier = modifier.pointerInput(drawingMode, size.width, size.height) {
            detectTapGestures(
                onTap = { tapOffset ->
                    if (size.width == 0 || size.height == 0) return@detectTapGestures
                    if (drawingMode) {
                        val n = screenToNormalized(tapOffset)
                        onDrawingTap(n.x, n.y)
                    } else {
                        onTapEmpty()
                    }
                }
            )
        }
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .transformable(transformState)
                .onSizeChanged { size = it }
        ) {
            AsyncImage(
                model = File(photoPath),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            // === 1) Отрисовка сохранённых полигонов (кроме скрытого при перерисовке) ===
            if (size.width > 0) {
                markers.forEach { marker ->
                    if (hiddenMarkerId != null && marker.id == hiddenMarkerId) return@forEach
                    val points = PolygonCodec.decode(marker.polygonJson)
                    if (points.size >= 3) {
                        PolygonShape(
                            points = points,
                            size = size,
                            color = markerColor(marker.type).copy(
                                alpha = if (developerMode) 0.18f else 0.1f
                            ),
                            strokeColor = markerColor(marker.type).copy(
                                alpha = if (developerMode) 0.85f else 0.55f
                            )
                        )
                    }
                }
            }

            // === 2) Текущий рисуемый контур ===
            if (drawingMode && drawingPoints.isNotEmpty() && size.width > 0) {
                PolygonShape(
                    points = drawingPoints,
                    size = size,
                    color = Color(0x552196F3),
                    strokeColor = Color(0xFF2196F3),
                    dashed = true,
                    showVertices = true
                )
            }

            // === 3) Hitbox-слой ===
            if (!drawingMode && size.width > 0) {
                val density = LocalDensity.current
                markers.forEach { marker ->
                    if (hiddenMarkerId != null && marker.id == hiddenMarkerId) return@forEach
                    val pts = PolygonCodec.decode(marker.polygonJson)
                    if (pts.size >= 3) {
                        val pathPoints = pts.map {
                            Offset(it.x * size.width, it.y * size.height)
                        }
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
                                            if (isPointInPolygon(abs, pathPoints)) {
                                                onMarkerClick(marker)
                                            }
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
                for (i in 1 until pathPoints.size) {
                    lineTo(pathPoints[i].x, pathPoints[i].y)
                }
                if (!dashed) close()
            }

            if (!dashed) {
                drawPath(path, color = color)
            }

            drawPath(
                path = path,
                color = strokeColor,
                style = Stroke(
                    width = with(density) { 1.5.dp.toPx() },
                    pathEffect = if (dashed) {
                        PathEffect.dashPathEffect(floatArrayOf(20f, 15f), 0f)
                    } else null
                )
            )

            if (dashed && pathPoints.size >= 2) {
                drawLine(
                    color = strokeColor,
                    start = pathPoints.last(),
                    end = pathPoints.first(),
                    strokeWidth = with(density) { 2.dp.toPx() },
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f), 0f)
                )
            }
        }

        if (showVertices) {
            pathPoints.forEach { pt ->
                drawCircle(
                    color = strokeColor,
                    radius = with(density) { 6.dp.toPx() },
                    center = pt
                )
                drawCircle(
                    color = Color.White,
                    radius = with(density) { 3.dp.toPx() },
                    center = pt
                )
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
            (point.x < (pj.x - pi.x) * (point.y - pi.y) / (pj.y - pi.y) + pi.x)
        ) {
            inside = !inside
        }
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
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая вещь") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название (Молоток, Отвёртки...)") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name) },
                enabled = name.isNotBlank()
            ) { Text("Добавить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}