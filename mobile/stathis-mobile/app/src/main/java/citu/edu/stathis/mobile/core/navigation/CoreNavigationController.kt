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

@Composable
fun CoreNavigationController(
    coreViewModel: CoreNavigationViewModel = hiltViewModel<CoreNavigationViewModel>(), // Renamed for clarity
    loginViewModel: LoginViewModel = hiltViewModel<LoginViewModel>(),
) {
    val navController = rememberNavController()
    val biometricHelper by coreViewModel.biometricHelperState.collectAsState()
    val isLoggedIn by coreViewModel.isLoggedIn.collectAsState()

    LaunchedEffect(Unit) {
        loginViewModel.refreshBiometricState()
    }

     LaunchedEffect(isLoggedIn, navController) {
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


    NavHost(navController = navController, startDestination = "auth") {
        composable("splash") {
             LaunchedEffect(isLoggedIn) {
                if (isLoggedIn) {
                    navController.navigate("home") { popUpTo("splash") { inclusive = true } }
                } else {
                    navController.navigate("auth") { popUpTo("splash") { inclusive = true } }
                }
             }
        }

        composable("auth") {
            LaunchedEffect(isLoggedIn) {
                if (isLoggedIn) {
                    navController.navigate("home") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            }
            if (!isLoggedIn) {
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
            if (isLoggedIn) {
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
}