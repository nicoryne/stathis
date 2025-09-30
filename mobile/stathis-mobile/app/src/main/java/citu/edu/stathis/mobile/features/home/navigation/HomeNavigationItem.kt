package citu.edu.stathis.mobile.features.home.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.School
import androidx.compose.ui.graphics.vector.ImageVector

sealed class HomeNavigationItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int? = null
) {
    data object Learn : HomeNavigationItem(
        route = "learn",
        title = "Learn",
        selectedIcon = Icons.Filled.School,
        unselectedIcon = Icons.Outlined.School
    )

    data object Practice : HomeNavigationItem(
        route = "practice",
        title = "Practice",
        selectedIcon = Icons.Filled.EmojiEvents,
        unselectedIcon = Icons.Outlined.EmojiEvents,
        badgeCount = null
    )

    data object Profile : HomeNavigationItem(
        route = "profile",
        title = "Profile",
        selectedIcon = Icons.Filled.AccountCircle,
        unselectedIcon = Icons.Outlined.AccountCircle
    )
}


