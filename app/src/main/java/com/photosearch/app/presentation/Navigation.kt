package com.photosearch.app.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.photosearch.app.presentation.home.HomeScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Similar : Screen("similar/{imageUri}") {
        fun createRoute(imageUri: String) = "similar/$imageUri"
    }
    object Duplicates : Screen("duplicates")
    object Settings : Screen("settings")
}

@Composable
fun PhotoSearchNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
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
        
        composable(Screen.Similar.route) { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            // SimilarImagesScreen(imageUri = imageUri)
            Text("Similar Images: $imageUri")
        }
        
        composable(Screen.Duplicates.route) {
            // DuplicatesScreen()
            Text("Duplicates Screen")
        }
        
        composable(Screen.Settings.route) {
            // SettingsScreen()
            Text("Settings Screen")
        }
    }
}

@Composable
fun Text(text: String) {
    androidx.compose.material3.Text(text = text)
}
