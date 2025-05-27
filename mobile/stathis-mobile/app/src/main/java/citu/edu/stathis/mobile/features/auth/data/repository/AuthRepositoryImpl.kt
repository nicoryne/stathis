package citu.edu.stathis.mobile.features.auth.data.repository

import citu.edu.stathis.mobile.core.data.AuthTokenManager
import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.auth.data.enums.UserRoles
import citu.edu.stathis.mobile.features.auth.data.models.LoginRequest
import citu.edu.stathis.mobile.features.auth.data.models.LoginResponse
import citu.edu.stathis.mobile.features.auth.data.models.RegisterRequest
import citu.edu.stathis.mobile.features.auth.domain.AuthApiService
import citu.edu.stathis.mobile.features.auth.data.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val authTokenManager: AuthTokenManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): ClientResponse<LoginResponse> {
        return try {
            val loginRequest = LoginRequest(email = email, password = password)
            val response = authApiService.login(loginRequest)
            authTokenManager.saveTokens(response.accessToken, response.refreshToken)
            ClientResponse(success = true, message = "Login successful.", data = response)
        } catch (e: HttpException) {
            ClientResponse(success = false, message = e.message() ?: "Login failed. Please check credentials.", data = null)
        } catch (e: IOException) {
            ClientResponse(success = false, message = "Network error during login. Please try again.", data = null)
        } catch (e: Exception) {
            ClientResponse(success = false, message = e.message ?: "An unknown login error occurred.", data = null)
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
    ): ClientResponse<Unit> {
        return try {
            val registerRequest = RegisterRequest(
                email = email, password = password, firstName = firstName,
                lastName = lastName, userRole = UserRoles.GUEST_USER
            )
            authApiService.register(registerRequest)
            ClientResponse(success = true, message = "Registration successful. Please check your email for verification.", data = Unit)
        } catch (e: HttpException) {
            ClientResponse(success = false, message = e.message() ?: "Registration failed. Email may already be in use.", data = null)
        } catch (e: IOException) {
            ClientResponse(success = false, message = "Network error during registration. Please try again.", data = null)
        } catch (e: Exception) {
            ClientResponse(success = false, message = e.message ?: "An unknown registration error occurred.", data = null)
        }
    }

    override suspend fun logout(): ClientResponse<Unit> {
        return try {
            val refreshToken = authTokenManager.refreshTokenFlow.firstOrNull()
            if (!refreshToken.isNullOrBlank()) {
                authApiService.logout(refreshToken)
            }
            authTokenManager.clearTokens()
            ClientResponse(success = true, message = "Logout successful.", data = Unit)
        } catch (e: Exception) {
            authTokenManager.clearTokens()
            ClientResponse(success = true, message = "Logged out locally. Server communication may have failed.", data = Unit)
        }
    }

    override suspend fun refreshToken(currentRefreshToken: String): ClientResponse<Unit> {
        return try {
            authApiService.refresh(currentRefreshToken)
            ClientResponse(success = true, message = "Session refresh attempt successful.", data = Unit)
        } catch (e: HttpException) {
            ClientResponse(success = false, message = e.message() ?: "Session refresh failed.", data = null)
        } catch (e: IOException) {
            ClientResponse(success = false, message = "Network error during session refresh. Please try again.", data = null)
        } catch (e: Exception) {
            ClientResponse(success = false, message = e.message ?: "An unknown error occurred during session refresh.", data = null)
        }
    }

    override suspend fun resendVerificationEmail(email: String): ClientResponse<Unit> {
        return try {
            authApiService.resendVerificationEmail(email)
            ClientResponse(success = true, message = "Verification email resent successfully.", data = Unit)
        } catch (e: HttpException) {
            ClientResponse(success = false, message = e.message() ?: "Failed to resend verification email.", data = null)
        } catch (e: IOException) {
            ClientResponse(success = false, message = "Network error. Could not resend verification email.", data = null)
        } catch (e: Exception) {
            ClientResponse(success = false, message = e.message ?: "An unknown error occurred while resending email.", data = null)
        }
    }

    override fun isLoggedIn(): Flow<Boolean> {
        return authTokenManager.isLoggedInFlow
    }
}