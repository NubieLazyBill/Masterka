package com.example.masterka.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.masterka.data.NodeType
import com.example.masterka.ui.home.HomeScreen
import com.example.masterka.ui.item.ItemScreen
import com.example.masterka.ui.materials.MaterialsScreen
import com.example.masterka.ui.node.NodeScreen

object Routes {
    const val HOME = "home"
    const val NODE = "node/{nodeId}"
    const val MATERIALS = "materials"
    const val ITEM = "item/{itemId}"
    fun node(id: Long) = "node/$id"
    fun item(id: Long) = "item/$id"
}

@Composable
fun MasterkaNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {

        // ==== ГЛАВНЫЙ ЭКРАН ====
        composable(Routes.HOME) {
            HomeScreen(
                onZoneClick = { zone ->
                    navController.navigate(Routes.node(zone.id))
                },
                onMaterialsClick = {
                    navController.navigate(Routes.MATERIALS)
                },
                onSearchResultClick = { node ->
                    val targetId = if (node.type == NodeType.ITEM) {
                        node.parentId ?: node.id
                    } else {
                        node.id
                    }
                    navController.navigate(Routes.node(targetId))
                }
            )
        }

        // ==== ЭКРАН ЗОНЫ/КОНТЕЙНЕРА ====
        composable(
            route = Routes.NODE,
            arguments = listOf(navArgument("nodeId") { type = NavType.LongType })
        ) { backStack ->
            val nodeId = backStack.arguments?.getLong("nodeId") ?: return@composable
            NodeScreen(
                nodeId = nodeId,
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
    }
}