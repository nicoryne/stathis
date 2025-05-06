package citu.edu.stathis.mobile.features.auth.data.repository

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
import io.github.jan.supabase.auth.providers.builtin.OTP
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val authTokenManager: AuthTokenManager
) : IAuthRepository {

    override suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            val response = supabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

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

            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Unknown error occurred")
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

            // Check if user was created successfully
            val user = supabaseClient.auth.currentUserOrNull()
            if (user != null) {
                AuthResult.Success
            } else {
                AuthResult.Error("Failed to create account")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    override suspend fun signInWithGoogle(): AuthResult {
        return try {
            supabaseClient.auth.signInWith(Google)
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Google sign-in failed")
        }
    }

    override suspend fun signInWithMicrosoft(): AuthResult {
        return try {
            supabaseClient.auth.signInWith(Azure)
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Microsoft sign-in failed")
        }
    }

    override suspend fun resetPassword(email: String): AuthResult {
        return try {
            supabaseClient.auth.resetPasswordForEmail(email)
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to send password reset email")
        }
    }

    override suspend fun signOut(): AuthResult {
        return try {
            supabaseClient.auth.signOut(SignOutScope.LOCAL)
            authTokenManager.clearTokens()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Sign out failed")
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
            AuthResult.Error(e.message ?: "Failed to resend verification email.")
        }
    }
}