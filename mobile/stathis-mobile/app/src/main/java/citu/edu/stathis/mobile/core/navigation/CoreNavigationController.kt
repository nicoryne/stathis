package citu.edu.stathis.mobile.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import citu.edu.stathis.mobile.features.auth.ui.login.LoginScreen
import citu.edu.stathis.mobile.features.auth.ui.login.LoginViewModel
import citu.edu.stathis.mobile.features.auth.ui.register.RegisterScreen
import citu.edu.stathis.mobile.features.home.HomeScreen
import citu.edu.stathis.mobile.features.tasks.navigation.taskGraph
import citu.edu.stathis.mobile.features.tasks.navigation.navigateToTaskList
import citu.edu.stathis.mobile.features.tasks.navigation.navigateToTaskDetail

@Composable
fun CoreNavigationController(
    coreViewModel: CoreNavigationViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val biometricHelper by coreViewModel.biometricHelperState.collectAsState()
    val shouldShowBiometric by coreViewModel.shouldShowBiometric.collectAsState()
    val isLoggedIn by coreViewModel.isLoggedIn.collectAsState()
    val selectedClassroomId by coreViewModel.selectedClassroomId.collectAsState()
    val selectedTaskId by coreViewModel.selectedTaskId.collectAsState()

    LaunchedEffect(Unit) {
        if (shouldShowBiometric) {
            loginViewModel.refreshBiometricState()
        }
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            navController.navigate("home") {
                popUpTo("auth") { inclusive = true }
            }
        } else {
            if (navController.currentDestination?.route != "auth" && navController.currentDestination?.route != "register") {
                navController.navigate("auth") {
                    popUpTo("home") { inclusive = true }
                }
            }
        }
    }

    // Handle classroom selection
    LaunchedEffect(selectedClassroomId) {
        selectedClassroomId?.let { classroomId ->
            navController.navigateToTaskList(classroomId)
        }
    }

    // Handle task selection
    LaunchedEffect(selectedTaskId) {
        selectedTaskId?.let { taskId ->
            navController.navigateToTaskDetail(taskId)
        }
    }

    NavHost(navController = navController, startDestination = "auth") {
        composable("auth") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("auth") { inclusive = true }
                    }
                },
                biometricHelper = biometricHelper
            )
        }

        composable("register") {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            LaunchedEffect(isLoggedIn) {
                if (!isLoggedIn) {
                    navController.navigate("auth") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            }
            HomeScreen(
                onNavigateToAuth = {
                    navController.navigate("auth") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onClassroomSelected = { classroomId ->
                    coreViewModel.setSelectedClassroom(classroomId)
                }
            )
        }

        // Add task navigation graph
        taskGraph(navController)
    }
}