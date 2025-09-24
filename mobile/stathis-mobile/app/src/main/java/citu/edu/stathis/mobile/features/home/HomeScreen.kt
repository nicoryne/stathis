package citu.edu.stathis.mobile.features.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import citu.edu.stathis.mobile.core.theme.StathisSpacing
import citu.edu.stathis.mobile.features.dashboard.ui.ModernDashboardScreen
import citu.edu.stathis.mobile.features.profile.ui.EditProfileScreen
import citu.edu.stathis.mobile.features.profile.ui.ProfileScreen
import citu.edu.stathis.mobile.features.progress.ui.ProgressScreen

/**
 * Simplified Home Screen with 3-tab navigation
 * Inspired by Duolingo's clean, focused design
 */
@Composable
fun HomeScreen(
    onNavigateToAuth: () -> Unit,
    onClassroomSelected: (String) -> Unit
) {
    val navController = rememberNavController()
    var bottomBarVisible by remember { mutableStateOf(true) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom bar on certain screens
    bottomBarVisible = when (currentRoute) {
        "edit_profile" -> false
        else -> true
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        val extraBottomForHostBar = StathisSpacing.XXL
        NavHost(
            navController = navController,
            startDestination = HomeNavigationItem.Learn.route,
            modifier = Modifier.padding(bottom = bottomInset + extraBottomForHostBar)
        ) {
            // Learn Tab - Main hub with mascot and today's focus
            composable(HomeNavigationItem.Learn.route) {
                ModernDashboardScreen(
                    navController = navController,
                    onClassroomSelected = onClassroomSelected
                )
            }

            // Progress Tab - Achievements, streaks, and health summary
            composable(HomeNavigationItem.Progress.route) {
                ProgressScreen(navController = navController)
            }

            // Profile Tab - Minimal profile with mascot customization
            composable(HomeNavigationItem.Profile.route) {
                ProfileScreen(
                    navController = navController,
                    onLogout = onNavigateToAuth
                )
            }

            // Edit Profile Screen (hidden from bottom nav)
            composable("edit_profile") {
                EditProfileScreen(navController = navController)
            }

            // Legacy screens - redirect to new structure
            composable("dashboard") {
                ModernDashboardScreen(
                    navController = navController,
                    onClassroomSelected = onClassroomSelected
                )
            }

            composable("exercise") {
                // TODO: Integrate exercise into Learn hub or create simplified exercise screen
                ModernDashboardScreen(
                    navController = navController,
                    onClassroomSelected = onClassroomSelected
                )
            }

            composable("tasks") {
                // TODO: Integrate tasks into Learn hub or create simplified task screen
                ModernDashboardScreen(
                    navController = navController,
                    onClassroomSelected = onClassroomSelected
                )
            }

            composable("vitals") {
                // TODO: Integrate vitals into Progress screen
                ProgressScreen(navController = navController)
            }
        }

        // Floating Bottom Navigation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            HomeBottomNavigation(
                navController = navController,
                isVisible = bottomBarVisible
            )
        }
    }
}