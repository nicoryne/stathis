package citu.edu.stathis.mobile.core.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing theme preferences and state
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themePreferences: ThemePreferences
) : ViewModel() {

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isDynamicColorEnabled = MutableStateFlow(true)
    val isDynamicColorEnabled: StateFlow<Boolean> = _isDynamicColorEnabled.asStateFlow()

    init {
        // Observe theme preferences
        viewModelScope.launch {
            themePreferences.isDarkMode.collect { isDark ->
                _isDarkMode.value = isDark
            }
        }

        viewModelScope.launch {
            themePreferences.isDynamicColorEnabled.collect { isEnabled ->
                _isDynamicColorEnabled.value = isEnabled
            }
        }
    }

    /**
     * Toggle between light and dark mode
     */
    fun toggleTheme() {
        viewModelScope.launch {
            themePreferences.toggleTheme()
        }
    }

    /**
     * Set specific theme mode
     */
    fun setDarkMode(isDarkMode: Boolean) {
        viewModelScope.launch {
            themePreferences.setDarkMode(isDarkMode)
        }
    }

    /**
     * Toggle dynamic color
     */
    fun toggleDynamicColor() {
        viewModelScope.launch {
            val currentValue = _isDynamicColorEnabled.value
            themePreferences.setDynamicColorEnabled(!currentValue)
        }
    }

    /**
     * Set dynamic color preference
     */
    fun setDynamicColorEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            themePreferences.setDynamicColorEnabled(isEnabled)
        }
    }
}


