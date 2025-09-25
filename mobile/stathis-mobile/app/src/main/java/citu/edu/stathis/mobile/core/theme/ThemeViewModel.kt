package citu.edu.stathis.mobile.core.theme

import android.app.Application
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing theme state and preferences
 */
class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val themePreferences = ThemePreferences(application)
    
    // Current theme state
    private val _currentTheme = MutableStateFlow(ThemeMode.DARK)
    val currentTheme: StateFlow<ThemeMode> = _currentTheme.asStateFlow()
    
    private val _dynamicColor = MutableStateFlow(false)
    val dynamicColor: StateFlow<Boolean> = _dynamicColor.asStateFlow()
    
    // Dynamic color scheme state
    private var _dynamicColorScheme by mutableStateOf<androidx.compose.material3.ColorScheme?>(null)
    val dynamicColorScheme: androidx.compose.material3.ColorScheme?
        get() = _dynamicColorScheme
    
    init {
        // Load initial theme preferences
        viewModelScope.launch {
            themePreferences.themeMode.collect { themeMode ->
                _currentTheme.value = themeMode
            }
        }
        
        viewModelScope.launch {
            themePreferences.dynamicColor.collect { dynamicColor ->
                _dynamicColor.value = dynamicColor
            }
        }
    }
    
    /**
     * Set the theme mode and persist it
     */
    fun setThemeMode(themeMode: ThemeMode) {
        _currentTheme.value = themeMode
        viewModelScope.launch {
            themePreferences.setThemeMode(themeMode)
        }
    }
    
    /**
     * Toggle between light and dark theme
     */
    fun toggleTheme() {
        val newTheme = when (_currentTheme.value) {
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.LIGHT_MEDIUM_CONTRAST -> ThemeMode.DARK_MEDIUM_CONTRAST
            ThemeMode.DARK_MEDIUM_CONTRAST -> ThemeMode.LIGHT_MEDIUM_CONTRAST
            ThemeMode.LIGHT_HIGH_CONTRAST -> ThemeMode.DARK_HIGH_CONTRAST
            ThemeMode.DARK_HIGH_CONTRAST -> ThemeMode.LIGHT_HIGH_CONTRAST
            ThemeMode.SYSTEM -> ThemeMode.LIGHT // Default to light when toggling from system
        }
        setThemeMode(newTheme)
    }
    
    /**
     * Set dynamic color preference
     */
    fun setDynamicColor(enabled: Boolean) {
        _dynamicColor.value = enabled
        viewModelScope.launch {
            themePreferences.setDynamicColor(enabled)
        }
    }
    
    /**
     * Update dynamic color scheme (called from composable)
     */
    fun updateDynamicColorScheme(scheme: androidx.compose.material3.ColorScheme?) {
        _dynamicColorScheme = scheme
    }
    
    /**
     * Get the current color scheme based on theme mode and system settings
     */
    @Composable
    fun getCurrentColorScheme(): androidx.compose.material3.ColorScheme {
        val isSystemDark = isSystemInDarkTheme()
        val currentThemeMode = _currentTheme.value
        updateDynamicColorScheme(null)
        return getColorSchemeForTheme(currentThemeMode, isSystemDark, null)
    }
    
    /**
     * Check if current theme is dark
     */
    @Composable
    fun isDarkTheme(): Boolean {
        val isSystemDark = isSystemInDarkTheme()
        return isDarkThemeForMode(_currentTheme.value, isSystemDark)
    }
    
    /**
     * Get theme provider for composition
     */
    @Composable
    fun getThemeProvider(): ThemeProvider {
        val colorScheme = getCurrentColorScheme()
        val isDark = isDarkTheme()
        val isDynamic = isDynamicColorEnabled(_currentTheme.value, _dynamicColorScheme)
        
        return ThemeProvider(
            currentTheme = _currentTheme.value,
            colorScheme = colorScheme,
            isDarkTheme = isDark,
            isDynamicColor = isDynamic
        )
    }
}
