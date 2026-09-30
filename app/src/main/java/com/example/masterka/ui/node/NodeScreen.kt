package com.example.masterka.ui.node

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.masterka.data.AppTheme
import com.example.masterka.data.StorageNode
import com.example.masterka.ui.common.NodeContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodeScreen(
    nodeId: Long,
    developerMode: Boolean,
    onDeveloperModeChange: (Boolean) -> Unit,
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    onBack: () -> Unit,
    onChildClick: (StorageNode) -> Unit,
    onItemClick: (StorageNode) -> Unit,
    onCategoriesClick: () -> Unit,      // ← НОВОЕ
    onBackupClick: () -> Unit,          // ← НОВОЕ
    vm: NodeViewModel = viewModel()
) {
    LaunchedEffect(nodeId) { vm.loadNode(nodeId) }

    val node by vm.currentNode.collectAsState()
    val children by vm.children.collectAsState()
    val breadcrumbs by vm.breadcrumbs.collectAsState()
    val allCategories by vm.allCategories.collectAsState()
    val categoriesMap by vm.categoriesMap.collectAsState()

    val current = node ?: return

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
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            NodeContent(
                node = current,
                children = children,
                developerMode = developerMode,
                onDeleteNode = { n -> vm.deleteChild(n) },
                onDeleteNodes = { ids -> vm.deleteItems(ids) },                  // ← НОВОЕ
                onMoveNodes = { ids, parentId -> vm.moveItems(ids, parentId) },  // ← НОВОЕ
                allCategories = allCategories,
                categoriesMap = categoriesMap,
                currentTheme = currentTheme,
                onThemeChange = onThemeChange,
                onDeveloperModeChange = onDeveloperModeChange,
                onCategoriesClick = onCategoriesClick,      // ← НОВОЕ
                onBackupClick = onBackupClick,              // ← НОВОЕ
                onAddCategoryToDb = { name -> vm.addCategory(name) },   // ← НОВОЕ
                onAddPhoto = { path -> vm.addPhoto(path) },
                onRemovePhoto = { index -> vm.removePhotoAt(index) },
                onAddContainerAtPhoto = { name, points, photoIndex ->
                    vm.addContainerAtPhoto(name, points, photoIndex)
                },
                onUpdatePolygon = { id, points -> vm.updatePolygon(id, points) },
                onChildClick = onChildClick,
                onItemClick = onItemClick,
                showMaterialsButton = false,
                showAddItem = true,
                onLoadChildren = { parentId -> vm.loadChildrenOnce(parentId) },
                onLoadParent = { id -> vm.loadParentOnce(id) },   // ← НОВОЕ
                onAddItemWithCategory = { name, qty, unit, note, category, photoPath ->
                    vm.addItemFull(name, qty, unit, note, category, photoPath)
                }
            )
        }
    }
}