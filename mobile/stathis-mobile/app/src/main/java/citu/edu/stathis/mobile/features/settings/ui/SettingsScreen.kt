package citu.edu.stathis.mobile.features.settings.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Brightness6
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.QuestionMark
import androidx.compose.material.icons.outlined.SettingsInputAntenna
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import citu.edu.stathis.mobile.core.theme.ThemeMode
import citu.edu.stathis.mobile.core.theme.ThemeViewModel

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun SettingsScreen(navController: NavHostController) {
    val themeViewModel: ThemeViewModel = hiltViewModel()
    val currentThemeState = themeViewModel.currentTheme
    val showAppearanceDialog = remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SectionHeader("App Settings")
            SettingsRow(
                icon = Icons.Outlined.Brightness6,
                title = "Appearance",
                onClick = { showAppearanceDialog.value = true },
                trailing = {
                    Text(currentThemeState.value.displayName(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            )
            SettingsRow(
                icon = Icons.Outlined.SettingsInputAntenna,
                title = "Health Connect",
                onClick = { navController.navigate("health_connect") }
            )
            SettingsRow(
                icon = Icons.Outlined.Videocam,
                title = "Test My Camera",
                onClick = { navController.navigate("exercise_test") }
            )

            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader("Share")
            SettingsRow(
                icon = Icons.Outlined.Policy,
                title = "Rate us on the Play Store",
                onClick = { /* TODO */ })
            SettingsRow(
                icon = Icons.Outlined.Policy,
                title = "Follow us on X",
                onClick = { /* TODO */ })
            SettingsRow(
                icon = Icons.Outlined.Policy,
                title = "Like us on Facebook",
                onClick = { /* TODO */ })

            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader("Contact us")
            SettingsRow(
                icon = Icons.Outlined.QuestionMark,
                title = "Help",
                onClick = { navController.navigate("help") })
            SettingsRow(
                icon = Icons.Outlined.Policy,
                title = "Terms and conditions",
                onClick = { navController.navigate("terms") })
            SettingsRow(
                icon = Icons.Outlined.Policy,
                title = "Privacy policy",
                onClick = { navController.navigate("privacy") })

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Version 1.0\n© 2025 Stathis",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }

    if (showAppearanceDialog.value) {
        AppearanceDialog(
            current = currentThemeState.value,
            onDismiss = { showAppearanceDialog.value = false },
            onSelect = { mode ->
                themeViewModel.setThemeMode(mode)
                showAppearanceDialog.value = false
            }
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null
) {
    val bg = MaterialTheme.colorScheme.surfaceVariant
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(bg, shape = MaterialTheme.shapes.medium)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.weight(1f))
        if (trailing != null) trailing()
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun AppearanceDialog(
    current: ThemeMode,
    onDismiss: () -> Unit,
    onSelect: (ThemeMode) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        title = { Text("Appearance") },
        text = {
            Column {
                ThemeMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(mode) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = mode.displayName(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (mode == current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    )
}

private fun ThemeMode.displayName(): String = when (this) {
    ThemeMode.SYSTEM -> "System"
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
    ThemeMode.LIGHT_MEDIUM_CONTRAST -> "Light (Medium contrast)"
    ThemeMode.DARK_MEDIUM_CONTRAST -> "Dark (Medium contrast)"
    ThemeMode.LIGHT_HIGH_CONTRAST -> "Light (High contrast)"
    ThemeMode.DARK_HIGH_CONTRAST -> "Dark (High contrast)"
}


