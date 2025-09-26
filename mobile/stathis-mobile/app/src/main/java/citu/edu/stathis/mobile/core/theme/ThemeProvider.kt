package citu.edu.stathis.mobile.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Theme mode enumeration for different theme options
 */
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM,
    LIGHT_MEDIUM_CONTRAST,
    DARK_MEDIUM_CONTRAST,
    LIGHT_HIGH_CONTRAST,
    DARK_HIGH_CONTRAST
}

/**
 * Theme provider data class that holds current theme information
 */
data class ThemeProvider(
    val currentTheme: ThemeMode,
    val colorScheme: ColorScheme,
    val isDarkTheme: Boolean,
    val isDynamicColor: Boolean = false
)

/**
 * Local composition provider for theme context
 */
val LocalThemeProvider = compositionLocalOf<ThemeProvider> {
    error("No ThemeProvider found")
}

/**
 * Composable function to provide theme context to the composition tree
 */
@Composable
fun ProvideTheme(
    themeProvider: ThemeProvider,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalThemeProvider provides themeProvider) {
        content()
    }
}

/**
 * Composable function to access current theme provider
 */
@Composable
fun rememberThemeProvider(): ThemeProvider {
    return LocalThemeProvider.current
}

/**
 * Extension function to get color scheme based on theme mode
 */
fun getColorSchemeForTheme(
    themeMode: ThemeMode,
    isSystemDark: Boolean,
    dynamicColorScheme: ColorScheme? = null
): ColorScheme {
    return when (themeMode) {
        ThemeMode.SYSTEM -> {
            if (dynamicColorScheme != null) {
                dynamicColorScheme
            } else {
                if (isSystemDark) darkScheme else lightScheme
            }
        }
        ThemeMode.LIGHT -> lightScheme
        ThemeMode.DARK -> darkScheme
        ThemeMode.LIGHT_MEDIUM_CONTRAST -> mediumContrastLightColorScheme
        ThemeMode.DARK_MEDIUM_CONTRAST -> mediumContrastDarkColorScheme
        ThemeMode.LIGHT_HIGH_CONTRAST -> highContrastLightColorScheme
        ThemeMode.DARK_HIGH_CONTRAST -> highContrastDarkColorScheme
    }
}

/**
 * Extension function to determine if theme is dark based on theme mode
 */
fun isDarkThemeForMode(themeMode: ThemeMode, isSystemDark: Boolean): Boolean {
    return when (themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.DARK, ThemeMode.DARK_MEDIUM_CONTRAST, ThemeMode.DARK_HIGH_CONTRAST -> true
        ThemeMode.LIGHT, ThemeMode.LIGHT_MEDIUM_CONTRAST, ThemeMode.LIGHT_HIGH_CONTRAST -> false
    }
}

/**
 * Extension function to check if dynamic color is enabled
 */
fun isDynamicColorEnabled(themeMode: ThemeMode, dynamicColorScheme: ColorScheme?): Boolean {
    return themeMode == ThemeMode.SYSTEM && dynamicColorScheme != null
}
