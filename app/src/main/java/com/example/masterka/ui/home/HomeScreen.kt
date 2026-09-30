package com.example.masterka.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.masterka.data.AppTheme
import com.example.masterka.data.StorageNode
import com.example.masterka.ui.common.NodeContent
import com.example.masterka.ui.spaces.SpaceDropdown

@Composable
fun HomeScreen(
    developerMode: Boolean,
    onDeveloperModeChange: (Boolean) -> Unit,
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    onMaterialsClick: () -> Unit,
    onChildClick: (StorageNode) -> Unit,
    onItemClick: (StorageNode) -> Unit,
    onBackupClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    onSpacesClick: () -> Unit,                    // ← НОВОЕ
    vm: HomeViewModel = viewModel()
) {
    val root by vm.rootNode.collectAsState()
    val children by vm.children.collectAsState()
    val allCategories by vm.allCategories.collectAsState()
    val categoriesMap by vm.categoriesMap.collectAsState()
    val spaces by vm.spaces.collectAsState()
    val activeSpaceId by vm.activeSpaceId.collectAsState()

    val rootNode = root ?: return

    Scaffold { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            NodeContent(
                node = rootNode,
                children = children,
                developerMode = developerMode,
                allCategories = allCategories,
                currentTheme = currentTheme,
                onThemeChange = onThemeChange,
                onDeveloperModeChange = onDeveloperModeChange,
                onAddPhoto = { path -> vm.addPhoto(path) },
                onRemovePhoto = { index -> vm.removePhotoAt(index) },
                onLoadChildren = { parentId -> vm.loadChildrenOnce(parentId) },
                onLoadParent = { id -> vm.loadParentOnce(id) },
                onAddContainerAtPhoto = { name, points, photoIndex ->
                    vm.addContainerAtPhoto(name, points, photoIndex)
                },
                onUpdatePolygon = { id, points -> vm.updatePolygon(id, points) },
                onDeleteNode = { node -> vm.deleteNode(node) },
                onDeleteNodes = { ids -> vm.deleteItems(ids) },
                onMoveNodes = { ids, parentId -> vm.moveItems(ids, parentId) },
                onChildClick = onChildClick,
                onItemClick = onItemClick,
                showMaterialsButton = true,
                onMaterialsClick = onMaterialsClick,
                showAddItem = false,
                onAddItemWithCategory = { _, _, _, _, _, _ -> },
                onBackupClick = onBackupClick,
                onCategoriesClick = onCategoriesClick,
                categoriesMap = categoriesMap,
            )

            // ==== Dropdown выбора помещения — поверх всего, сверху слева ====
            SpaceDropdown(
                spaces = spaces,
                activeSpaceId = activeSpaceId,
                onSelect = { id -> vm.switchSpace(id) },
                onManage = onSpacesClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 12.dp, top = 60.dp)
            )
        }
    }
}