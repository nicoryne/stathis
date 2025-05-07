package citu.edu.stathis.mobile.features.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import citu.edu.stathis.mobile.core.theme.BrandColors
import citu.edu.stathis.mobile.features.dashboard.ui.DashboardScreen
import citu.edu.stathis.mobile.features.posture.PostureScreen
import citu.edu.stathis.mobile.features.profile.ui.EditProfileScreen
import citu.edu.stathis.mobile.features.profile.ui.ProfileScreen
import citu.edu.stathis.mobile.features.progress.ui.ProgressScreen
import citu.edu.stathis.mobile.features.tasks.ui.TasksScreen

@Composable
fun HomeScreen(
    onNavigateToAuth: () -> Unit
) {
    val navController = rememberNavController()
    var bottomBarVisible by remember { mutableStateOf(true) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom bar on certain screens
    bottomBarVisible = when (currentRoute) {
        HomeNavigationItem.EditProfile.route -> false
        else -> true
    }

    Scaffold(
        bottomBar = {
            HomeBottomNavigation(
                navController = navController,
                isVisible = bottomBarVisible
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeNavigationItem.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(HomeNavigationItem.Dashboard.route) {
                DashboardScreen(navController = navController)
            }

            composable(HomeNavigationItem.Posture.route) {
                PostureScreen(navController = navController)
            }

            composable(HomeNavigationItem.Tasks.route) {
                TasksScreen(navController = navController)
            }

            composable(HomeNavigationItem.Progress.route) {
                ProgressScreen(navController = navController)
            }

            composable(HomeNavigationItem.Profile.route) {
                ProfileScreen(
                    navController = navController,
                    onLogout = onNavigateToAuth
                )
            }

            // Edit Profile Screen
            composable(HomeNavigationItem.EditProfile.route) {
                EditProfileScreen(navController = navController)
            }

            // Add other nested navigation routes here as needed
        }
    }
}

@Composable
fun HomeBottomNavigation(
    navController: NavHostController,
    isVisible: Boolean = true
) {
    val screens = listOf(
        HomeNavigationItem.Dashboard,
        HomeNavigationItem.Posture,
        HomeNavigationItem.Tasks,
        HomeNavigationItem.Progress,
        HomeNavigationItem.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        NavigationBar(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            screens.forEach { screen ->
                val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id)
                            launchSingleTop = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                            contentDescription = screen.title,
                            tint = if (selected) BrandColors.Purple else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    label = {
                        Text(
                            text = screen.title,
                            color = if (selected) BrandColors.Purple else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
    }
}