package citu.edu.stathis.mobile.core.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun CoreNavigationController(viewModel: CoreNavigationViewModel = hiltViewModel<CoreNavigationViewModel>()) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "start") {
        composable("start") {

        }
    }
}
