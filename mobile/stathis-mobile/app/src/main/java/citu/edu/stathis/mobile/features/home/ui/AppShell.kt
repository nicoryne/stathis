package citu.edu.stathis.mobile.features.home.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import citu.edu.stathis.mobile.core.data.AuthTokenManager
import citu.edu.stathis.mobile.features.auth.domain.usecase.TokenValidationUseCase
import citu.edu.stathis.mobile.features.auth.data.enums.UserRoles
import androidx.navigation.compose.rememberNavController
import citu.edu.stathis.mobile.features.home.navigation.HomeBottomNavigation
import citu.edu.stathis.mobile.features.home.navigation.HomeNavHost
import citu.edu.stathis.mobile.features.home.navigation.HomeNavigationItem

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppShell() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authManager = remember { AuthTokenManager(context, TokenValidationUseCase()) }
    val userRole by authManager.userRoleFlow.collectAsState(initial = null)
    val isStudent = userRole == UserRoles.STUDENT
    val items = if (isStudent) {
        listOf(HomeNavigationItem.Practice, HomeNavigationItem.Learn, HomeNavigationItem.Profile)
    } else {
        listOf(HomeNavigationItem.Practice, HomeNavigationItem.Profile)
    }
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute == HomeNavigationItem.Practice.route ||
            (isStudent && currentRoute == HomeNavigationItem.Learn.route) ||
            currentRoute == HomeNavigationItem.Profile.route ||
            currentRoute == null
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                HomeBottomNavigation(navController = navController, items = items)
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars) // only status bar padding
        ) {
            HomeNavHost(navController = navController)
        }
    }
}


