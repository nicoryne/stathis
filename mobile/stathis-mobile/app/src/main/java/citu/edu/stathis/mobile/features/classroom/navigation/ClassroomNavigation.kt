package citu.edu.stathis.mobile.features.classroom.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import citu.edu.stathis.mobile.features.classroom.presentation.viewmodel.ClassroomViewModel
import citu.edu.stathis.mobile.features.classroom.ui.ClassroomDetailScreen
import citu.edu.stathis.mobile.features.classroom.ui.ClassroomsListScreen
import citu.edu.stathis.mobile.features.classroom.ui.EnrollClassroomScreen
import citu.edu.stathis.mobile.features.tasks.ui.TaskDetailScreen

/**
 * Navigation constants for classroom-related screens
 */
object ClassroomDestinations {
    const val CLASSROOMS_ROUTE = "classrooms"
    const val CLASSROOM_DETAIL_ROUTE = "classroom/{classroomId}"
    const val CLASSROOM_DETAIL_ROUTE_PATTERN = "classroom/{classroomId}"
    const val ENROLL_CLASSROOM_ROUTE = "classroom/enroll"
    const val TASK_DETAIL_ROUTE = "task/{taskId}"
    const val TASK_DETAIL_ROUTE_PATTERN = "task/{taskId}"
    
    fun classroomDetailRoute(classroomId: String) = "classroom/$classroomId"
    fun taskDetailRoute(taskId: String) = "task/$taskId"
}

/**
 * Extension function to add classroom-related navigation destinations to the NavGraphBuilder
 */
fun NavGraphBuilder.classroomNavigation(navController: NavHostController) {
    // Classrooms list screen
    composable(ClassroomDestinations.CLASSROOMS_ROUTE) {
        val viewModel = hiltViewModel<ClassroomViewModel>()
        ClassroomsListScreen(
            navigateToClassroomDetail = { classroomId ->
                navController.navigate(ClassroomDestinations.classroomDetailRoute(classroomId))
            },
            navigateToEnrollClassroom = {
                navController.navigate(ClassroomDestinations.ENROLL_CLASSROOM_ROUTE)
            },
            viewModel = viewModel
        )
    }
    
    // Classroom detail screen
    composable(
        route = ClassroomDestinations.CLASSROOM_DETAIL_ROUTE_PATTERN,
        arguments = listOf(navArgument("classroomId") { type = NavType.StringType })
    ) { backStackEntry ->
        val classroomId = backStackEntry.arguments?.getString("classroomId") ?: ""
        val viewModel = hiltViewModel<ClassroomViewModel>()
        
        ClassroomDetailScreen(
            navController = navController,
            classroomId = classroomId,
            viewModel = viewModel
        )
    }
    
    // Enroll in classroom screen
    composable(ClassroomDestinations.ENROLL_CLASSROOM_ROUTE) {
        val viewModel = hiltViewModel<ClassroomViewModel>()
        EnrollClassroomScreen(
            navigateBack = {
                navController.popBackStack()
            },
            navigateToClassrooms = {
                navController.navigate(ClassroomDestinations.CLASSROOMS_ROUTE) {
                    popUpTo(ClassroomDestinations.CLASSROOMS_ROUTE) { inclusive = true }
                }
            },
            viewModel = viewModel
        )
    }
    
    // Task detail screen
    composable(
        route = ClassroomDestinations.TASK_DETAIL_ROUTE_PATTERN,
        arguments = listOf(navArgument("taskId") { type = NavType.StringType })
    ) { backStackEntry ->
        val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
        
        TaskDetailScreen(
            navController = navController,
            taskId = taskId
        )
    }
}

/**
 * Navigation helper functions for Classroom feature
 */
fun NavController.navigateToClassrooms() {
    this.navigate(ClassroomDestinations.CLASSROOMS_ROUTE) {
        popUpTo(this@navigateToClassrooms.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

fun NavController.navigateToClassroomDetail(classroomId: String) {
    this.navigate(ClassroomDestinations.classroomDetailRoute(classroomId))
}

fun NavController.navigateToEnrollClassroom() {
    this.navigate(ClassroomDestinations.ENROLL_CLASSROOM_ROUTE)
}

fun NavController.navigateToTaskDetail(taskId: String) {
    this.navigate(ClassroomDestinations.taskDetailRoute(taskId))
}
