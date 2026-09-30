package com.example.masterka.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoveDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.masterka.data.NodeType
import com.example.masterka.data.NormalizedPoint
import com.example.masterka.data.PolygonCodec
import com.example.masterka.data.StorageNode
import com.example.masterka.data.allPhotoPaths
import java.io.File
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

/**
 * Полноэкранный экран выбора контейнера для перемещения.
 * Показывает фото текущего узла + маркеры-полигоны. Тап по контейнеру — заходим внутрь.
 * Долгий тап по контейнеру — сразу переместить туда. FAB — переместить в текущий узел.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovePickerScreen(
    rootNode: StorageNode,
    loadChildren: suspend (Long) -> List<StorageNode>,
    onConfirm: (Long) -> Unit,
    onCancel: () -> Unit
) {
    // Стек узлов — от корня к текущему.
    val navStack = remember { mutableStateListOf<StorageNode>(rootNode) }
    val current = navStack.last()

    // Дети текущего узла (без ITEM — вещи не могут содержать вещи)
    var children by remember { mutableStateOf<List<StorageNode>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(current.id) {
        loading = true
        children = loadChildren(current.id).filter { it.type != NodeType.ITEM }
        loading = false
    }

    // Фото текущего узла и пагинация
    val photos = current.allPhotoPaths()
    var currentPage by remember { mutableStateOf(0) }
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { photos.size.coerceAtLeast(1) }
    )
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { currentPage = it }
    }
    // Сброс страницы при смене узла
    LaunchedEffect(current.id) {
        if (photos.isNotEmpty()) pagerState.scrollToPage(0)
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
                                "Куда положить?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                current.name,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
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
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { onConfirm(current.id) },
                    icon = { Icon(Icons.Default.MoveDown, contentDescription = null) },
                    text = { Text("Переместить сюда") },
                    modifier = Modifier.padding(bottom = 72.dp)
                )
            }
        ) { padding ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when {
                    loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    photos.isEmpty() -> {
                        // Нет фото — показываем список детей, как раньше
                        if (children.isEmpty()) {
                            EmptyState(
                                text = "Здесь пусто.\nМожно переместить прямо сюда — кнопка ниже."
                            )
                        } else {
                            Column(Modifier.fillMaxSize()) {
                                Text(
                                    "У зоны нет фото. Выбери контейнер из списка:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                                LazyColumnForChildren(
                                    children = children,
                                    onEnter = { navStack.add(it) },
                                    onQuickMove = { onConfirm(it.id) }
                                )
                            }
                        }
                    }
                    else -> {
                        // Фото + маркеры
                        MovePhotoWithMarkers(
                            photos = photos,
                            pagerState = pagerState,
                            currentPage = currentPage,
                            markers = children,
                            onMarkerTap = { marker -> navStack.add(marker) },
                            onMarkerLongTap = { marker -> onConfirm(marker.id) }
                        )

                        // Подсказка вверху
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 8.dp)
                        ) {
                            Text(
                                "Тап — зайти внутрь. Долгий тап — переместить сразу.",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LazyColumnForChildren(
    children: List<StorageNode>,
    onEnter: (StorageNode) -> Unit,
    onQuickMove: (StorageNode) -> Unit
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        items(children, key = { it.id }) { child ->
            ListItem(
                headlineContent = { Text(child.name) },
                supportingContent = {
                    Text(
                        if (child.type == NodeType.ZONE) "Зона" else "Контейнер",
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                trailingContent = {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null
                    )
                },
                modifier = Modifier
                    .clickable { onEnter(child) }
                    .pointerInput(child.id) {
                        detectTapGestures(
                            onLongPress = { onQuickMove(child) }
                        )
                    }
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun EmptyState(text: String) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Фото с маркерами-полигонами. Тап по полигону — колбэк, долгий тап — второй колбэк.
 */
@Composable
private fun MovePhotoWithMarkers(
    photos: List<String>,
    pagerState: androidx.compose.foundation.pager.PagerState,
    currentPage: Int,
    markers: List<StorageNode>,
    onMarkerTap: (StorageNode) -> Unit,
    onMarkerLongTap: (StorageNode) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        // Счётчик страниц
        if (photos.size > 1) {
            Row(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp)
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

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = true
        ) { page ->
            val photoPath = photos[page]
            val pageMarkers = markers.filter { it.photoIndex == page && it.polygonJson != null }

            PhotoWithMarkersForMove(
                photoPath = photoPath,
                markers = pageMarkers,
                onMarkerTap = onMarkerTap,
                onMarkerLongTap = onMarkerLongTap
            )
        }
    }
}

@Composable
private fun PhotoWithMarkersForMove(
    photoPath: String,
    markers: List<StorageNode>,
    onMarkerTap: (StorageNode) -> Unit,
    onMarkerLongTap: (StorageNode) -> Unit
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    Box(
        Modifier
            .fillMaxSize()
            .onSizeChanged { size = it }
    ) {
        AsyncImage(
            model = File(photoPath),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        if (size.width == 0) return@Box

        markers.forEach { marker ->
            val pts = PolygonCodec.decode(marker.polygonJson)
            if (pts.size < 3) return@forEach

            val pathPoints = pts.map { Offset(it.x * size.width, it.y * size.height) }

            androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                val path = Path().apply {
                    moveTo(pathPoints.first().x, pathPoints.first().y)
                    for (i in 1 until pathPoints.size) {
                        lineTo(pathPoints[i].x, pathPoints[i].y)
                    }
                    close()
                }
                drawPath(
                    path = path,
                    color = Color(0x332196F3)
                )
                drawPath(
                    path = path,
                    color = Color(0xFF2196F3),
                    style = Stroke(width = with(density) { 2.dp.toPx() })
                )
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
                    .pointerInput(marker.id) {
                        detectTapGestures(
                            onTap = { local ->
                                val abs = Offset(minX + local.x, minY + local.y)
                                if (isPointInPolygon(abs, pathPoints)) onMarkerTap(marker)
                            },
                            onLongPress = { local ->
                                val abs = Offset(minX + local.x, minY + local.y)
                                if (isPointInPolygon(abs, pathPoints)) onMarkerLongTap(marker)
                            }
                        )
                    }
            )
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