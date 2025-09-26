package citu.edu.stathis.mobile.core.learn

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import citu.edu.stathis.mobile.features.onboarding.domain.model.ExperienceLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.learnDataStore: DataStore<Preferences> by preferencesDataStore(name = "learn_preferences")

class LearnPreferences(private val context: Context) {
    private val LEVEL_KEY = stringPreferencesKey("learn_experience_level")

    val levelFlow: Flow<ExperienceLevel> = context.learnDataStore.data.map { prefs ->
        val raw = prefs[LEVEL_KEY] ?: ExperienceLevel.BEGINNER.name
        runCatching { ExperienceLevel.valueOf(raw) }.getOrDefault(ExperienceLevel.BEGINNER)
    }

    suspend fun setLevel(level: ExperienceLevel) {
        context.learnDataStore.edit { prefs ->
            prefs[LEVEL_KEY] = level.name
        }
    }
}


