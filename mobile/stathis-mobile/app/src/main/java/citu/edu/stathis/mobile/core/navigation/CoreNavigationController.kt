package citu.edu.stathis.mobile.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import citu.edu.stathis.mobile.features.auth.ui.forgotpassword.ForgotPasswordScreen
import citu.edu.stathis.mobile.features.auth.ui.login.LoginScreen
import citu.edu.stathis.mobile.features.auth.ui.login.LoginViewModel
import citu.edu.stathis.mobile.features.auth.ui.register.RegisterScreen
import citu.edu.stathis.mobile.features.home.HomeScreen

@Composable
fun CoreNavigationController(
    viewModel: CoreNavigationViewModel = hiltViewModel<CoreNavigationViewModel>(),
    loginViewModel: LoginViewModel = hiltViewModel<LoginViewModel>(),
) {
    val navController = rememberNavController()
    val biometricHelper by viewModel.biometricHelperState.collectAsState()

    LaunchedEffect(Unit) {
        loginViewModel.refreshBiometricState()
    }

    NavHost(navController = navController, startDestination = "auth") {
        composable("splash") {
            // Splash screen will be shown while checking auth state
        }

        composable("auth") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToForgotPassword = { navController.navigate("forgot_password") },
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

        composable("forgot_password") {
            ForgotPasswordScreen(
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable("home") {
            // Use our HomeScreen here
            HomeScreen(
                onNavigateToAuth = {
                    navController.navigate("auth") {
                        popUpTo(0) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}