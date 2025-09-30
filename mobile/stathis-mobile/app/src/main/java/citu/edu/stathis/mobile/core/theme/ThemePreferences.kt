package citu.edu.stathis.mobile.core.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

/**
 * DataStore instance for theme preferences
 */
private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

/**
 * Keys for theme preferences
 */
private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
private val DYNAMIC_COLOR_KEY = booleanPreferencesKey("dynamic_color")
private val ONBOARDED_KEY = booleanPreferencesKey("onboarded")
private val ONBOARD_ROLE_KEY = stringPreferencesKey("onboard_role") // student|guest
private val ONBOARD_LEVEL_KEY = stringPreferencesKey("onboard_level") // 0|1|2 as string

/**
 * Theme preferences manager for persistent theme storage
 */
class ThemePreferences(private val context: Context) {
    
    /**
     * Get the current theme mode as a flow
     */
    val themeMode: Flow<ThemeMode> = context.themeDataStore.data.map { preferences ->
        val themeModeString = preferences[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(themeModeString)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }
    
    /**
     * Get the dynamic color preference as a flow
     */
    val dynamicColor: Flow<Boolean> = context.themeDataStore.data.map { preferences ->
        preferences[DYNAMIC_COLOR_KEY] ?: true
    }
    val onboarded: Flow<Boolean> = context.themeDataStore.data.map { preferences ->
        preferences[ONBOARDED_KEY] ?: false
    }
    val onboardRole: Flow<String> = context.themeDataStore.data.map { preferences ->
        preferences[ONBOARD_ROLE_KEY] ?: "guest"
    }
    val onboardLevel: Flow<Int> = context.themeDataStore.data.map { preferences ->
        (preferences[ONBOARD_LEVEL_KEY] ?: "0").toIntOrNull() ?: 0
    }
    
    /**
     * Set the theme mode
     */
    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.themeDataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = themeMode.name
        }
    }
    
    /**
     * Set the dynamic color preference
     */
    suspend fun setDynamicColor(enabled: Boolean) {
        context.themeDataStore.edit { preferences ->
            preferences[DYNAMIC_COLOR_KEY] = enabled
        }
    }

    suspend fun setOnboarded(onboarded: Boolean) {
        context.themeDataStore.edit { preferences ->
            preferences[ONBOARDED_KEY] = onboarded
        }
    }

    suspend fun setOnboardingRole(role: String) {
        context.themeDataStore.edit { preferences ->
            preferences[ONBOARD_ROLE_KEY] = role
        }
    }

    suspend fun setOnboardingLevel(level: Int) {
        context.themeDataStore.edit { preferences ->
            preferences[ONBOARD_LEVEL_KEY] = level.toString()
        }
    }
    
    /**
     * Get the current theme mode synchronously (for initial state)
     */
    suspend fun getCurrentThemeMode(): ThemeMode {
        val prefs = context.themeDataStore.data.first()
        val raw = prefs[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name
        return runCatching { ThemeMode.valueOf(raw) }.getOrElse { ThemeMode.SYSTEM }
    }
    
    /**
     * Get the current dynamic color preference synchronously (for initial state)
     */
    suspend fun getCurrentDynamicColor(): Boolean {
        val prefs = context.themeDataStore.data.first()
        return prefs[DYNAMIC_COLOR_KEY] ?: true
    }
}
