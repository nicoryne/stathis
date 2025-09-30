package citu.edu.stathis.mobile.core.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import android.util.Log
import androidx.compose.ui.graphics.toArgb

/**
 * Enhanced AppTheme composable that integrates with ThemeProvider
 * This is the main theme composable that should be used in the app
 */
@Composable
fun AppThemeWithProvider(
    themeViewModel: ThemeViewModel = viewModel(),
    content: @Composable () -> Unit
) {
    val currentTheme by themeViewModel.currentTheme.collectAsState()
    val dynamicColor by themeViewModel.dynamicColor.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    
    val colorScheme = getColorSchemeForTheme(currentTheme, isSystemDark)
    val themeProvider = ThemeProvider(
        currentTheme = currentTheme,
        colorScheme = colorScheme,
        isDarkTheme = isDarkThemeForMode(currentTheme, isSystemDark),
        isDynamicColor = false
    )
    Log.d(
        "AppTheme",
        "currentTheme=${currentTheme} primary=#${Integer.toHexString(colorScheme.primary.toArgb())} surface=#${Integer.toHexString(colorScheme.surface.toArgb())}"
    )
    
    // Provide theme context to the composition tree
    ProvideTheme(themeProvider = themeProvider) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}

/**
 * Legacy AppTheme composable for backward compatibility
 * This maintains the original behavior while using the new theme system
 */
@Composable
fun LegacyAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkScheme
        else -> lightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}

/**
 * Convenience composable for accessing theme colors
 */
@Composable
fun StathisTheme(
    content: @Composable () -> Unit
) {
    AppThemeWithProvider(content = content)
}
