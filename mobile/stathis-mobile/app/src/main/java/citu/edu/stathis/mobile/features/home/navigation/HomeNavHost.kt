package citu.edu.stathis.mobile.features.home.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import citu.edu.stathis.mobile.features.exercise.ui.screens.ExerciseTestScreen
import citu.edu.stathis.mobile.features.legal.ui.PrivacyScreen
import citu.edu.stathis.mobile.features.legal.ui.TermsScreen
import citu.edu.stathis.mobile.features.profile.ui.ProfileScreen
import citu.edu.stathis.mobile.features.profile.ui.EditProfileScreen
import citu.edu.stathis.mobile.features.profile.ui.CreateProfileScreen
import citu.edu.stathis.mobile.features.settings.ui.SettingsScreen
import citu.edu.stathis.mobile.features.support.ui.HelpScreen
import citu.edu.stathis.mobile.features.home.ui.LearnScreen
import citu.edu.stathis.mobile.features.home.ui.PracticeScreen
import citu.edu.stathis.mobile.features.home.ui.PracticeExercisesScreen
import citu.edu.stathis.mobile.features.home.ui.PracticeExercisePreviewScreen
import citu.edu.stathis.mobile.features.home.ui.PracticeExerciseSessionScreen
import citu.edu.stathis.mobile.features.classroom.presentation.ClassroomDetailScreen
import androidx.navigation.navArgument
import androidx.navigation.NavType
import citu.edu.stathis.mobile.features.auth.ui.LoginScreen
import citu.edu.stathis.mobile.features.vitals.ui.HealthConnectScreen
import citu.edu.stathis.mobile.features.tasks.presentation.TaskDetailScreen
import citu.edu.stathis.mobile.features.tasks.presentation.TaskListScreen
import citu.edu.stathis.mobile.features.classroom.presentation.viewmodel.ClassroomViewModel
import citu.edu.stathis.mobile.core.data.AuthTokenManager
import citu.edu.stathis.mobile.features.auth.domain.usecase.TokenValidationUseCase
import citu.edu.stathis.mobile.features.auth.data.enums.UserRoles
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeNavHost(navController: NavHostController) {
    val context = LocalContext.current
    val authManager = remember { AuthTokenManager(context, TokenValidationUseCase()) }
    val userRole by authManager.userRoleFlow.collectAsState(initial = null)
    val isStudent = userRole == UserRoles.STUDENT

    NavHost(navController = navController, startDestination = HomeNavigationItem.Practice.route) {
        composable(HomeNavigationItem.Learn.route) {
            if (isStudent) LearnScreen(navController) else PracticeScreen(navController)
        }
        composable(HomeNavigationItem.Practice.route) { PracticeScreen(navController) }
        composable(
            route = "practice_exercises"
        ) {
            PracticeExercisesScreen(navController)
        }
        composable(
            route = "practice_exercise_preview/{exerciseId}",
            arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: return@composable
            PracticeExercisePreviewScreen(exerciseId = exerciseId, navController = navController)
        }
        composable(
            route = "practice_session/{exerciseId}",
            arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: return@composable
            PracticeExerciseSessionScreen(exerciseId = exerciseId, navController = navController)
        }
        composable(
            route = "classroom_detail/{classroomId}",
            arguments = listOf(navArgument("classroomId") { type = NavType.StringType })
        ) { backStackEntry ->
            val classroomId = backStackEntry.arguments?.getString("classroomId") ?: return@composable
            val classroomViewModel: ClassroomViewModel = hiltViewModel()
            val verifiedMap by classroomViewModel.verifiedMap.collectAsState()

            // Ensure verification data is loaded when entering directly
            LaunchedEffect(verifiedMap.isEmpty()) {
                if (verifiedMap.isEmpty()) classroomViewModel.loadStudentClassrooms()
            }

            when (verifiedMap[classroomId]) {
                null -> PendingVerificationLoading(onBack = { navController.popBackStack() })
                true -> ClassroomDetailScreen(classroomId = classroomId, navController = navController)
                false -> PendingVerificationScreen(onBack = { navController.popBackStack() })
            }
        }
        composable(HomeNavigationItem.Profile.route) { ProfileScreen(navController) }
        composable(
            route = "settings",
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
        ) { SettingsScreen(navController) }
        composable("exercise_test") { ExerciseTestScreen(navController) }
        composable(
            route = "edit_profile",
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) }
        ) { EditProfileScreen(navController) }
        composable(
            route = "register",
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) }
        ) { CreateProfileScreen(navController) }
        composable(
            route = "login",
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) }
        ) { LoginScreen(navController) }
        composable(
            route = "help",
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) }
        ) { HelpScreen(navController) }
        composable(
            route = "terms",
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) }
        ) { TermsScreen(navController) }
        composable(
            route = "privacy",
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) }
        ) { PrivacyScreen(navController) }
        composable(
            route = "health_connect",
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) }
        ) { HealthConnectScreen(navController) }
        composable(
            route = "task_list/{classroomId}",
            arguments = listOf(navArgument("classroomId") { type = NavType.StringType }),
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) }
        ) { backStackEntry ->
            val classroomId = backStackEntry.arguments?.getString("classroomId") ?: return@composable
            val classroomViewModel: ClassroomViewModel = hiltViewModel()
            val verifiedMap by classroomViewModel.verifiedMap.collectAsState()

            // Ensure verification data is loaded when entering directly
            LaunchedEffect(verifiedMap.isEmpty()) {
                if (verifiedMap.isEmpty()) classroomViewModel.loadStudentClassrooms()
            }

            when (verifiedMap[classroomId]) {
                null -> {
                    // While verification map is loading, show a lightweight loading state
                    PendingVerificationLoading(onBack = { navController.popBackStack() })
                }
                true -> {
                    TaskListScreen(
                        classroomId = classroomId,
                        onTaskClick = { taskId ->
                            navController.navigate("task_detail/$taskId")
                        },
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
                false -> {
                    PendingVerificationScreen(onBack = { navController.popBackStack() })
                }
            }
        }
        composable(
            route = "task_detail/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.StringType }),
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) }
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            TaskDetailScreen(
                taskId = taskId,
                onNavigateBack = { navController.popBackStack() },
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

        // All upcoming tasks across classrooms
        composable(
            route = "tasks_upcoming_all",
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) }
        ) {
            citu.edu.stathis.mobile.features.home.ui.AllUpcomingTasksScreen(navController)
        }

        // No global tasks screen route; "View All" navigates to first enrolled classroom's tasks

        // Student-only lesson screen
        composable(
            route = "task_lesson/{taskId}/{templateId}",
            arguments = listOf(
                navArgument("taskId") { type = NavType.StringType },
                navArgument("templateId") { type = NavType.StringType }
            ),
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) }
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            val templateId = backStackEntry.arguments?.getString("templateId") ?: return@composable
            citu.edu.stathis.mobile.features.tasks.presentation.TaskTemplateScreen(
                taskId = taskId,
                templateType = "LESSON",
                templateId = templateId,
                onNavigateBack = { navController.popBackStack() },
                onTaskCompleted = { navController.popBackStack() }
            )
        }

        // Student-only quiz taking screen
        composable(
            route = "task_quiz/{taskId}/{templateId}",
            arguments = listOf(
                navArgument("taskId") { type = NavType.StringType },
                navArgument("templateId") { type = NavType.StringType }
            ),
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) }
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            val templateId = backStackEntry.arguments?.getString("templateId") ?: return@composable
            citu.edu.stathis.mobile.features.tasks.presentation.TaskTemplateScreen(
                taskId = taskId,
                templateType = "QUIZ",
                templateId = templateId,
                onNavigateBack = { navController.popBackStack() },
                onTaskCompleted = { navController.popBackStack() }
            )
        }

        // Student-only exercise screen
        composable(
            route = "task_exercise/{taskId}/{templateId}",
            arguments = listOf(
                navArgument("taskId") { type = NavType.StringType },
                navArgument("templateId") { type = NavType.StringType }
            ),
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) }
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: return@composable
            val templateId = backStackEntry.arguments?.getString("templateId") ?: return@composable
            citu.edu.stathis.mobile.features.tasks.presentation.TaskTemplateScreen(
                taskId = taskId,
                templateType = "EXERCISE",
                templateId = templateId,
                onNavigateBack = { navController.popBackStack() },
                onTaskCompleted = { navController.popBackStack() }
            )
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PendingVerificationScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pending Verification") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Your enrollment is pending teacher verification.",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "You will gain access once verified.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PendingVerificationLoading(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Loading…") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
