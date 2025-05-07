package citu.edu.stathis.mobile.features.auth.data.repository

import android.util.Log
import citu.edu.stathis.mobile.features.auth.domain.model.AuthResult
import citu.edu.stathis.mobile.features.auth.domain.model.UserData
import citu.edu.stathis.mobile.features.auth.domain.repository.IAuthRepository
import citu.edu.stathis.mobile.core.data.AuthTokenManager
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.SignOutScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.Azure
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.sql.Date
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val authTokenManager: AuthTokenManager
) : IAuthRepository {

    private val _authState = MutableStateFlow(false)

    init {
        _authState.value = supabaseClient.auth.currentSessionOrNull() != null
    }

    override suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            val response = supabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            if(verifyRoleOfCurrentUser()) {
                val session = supabaseClient.auth.currentSessionOrNull()
                if (session != null) {
                    authTokenManager.saveTokens(
                        accessToken = session.accessToken,
                        refreshToken = session.refreshToken
                    )
                }

                // Get the current user after sign in
                val user = supabaseClient.auth.currentUserOrNull()
                if (user != null) {
                    authTokenManager.saveUserInfo(
                        userId = user.id,
                        email = user.email ?: ""
                    )
                }

                _authState.value = true
                AuthResult.Success
            } else {
                signOut()
                AuthResult.Error("That's a TEACHER account! Try logging in with something else.")
            }
        } catch (e: Exception) {
            AuthResult.Error("Whoops! Something went wrong. Maybe check your email or password.")
        }
    }

    override suspend fun signUp(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ): AuthResult {
        return try {
            val userData = buildJsonObject {
                put("first_name", firstName)
                put("last_name", lastName)
                put("user_role", "student")
            }

            val response = supabaseClient.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                this.data = userData
            }

            AuthResult.Success
        } catch (e: Exception) {
            val message = e.message;

            if(message.isNullOrEmpty()) {
                AuthResult.Error("Whoops! Something went wrong.")
            } else {
                AuthResult.Error("Woah there! This user already exists.")
            }
        }
    }

    override suspend fun signInWithGoogle(): AuthResult {
        return try {
            supabaseClient.auth.signInWith(Google)
            if(verifyRoleOfCurrentUser()) {
                AuthResult.Success
            } else {
                signOut()
                AuthResult.Error("That's a TEACHER account! Try logging in with something else.")
            }
        } catch (e: Exception) {
            AuthResult.Error("Whoops! Something went wrong.")
        }
    }

    override suspend fun signInWithMicrosoft(): AuthResult {
        return try {
            supabaseClient.auth.signInWith(Azure)

            if(verifyRoleOfCurrentUser()) {
                AuthResult.Success
            } else {
                signOut()
                AuthResult.Error("That's a TEACHER account! Try logging in with something else.")
            }
        } catch (e: Exception) {
            AuthResult.Error("Whoops! Something went wrong.")
        }
    }

    override suspend fun resetPassword(email: String): AuthResult {
        return try {
            supabaseClient.auth.resetPasswordForEmail(email)
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error("Whoops! Something went wrong.")
        }
    }

    override suspend fun signOut(): AuthResult {
        return try {
            supabaseClient.auth.signOut(SignOutScope.LOCAL)
            authTokenManager.clearTokens()

            _authState.value = false
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error("Whoops! Something went wrong.")
        }
    }

    override fun isLoggedIn(): Flow<Boolean> {
        return authTokenManager.isLoggedInFlow
    }

    override suspend fun getCurrentUser(): UserData? {
        val user = supabaseClient.auth.currentUserOrNull() ?: return null

        return UserData(
            id = user.id,
            email = user.email ?: "",
            firstName = user.userMetadata?.get("first_name")?.toString() ?: "",
            lastName = user.userMetadata?.get("last_name")?.toString() ?: ""
        )
    }

    override suspend fun resendVerificationEmail(email: String): AuthResult {
        return try {
            supabaseClient.auth.resendEmail(OtpType.Email.SIGNUP, email = email)
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error("Whoops! Something went wrong.")
        }
    }

    override suspend fun verifyRoleOfCurrentUser(): Boolean {
        val user = supabaseClient.auth.currentUserOrNull() ?: return false

        if (user.userMetadata?.containsKey("user_role") == true) {
            val role = user.userMetadata!!["user_role"]?.jsonPrimitive?.content
            return role.equals("student")
        }

        return false
    }

    override suspend fun isRefreshTokenValid(): Boolean {
        val refreshToken = authTokenManager.refreshTokenFlow.firstOrNull() ?: return false

        Log.d("AuthRepository", "Refresh Token: ${refreshToken}")

        return refreshToken.isNotBlank()
    }

    override suspend fun refreshSession(): AuthResult {
        val refreshToken = authTokenManager.refreshTokenFlow.firstOrNull()
            ?: return AuthResult.Error("No refresh token found")

        return try {
            supabaseClient.auth.refreshSession(refreshToken)
            _authState.value = true
            AuthResult.Success
        } catch (e: Exception) {
            _authState.value = false
            AuthResult.Error(e.message ?: "Session refresh failed")
        }
    }
}