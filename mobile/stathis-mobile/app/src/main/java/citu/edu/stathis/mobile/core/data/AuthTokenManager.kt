package citu.edu.stathis.mobile.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import citu.edu.stathis.mobile.features.auth.data.enums.UserRoles
import citu.edu.stathis.mobile.features.auth.domain.usecase.TokenValidationUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class AuthTokenManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenValidationUseCase: TokenValidationUseCase
) {
    private val dataStore = context.dataStore

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
        private val PHYSICAL_ID_KEY = stringPreferencesKey("physical_id")
        private val USER_ROLE_KEY = stringPreferencesKey("user_role")
    }

    val accessTokenFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN_KEY]
    }

    val refreshTokenFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[REFRESH_TOKEN_KEY]
    }

    val isLoggedInFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        val accessToken = preferences[ACCESS_TOKEN_KEY]
        val isLoggedIn = preferences[IS_LOGGED_IN_KEY] ?: false
        isLoggedIn && !tokenValidationUseCase.isTokenExpired(accessToken)
    }

    val physicalIdFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PHYSICAL_ID_KEY]
    }

    val userRoleFlow: Flow<UserRoles?> = dataStore.data.map { preferences ->
        preferences[USER_ROLE_KEY]?.let { UserRoles.valueOf(it.uppercase()) }
    }

    suspend fun saveSessionTokensAndRole(
        accessToken: String,
        refreshToken: String,
        role: UserRoles
    ) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
            preferences[USER_ROLE_KEY] = role.name
            preferences[IS_LOGGED_IN_KEY] = true
        }
    }

    suspend fun updateUserIdentity(physicalId: String, role: UserRoles) {
        dataStore.edit { preferences ->
            preferences[PHYSICAL_ID_KEY] = physicalId
            preferences[USER_ROLE_KEY] = role.name
            if (preferences[IS_LOGGED_IN_KEY] != true) {
                preferences[IS_LOGGED_IN_KEY] = !preferences[ACCESS_TOKEN_KEY].isNullOrBlank()
            }
        }
    }

    suspend fun clearAuthData() {
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
            preferences.remove(PHYSICAL_ID_KEY)
            preferences.remove(USER_ROLE_KEY)
            preferences[IS_LOGGED_IN_KEY] = false
        }
    }

    suspend fun checkAndHandleTokenExpiration() {
        val accessToken = dataStore.data.map { it[ACCESS_TOKEN_KEY] }.firstOrNull()
        if (tokenValidationUseCase.isTokenExpired(accessToken)) {
            clearAuthData()
        }
    }
}