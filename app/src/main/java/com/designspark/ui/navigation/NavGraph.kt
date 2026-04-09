package com.designspark.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.designspark.ui.screens.generate.GenerateScreen
import com.designspark.ui.screens.home.HomeScreen
import com.designspark.ui.screens.projectdetail.ProjectDetailScreen

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToGenerate = { navController.navigate(Screen.Generate.route) },
                onNavigateToProject = { projectId ->
                    navController.navigate(Screen.ProjectDetail.createRoute(projectId))
                }
            )
        }
        composable(Screen.Generate.route) {
            GenerateScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProject = { projectId ->
                    navController.navigate(Screen.ProjectDetail.createRoute(projectId)) {
                        popUpTo(Screen.Generate.route) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Screen.ProjectDetail.route,
            arguments = listOf(navArgument(Screen.ARG_PROJECT_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString(Screen.ARG_PROJECT_ID) ?: return@composable
            ProjectDetailScreen(
                projectId = projectId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
