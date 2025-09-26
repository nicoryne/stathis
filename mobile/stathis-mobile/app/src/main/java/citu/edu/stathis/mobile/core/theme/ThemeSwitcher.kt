package citu.edu.stathis.mobile.core.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Theme switcher component that allows users to change themes
 */
@Composable
fun ThemeSwitcher(
    modifier: Modifier = Modifier,
    themeViewModel: ThemeViewModel = viewModel()
) {
    val currentTheme by themeViewModel.currentTheme.collectAsState()
    val dynamicColor by themeViewModel.dynamicColor.collectAsState()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Theme Settings",
                style = MaterialTheme.typography.headlineSmall
            )
            
            // Theme mode selection
            Text(
                text = "Theme Mode",
                style = MaterialTheme.typography.titleMedium
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeMode.values().forEach { themeMode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = currentTheme == themeMode,
                                onClick = { themeViewModel.setThemeMode(themeMode) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentTheme == themeMode,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = getThemeModeDisplayName(themeMode),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            Divider()
            
            // Dynamic color toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Dynamic Color",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Use system colors (Android 12+)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = dynamicColor,
                    onCheckedChange = { themeViewModel.setDynamicColor(it) }
                )
            }
        }
    }
}

/**
 * Simple theme toggle button for quick theme switching
 */
@Composable
fun ThemeToggleButton(
    modifier: Modifier = Modifier,
    themeViewModel: ThemeViewModel = viewModel()
) {
    val currentTheme by themeViewModel.currentTheme.collectAsState()
    
    IconButton(
        onClick = { themeViewModel.toggleTheme() },
        modifier = modifier
    ) {
            Icon(
                imageVector = if (currentTheme == ThemeMode.DARK || 
                currentTheme == ThemeMode.DARK_MEDIUM_CONTRAST || 
                currentTheme == ThemeMode.DARK_HIGH_CONTRAST) {
                // Dark theme icon
                Icons.Default.DarkMode
            } else {
                // Light theme icon
                Icons.Default.LightMode
            },
                contentDescription = "Toggle theme"
            )
    }
}

/**
 * Get display name for theme mode
 */
private fun getThemeModeDisplayName(themeMode: ThemeMode): String {
    return when (themeMode) {
        ThemeMode.LIGHT -> "Light"
        ThemeMode.DARK -> "Dark"
        ThemeMode.SYSTEM -> "System"
        ThemeMode.LIGHT_MEDIUM_CONTRAST -> "Light (Medium Contrast)"
        ThemeMode.DARK_MEDIUM_CONTRAST -> "Dark (Medium Contrast)"
        ThemeMode.LIGHT_HIGH_CONTRAST -> "Light (High Contrast)"
        ThemeMode.DARK_HIGH_CONTRAST -> "Dark (High Contrast)"
    }
}
