package citu.edu.stathis.mobile.features.onboarding.navigation

import androidx.compose.runtime.Composable
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import citu.edu.stathis.mobile.features.onboarding.ui.screens.OnboardingWelcomeScreen
import citu.edu.stathis.mobile.features.onboarding.ui.screens.OnboardingStathisIntroScreen
import citu.edu.stathis.mobile.features.onboarding.ui.screens.OnboardingThemeChoiceScreen
import citu.edu.stathis.mobile.features.onboarding.ui.screens.OnboardingGoalScreen
import citu.edu.stathis.mobile.features.onboarding.ui.screens.OnboardingExperienceScreen
import citu.edu.stathis.mobile.features.onboarding.ui.screens.OnboardingLoadingScreen
import citu.edu.stathis.mobile.features.onboarding.ui.screens.OnboardingSmartwatchQuestionScreen
import citu.edu.stathis.mobile.features.onboarding.ui.screens.OnboardingHealthConnectConsentScreen
import citu.edu.stathis.mobile.core.theme.ThemeViewModel

object OnboardingRoutes {
    const val WELCOME = "onboard_welcome"
    const val STATHIS_INTRO = "onboard_stathis_intro"
    const val THEME_CHOICE = "onboard_theme_choice"
    const val GOAL = "onboard_goal"
    const val EXPERIENCE = "onboard_experience"
    const val SMARTWATCH_QUESTION = "onboard_smartwatch"
    const val HEALTH_CONNECT = "onboard_health_connect"
    const val LOADING = "onboard_loading/{student}/{level}"
}

@Composable
fun OnboardingNavHost(
    navController: NavHostController,
    onFinished: () -> Unit,
    themeViewModel: ThemeViewModel
) {
    NavHost(navController = navController, startDestination = OnboardingRoutes.WELCOME) {
        var selectedStudent: Boolean? = null
        composable(
            OnboardingRoutes.WELCOME,
            enterTransition = { null },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(durationMillis = 300)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(durationMillis = 300)
                )
            },
            popExitTransition = { null }
        ) {
            OnboardingWelcomeScreen(
                onNext = { navController.navigate(OnboardingRoutes.STATHIS_INTRO) },
                onLogin = { navController.navigate("onboard_loading/false/0") }
            )
        }
        composable(
            OnboardingRoutes.STATHIS_INTRO,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
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
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
        ) {
            OnboardingStathisIntroScreen(
                onNext = { navController.navigate(OnboardingRoutes.THEME_CHOICE) }
            )
        }
        composable(
            OnboardingRoutes.THEME_CHOICE,
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
            }
        ) {
            OnboardingThemeChoiceScreen(onContinue = { navController.navigate(OnboardingRoutes.GOAL) }, themeViewModel = themeViewModel)
        }
        composable(
            OnboardingRoutes.GOAL,
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
            }
        ) {
            OnboardingGoalScreen(onNext = { isStudent ->
                selectedStudent = isStudent
                navController.navigate(OnboardingRoutes.EXPERIENCE)
            })
        }
        composable(
            OnboardingRoutes.EXPERIENCE,
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
            }
        ) {
            OnboardingExperienceScreen(onConfirm = { level ->
                val student = (selectedStudent ?: false).toString()
                navController.navigate(OnboardingRoutes.SMARTWATCH_QUESTION + "?student=" + student + "&level=" + level)
            })
        }
        composable(
            OnboardingRoutes.SMARTWATCH_QUESTION + "?student={student}&level={level}",
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
            }
        ) { backStackEntry ->
            val studentArg = backStackEntry.arguments?.getString("student") ?: "false"
            val levelArg = backStackEntry.arguments?.getString("level") ?: "0"
            OnboardingSmartwatchQuestionScreen(onAnswer = { hasWatch ->
                if (hasWatch) {
                    navController.navigate(OnboardingRoutes.HEALTH_CONNECT + "?student=" + studentArg + "&level=" + levelArg)
                } else {
                    navController.navigate("onboard_loading/" + studentArg + "/" + levelArg)
                }
            })
        }
        composable(
            OnboardingRoutes.HEALTH_CONNECT + "?student={student}&level={level}",
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
            }
        ) { backStackEntry ->
            val studentArg = backStackEntry.arguments?.getString("student") ?: "false"
            val levelArg = backStackEntry.arguments?.getString("level") ?: "0"
            OnboardingHealthConnectConsentScreen(
                onContinue = {
                    navController.navigate("onboard_loading/" + studentArg + "/" + levelArg)
                },
                onSkip = {
                    navController.navigate("onboard_loading/" + studentArg + "/" + levelArg)
                }
            )
        }
        composable(
            OnboardingRoutes.LOADING,
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
            }
        ) { backStackEntry ->
            val studentArg = backStackEntry.arguments?.getString("student") ?: "false"
            val levelArg = backStackEntry.arguments?.getString("level") ?: "0"
            val isStudent = studentArg.toBooleanStrictOrNull() ?: false
            val level = levelArg.toIntOrNull() ?: 0
            OnboardingLoadingScreen(
                isStudent = isStudent,
                level = level,
                onFinished = onFinished
            )
        }
    }
}


