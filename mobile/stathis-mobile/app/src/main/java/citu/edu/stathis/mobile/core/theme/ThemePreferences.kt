package citu.edu.stathis.mobile.core.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Theme preferences for managing light/dark mode switching
 */
@Singleton
class ThemePreferences @Inject constructor(
    private val context: Context
) {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")
        private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        private val DYNAMIC_COLOR_ENABLED = booleanPreferencesKey("dynamic_color_enabled")
    }

    /**
     * Get the current theme mode preference
     */
    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_DARK_MODE] ?: false
    }

    /**
     * Get the dynamic color preference
     */
    val isDynamicColorEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DYNAMIC_COLOR_ENABLED] ?: true
    }

    /**
     * Set the theme mode preference
     */
    suspend fun setDarkMode(isDarkMode: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = isDarkMode
        }
    }

    /**
     * Set the dynamic color preference
     */
    suspend fun setDynamicColorEnabled(isEnabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DYNAMIC_COLOR_ENABLED] = isEnabled
        }
    }

    /**
     * Toggle between light and dark mode
     */
    suspend fun toggleTheme() {
        context.dataStore.edit { preferences ->
            val currentMode = preferences[IS_DARK_MODE] ?: false
            preferences[IS_DARK_MODE] = !currentMode
        }
    }
}


