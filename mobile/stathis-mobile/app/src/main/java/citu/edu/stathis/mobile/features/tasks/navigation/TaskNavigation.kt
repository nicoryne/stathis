package citu.edu.stathis.mobile.features.tasks.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import citu.edu.stathis.mobile.features.tasks.presentation.TaskDetailScreen
import citu.edu.stathis.mobile.features.tasks.presentation.TaskListScreen
import citu.edu.stathis.mobile.features.tasks.presentation.TaskTemplateScreen

const val taskListRoute = "task_list/{classroomId}"
const val taskDetailRoute = "task_detail/{taskId}"
const val taskQuizRoute = "task_quiz/{taskId}/{templateId}"
const val taskLessonRoute = "task_lesson/{taskId}/{templateId}"
const val taskExerciseRoute = "task_exercise/{taskId}/{templateId}"

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
            },
            onStartLesson = { templateId ->
                navController.navigate("task_lesson/$taskId/$templateId")
            },
            onStartQuiz = { templateId ->
                navController.navigate("task_quiz/$taskId/$templateId")
            },
            onStartExercise = { templateId ->
                navController.navigate("task_exercise/$taskId/$templateId")
            }
        )
    }

    // Student-only quiz taking screen
    composable(
        route = taskQuizRoute,
        arguments = listOf(
            navArgument("taskId") { type = NavType.StringType },
            navArgument("templateId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
        val templateId = backStackEntry.arguments?.getString("templateId") ?: return@composable
        TaskTemplateScreen(
            taskId = taskId,
            templateType = "QUIZ",
            templateId = templateId,
            onNavigateBack = { navController.popBackStack() },
            onTaskCompleted = { navController.popBackStack() }
        )
    }

    // Student-only exercise screen (duplicate of practice flow access)
    composable(
        route = taskExerciseRoute,
        arguments = listOf(
            navArgument("taskId") { type = NavType.StringType },
            navArgument("templateId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
        val templateId = backStackEntry.arguments?.getString("templateId") ?: return@composable
        TaskTemplateScreen(
            taskId = taskId,
            templateType = "EXERCISE",
            templateId = templateId,
            onNavigateBack = { navController.popBackStack() },
            onTaskCompleted = { navController.popBackStack() }
        )
    }

    // Student-only lesson screen
    composable(
        route = taskLessonRoute,
        arguments = listOf(
            navArgument("taskId") { type = NavType.StringType },
            navArgument("templateId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
        val templateId = backStackEntry.arguments?.getString("templateId") ?: return@composable
        TaskTemplateScreen(
            taskId = taskId,
            templateType = "LESSON",
            templateId = templateId,
            onNavigateBack = { navController.popBackStack() },
            onTaskCompleted = { navController.popBackStack() }
        )
    }
}