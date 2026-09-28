package com.example.masterka.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.masterka.ui.home.HomeScreen
import com.example.masterka.ui.node.NodeScreen

object Routes {
    const val HOME = "home"
    const val NODE = "node/{nodeId}"
    fun node(id: Long) = "node/$id"
}

@Composable
fun MasterkaNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onZoneClick = { zone ->
                    navController.navigate(Routes.node(zone.id))
                }
            )
        }
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
                }
            )
        }
    }
}