package com.designspark.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Generate : Screen("generate")
    object ProjectDetail : Screen("project_detail/{$ARG_PROJECT_ID}") {
        fun createRoute(projectId: String) = "project_detail/$projectId"
    }

    companion object {
        const val ARG_PROJECT_ID = "projectId"
    }
}
