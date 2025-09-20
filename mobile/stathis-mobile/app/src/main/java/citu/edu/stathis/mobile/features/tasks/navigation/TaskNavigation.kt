package citu.edu.stathis.mobile.features.tasks.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import citu.edu.stathis.mobile.features.tasks.presentation.TaskDetailScreen
import citu.edu.stathis.mobile.features.tasks.presentation.TaskListScreen

const val taskListRoute = "task_list/{classroomId}"
const val taskDetailRoute = "task_detail/{taskId}"

fun NavController.navigateToTaskList(classroomId: String) {
    navigate("task_list/$classroomId")
}

fun NavController.navigateToTaskDetail(taskId: String) {
    navigate("task_detail/$taskId")
}

fun NavGraphBuilder.taskGraph(navController: NavController) {
    composable(
        route = taskListRoute,
        arguments = listOf(
            navArgument("classroomId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val classroomId = backStackEntry.arguments?.getString("classroomId") ?: return@composable
        TaskListScreen(
            classroomId = classroomId,
            onTaskClick = { taskId ->
                navController.navigateToTaskDetail(taskId)
            }
        )
    }

    composable(
        route = taskDetailRoute,
        arguments = listOf(
            navArgument("taskId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
        TaskDetailScreen(
            taskId = taskId,
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }
}