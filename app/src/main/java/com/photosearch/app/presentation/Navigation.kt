package com.photosearch.app.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.photosearch.app.presentation.duplicate.DuplicatesScreen
import com.photosearch.app.presentation.home.HomeScreen
import com.photosearch.app.presentation.settings.SettingsScreen
import com.photosearch.app.presentation.similar.SimilarImagesScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Similar : Screen("similar/{imageUri}") {
        fun createRoute(imageUri: String) = "similar/${Uri.encode(imageUri)}"
    }
    object Duplicates : Screen("duplicates")
    object Settings : Screen("settings")
}

@Composable
fun PhotoSearchNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSimilar = { imageUri ->
                    navController.navigate(Screen.Similar.createRoute(imageUri))
                },
                onNavigateToDuplicates = {
                    navController.navigate(Screen.Duplicates.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.Similar.route,
            arguments = listOf(
                navArgument("imageUri") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val imageUri = Uri.decode(backStackEntry.arguments?.getString("imageUri") ?: "")
            SimilarImagesScreen(
                imageUri = imageUri,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Duplicates.route) {
            DuplicatesScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

// 简单的 Uri 编码/解码工具
private object Uri {
    fun encode(uri: String): String = java.net.URLEncoder.encode(uri, "UTF-8")
    fun decode(uri: String): String = java.net.URLDecoder.decode(uri, "UTF-8")
}