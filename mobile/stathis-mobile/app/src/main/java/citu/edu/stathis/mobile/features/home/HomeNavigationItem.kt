package citu.edu.stathis.mobile.features.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Accessibility
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.ui.graphics.vector.ImageVector

sealed class HomeNavigationItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNews: Boolean = false,
    val badgeCount: Int? = null
) {
    data object Dashboard : HomeNavigationItem(
        route = "dashboard",
        title = "Dashboard",
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard
    )

    data object Exercise : HomeNavigationItem(
        route = "exercise",
        title = "Exercise",
        selectedIcon = Icons.Filled.FitnessCenter,
        unselectedIcon = Icons.Outlined.FitnessCenter
    )

    data object Tasks : HomeNavigationItem(
        route = "tasks",
        title = "Tasks",
        selectedIcon = Icons.Filled.Assignment,
        unselectedIcon = Icons.Outlined.Assignment,
        badgeCount = 5 // Example badge count for new tasks
    )

    data object Vitals : HomeNavigationItem(
        route = "vitals",
        title = "Vitals",
        selectedIcon = Icons.Filled.Favorite,
        unselectedIcon = Icons.Outlined.Favorite
    )

    data object Profile : HomeNavigationItem(
        route = "profile",
        title = "Profile",
        selectedIcon = Icons.Filled.AccountCircle,
        unselectedIcon = Icons.Outlined.AccountCircle
    )

    data object EditProfile : HomeNavigationItem(
        route = "edit_profile",
        title = "Edit Profile",
        selectedIcon = Icons.Filled.Edit,
        unselectedIcon = Icons.Filled.Edit
    )
}