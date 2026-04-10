package com.designspark.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object NewIdea : Screen("new_idea")
    data object Stage1 : Screen("stage1/{${ARG_PROJECT_ID}}") {
        fun createRoute(projectId: String) = "stage1/$projectId"
    }
    data object CompetitorScan : Screen("competitor_scan/{${ARG_PROJECT_ID}}") {
        fun createRoute(projectId: String) = "competitor_scan/$projectId"
    }
    data object UserInterviews : Screen("user_interviews/{${ARG_PROJECT_ID}}") {
        fun createRoute(projectId: String) = "user_interviews/$projectId"
    }
    data object Swot : Screen("swot/{${ARG_PROJECT_ID}}") {
        fun createRoute(projectId: String) = "swot/$projectId"
    }
    data object Summary : Screen("summary/{${ARG_PROJECT_ID}}") {
        fun createRoute(projectId: String) = "summary/$projectId"
    }
    data object Stage2 : Screen("stage2/{${ARG_PROJECT_ID}}") {
        fun createRoute(projectId: String) = "stage2/$projectId"
    }
    data object Stage3 : Screen("stage3/{${ARG_PROJECT_ID}}") {
        fun createRoute(projectId: String) = "stage3/$projectId"
    }

    companion object {
        const val ARG_PROJECT_ID = "projectId"
    }
}
