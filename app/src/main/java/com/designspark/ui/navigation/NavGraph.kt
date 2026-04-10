package com.designspark.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.designspark.ui.screens.competitorscan.CompetitorScanScreen
import com.designspark.ui.screens.home.HomeScreen
import com.designspark.ui.screens.newidea.NewIdeaScreen
import com.designspark.ui.screens.stage1.Stage1Screen
import com.designspark.ui.screens.stage2.Stage2Screen
import com.designspark.ui.screens.stage3.Stage3Screen
import com.designspark.ui.screens.summary.SummaryScreen
import com.designspark.ui.screens.swot.SwotScreen
import com.designspark.ui.screens.userinterviews.UserInterviewsScreen

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToNewIdea = { navController.navigate(Screen.NewIdea.route) },
                onNavigateToProject = { id ->
                    navController.navigate(Screen.Stage1.createRoute(id))
                }
            )
        }
        composable(Screen.NewIdea.route) {
            NewIdeaScreen(
                onNavigateToStage1 = { id ->
                    navController.navigate(Screen.Stage1.createRoute(id)) {
                        popUpTo(Screen.NewIdea.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            Screen.Stage1.route,
            arguments = listOf(navArgument(Screen.ARG_PROJECT_ID) { type = NavType.StringType })
        ) { back ->
            val projectId = back.arguments?.getString(Screen.ARG_PROJECT_ID)!!
            Stage1Screen(
                projectId = projectId,
                onNavigateToCompetitors = { navController.navigate(Screen.CompetitorScan.createRoute(projectId)) },
                onNavigateToInterviews = { navController.navigate(Screen.UserInterviews.createRoute(projectId)) },
                onNavigateToSwot = { navController.navigate(Screen.Swot.createRoute(projectId)) },
                onNavigateToSummary = { navController.navigate(Screen.Summary.createRoute(projectId)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            Screen.CompetitorScan.route,
            arguments = listOf(navArgument(Screen.ARG_PROJECT_ID) { type = NavType.StringType })
        ) { back ->
            val projectId = back.arguments?.getString(Screen.ARG_PROJECT_ID)!!
            CompetitorScanScreen(
                projectId = projectId,
                onNext = {
                    navController.navigate(Screen.UserInterviews.createRoute(projectId)) {
                        popUpTo(Screen.CompetitorScan.createRoute(projectId)) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            Screen.UserInterviews.route,
            arguments = listOf(navArgument(Screen.ARG_PROJECT_ID) { type = NavType.StringType })
        ) { back ->
            val projectId = back.arguments?.getString(Screen.ARG_PROJECT_ID)!!
            UserInterviewsScreen(
                projectId = projectId,
                onNext = {
                    navController.navigate(Screen.Swot.createRoute(projectId)) {
                        popUpTo(Screen.UserInterviews.createRoute(projectId)) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            Screen.Swot.route,
            arguments = listOf(navArgument(Screen.ARG_PROJECT_ID) { type = NavType.StringType })
        ) { back ->
            val projectId = back.arguments?.getString(Screen.ARG_PROJECT_ID)!!
            SwotScreen(
                projectId = projectId,
                onDone = {
                    navController.navigate(Screen.Summary.createRoute(projectId)) {
                        popUpTo(Screen.Stage1.createRoute(projectId)) { inclusive = false }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            Screen.Summary.route,
            arguments = listOf(navArgument(Screen.ARG_PROJECT_ID) { type = NavType.StringType })
        ) { back ->
            val projectId = back.arguments?.getString(Screen.ARG_PROJECT_ID)!!
            SummaryScreen(
                projectId = projectId,
                onContinueToStage2 = { navController.navigate(Screen.Stage2.createRoute(projectId)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            Screen.Stage2.route,
            arguments = listOf(navArgument(Screen.ARG_PROJECT_ID) { type = NavType.StringType })
        ) { back ->
            Stage2Screen(onBack = { navController.popBackStack() })
        }
        composable(
            Screen.Stage3.route,
            arguments = listOf(navArgument(Screen.ARG_PROJECT_ID) { type = NavType.StringType })
        ) { back ->
            Stage3Screen(onBack = { navController.popBackStack() })
        }
    }
}
