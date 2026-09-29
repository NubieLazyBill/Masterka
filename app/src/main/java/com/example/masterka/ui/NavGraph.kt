package com.example.masterka.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.masterka.data.AppTheme
import com.example.masterka.data.ThemePreferences
import com.example.masterka.ui.backup.BackupScreen
import com.example.masterka.ui.categories.CategoriesScreen
import com.example.masterka.ui.home.HomeScreen
import com.example.masterka.ui.item.ItemScreen
import com.example.masterka.ui.materials.MaterialsScreen
import com.example.masterka.ui.node.NodeScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Routes {
    const val HOME = "home"
    const val NODE = "node/{nodeId}"
    const val MATERIALS = "materials"
    const val ITEM = "item/{itemId}"
    const val BACKUP = "backup"
    const val CATEGORIES = "categories"
    fun node(id: Long) = "node/$id"
    fun item(id: Long) = "item/$id"
}

@Composable
fun MasterkaNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Текущая тема (читается из DataStore)
    val appTheme by ThemePreferences
        .getTheme(context)
        .collectAsState(initial = AppTheme.WORKSHOP)

    // Обработчик смены темы
    val onThemeChange: (AppTheme) -> Unit = { newTheme ->
        CoroutineScope(Dispatchers.IO).launch {
            ThemePreferences.setTheme(context, newTheme)
        }
    }

    // Dev-режим сохраняется между переходами, сбрасывается при выходе из приложения
    var developerMode by rememberSaveable { mutableStateOf(false) }

    NavHost(navController = navController, startDestination = Routes.HOME) {

        // ==== ГЛАВНЫЙ ЭКРАН ====
        composable(Routes.HOME) {
            HomeScreen(
                developerMode = developerMode,
                onDeveloperModeChange = { developerMode = it },
                currentTheme = appTheme,
                onThemeChange = onThemeChange,
                onMaterialsClick = {
                    navController.navigate(Routes.MATERIALS)
                },
                onChildClick = { node ->
                    navController.navigate(Routes.node(node.id))
                },
                onItemClick = { item ->
                    navController.navigate(Routes.item(item.id))
                },
                onBackupClick = {
                    navController.navigate(Routes.BACKUP)
                },
                onCategoriesClick = {
                    navController.navigate(Routes.CATEGORIES)
                }
            )
        }

        // ==== ЭКРАН УЗЛА ====
        composable(
            route = Routes.NODE,
            arguments = listOf(navArgument("nodeId") { type = NavType.LongType })
        ) { backStack ->
            val nodeId = backStack.arguments?.getLong("nodeId") ?: return@composable
            NodeScreen(
                nodeId = nodeId,
                developerMode = developerMode,
                onDeveloperModeChange = { developerMode = it },
                currentTheme = appTheme,
                onThemeChange = onThemeChange,
                onBack = { navController.popBackStack() },
                onChildClick = { child ->
                    navController.navigate(Routes.node(child.id))
                },
                onItemClick = { item ->
                    navController.navigate(Routes.item(item.id))
                }
            )
        }

        // ==== ЭКРАН ВСЕХ МАТЕРИАЛОВ ====
        composable(Routes.MATERIALS) {
            MaterialsScreen(
                onBack = { navController.popBackStack() },
                onLocationClick = { item ->
                    val parentId = item.parentId
                    if (parentId != null) {
                        navController.navigate(Routes.node(parentId))
                    }
                }
            )
        }

        // ==== ЭКРАН ОТДЕЛЬНОГО МАТЕРИАЛА ====
        composable(
            route = Routes.ITEM,
            arguments = listOf(navArgument("itemId") { type = NavType.LongType })
        ) { backStack ->
            val itemId = backStack.arguments?.getLong("itemId") ?: return@composable
            ItemScreen(
                itemId = itemId,
                onBack = { navController.popBackStack() }
            )
        }

        // ==== ЭКРАН РЕЗЕРВНОЙ КОПИИ ====
        composable(Routes.BACKUP) {
            BackupScreen(onBack = { navController.popBackStack() })
        }

        // ==== ЭКРАН КАТЕГОРИЙ ====
        composable(Routes.CATEGORIES) {
            CategoriesScreen(onBack = { navController.popBackStack() })
        }
    }
}