package citu.edu.stathis.mobile.features.home.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
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

@Composable
fun HomeNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = HomeNavigationItem.Practice.route) {
        composable(HomeNavigationItem.Learn.route) { LearnScreen(navController) }
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
            ClassroomDetailScreen(classroomId = classroomId, navController = navController)
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
    }
}


