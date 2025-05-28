package citu.edu.stathis.mobile.features.auth.data.repository

import citu.edu.stathis.mobile.core.data.AuthTokenManager
import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.auth.data.enums.UserRoles
import citu.edu.stathis.mobile.features.auth.data.models.LoginRequest
import citu.edu.stathis.mobile.features.auth.data.models.LoginResponse
import citu.edu.stathis.mobile.features.auth.data.models.RegisterRequest
import citu.edu.stathis.mobile.features.auth.data.models.UserResponseDTO
import citu.edu.stathis.mobile.features.auth.domain.AuthApiService
import com.auth0.android.jwt.JWT
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
            val response: LoginResponse = authApiService.login(loginRequest)

            val jwt = JWT(response.accessToken)
            val roleString = jwt.getClaim("role").asString()
            val roleName = roleString?.removePrefix("ROLE_")
            val userRole = roleName?.let { UserRoles.valueOf(it.uppercase()) }

            if (userRole == UserRoles.TEACHER) {
                ClientResponse(success = false, message = "Teacher accounts are not permitted on this mobile application.", data = null)
            } else if (userRole != null) {
                authTokenManager.saveSessionTokensAndRole(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken,
                    role = userRole
                )
                ClientResponse(success = true, message = "Login successful. User details will be fetched.", data = response)
            } else {
                ClientResponse(success = false, message = "Could not determine user role from token.", data = null)
            }
        } catch (e: HttpException) {
            ClientResponse(success = false, message = e.message() ?: "Login failed.", data = null)
        } catch (e: IOException) {
            ClientResponse(success = false, message = "Network error during login.", data = null)
        } catch (e: Exception) {
            ClientResponse(success = false, message = e.message ?: "An unknown login error occurred.", data = null)
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        userRole: UserRoles
    ): ClientResponse<Unit> {
        return try {
            val registerRequest = RegisterRequest(
                email = email,
                password = password,
                firstName = firstName,
                lastName = lastName,
                userRole = userRole
            )
            authApiService.register(registerRequest)
            ClientResponse(success = true, message = "Registration successful. Please check your email for verification.", data = Unit)
        } catch (e: HttpException) {
            ClientResponse(success = false, message = e.message() ?: "Registration failed.", data = null)
        } catch (e: IOException) {
            ClientResponse(success = false, message = "Network error during registration.", data = null)
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
            authTokenManager.clearAuthData()
            ClientResponse(success = true, message = "Logout successful.", data = Unit)
        } catch (e: Exception) {
            authTokenManager.clearAuthData()
            ClientResponse(success = true, message = "Logged out locally. Server communication may have failed.", data = Unit)
        }
    }

    override suspend fun refreshToken(currentRefreshToken: String): ClientResponse<LoginResponse> {
        return try {
            val response = authApiService.refresh(currentRefreshToken)

            val jwt = JWT(response.accessToken)
            val roleString = jwt.getClaim("role").asString()
            val roleName = roleString?.removePrefix("ROLE_")
            val userRole = roleName?.let { UserRoles.valueOf(it.uppercase()) }

            if (userRole == UserRoles.TEACHER) {
                authTokenManager.clearAuthData()
                ClientResponse(success = false, message = "Teacher accounts are not permitted. Session terminated.", data = null)
            } else if (userRole != null) {
                authTokenManager.saveSessionTokensAndRole(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken,
                    role = userRole
                )
                ClientResponse(success = true, message = "Token refresh successful. User details will be fetched/updated.", data = response)
            } else {
                ClientResponse(success = false, message = "Could not determine user role from token during refresh.", data = null)
            }
        } catch (e: HttpException) {
            ClientResponse(success = false, message = e.message() ?: "Token refresh failed.", data = null)
        } catch (e: IOException) {
            ClientResponse(success = false, message = "Network error during token refresh.", data = null)
        } catch (e: Exception) {
            ClientResponse(success = false, message = e.message ?: "An unknown error occurred during token refresh.", data = null)
        }
    }

    override suspend fun resendVerificationEmail(email: String): ClientResponse<Unit> {
        return try {
            authApiService.resendVerificationEmail(email)
            ClientResponse(success = true, message = "Verification email resent successfully.", data = Unit)
        } catch (e: HttpException) {
            ClientResponse(success = false, message = e.message() ?: "Failed to resend verification email.", data = null)
        } catch (e: IOException) {
            ClientResponse(success = false, message = "Network error. Could not resend email.", data = null)
        } catch (e: Exception) {
            ClientResponse(success = false, message = e.message ?: "An unknown error occurred while resending email.", data = null)
        }
    }

    override fun isLoggedIn(): Flow<Boolean> {
        return authTokenManager.isLoggedInFlow
    }

    override suspend fun getCurrentUserId(): String? {
        return authTokenManager.physicalIdFlow.firstOrNull()
    }

    override suspend fun getUserProfile(): ClientResponse<UserResponseDTO> {
        return try {
            val accessToken = authTokenManager.accessTokenFlow.firstOrNull()
            if (accessToken.isNullOrBlank()) {
                return ClientResponse(success = false, message = "User not authenticated to fetch profile.", data = null)
            }

            val jwt = JWT(accessToken)
            val roleString = jwt.getClaim("role").asString()
            val roleName = roleString?.removePrefix("ROLE_")
            val userRole = roleName?.let { UserRoles.valueOf(it.uppercase()) }

            if (userRole == null) {
                return ClientResponse(success = false, message = "Could not determine user role from token for profile fetch.", data = null)
            }

            if (userRole == UserRoles.TEACHER) {
                return ClientResponse(success = false, message = "Teacher profiles are not handled by this mobile application.", data = null)
            }

            val userProfileDto: UserResponseDTO = when (userRole) {
                UserRoles.STUDENT -> authApiService.getStudentProfile()
                UserRoles.GUEST_USER -> return ClientResponse(success = false, message = "Profile not available for guest user.", data = null)
                else -> return ClientResponse(success = false, message = "Unsupported user role for profile.", data = null)
            }

            authTokenManager.updateUserIdentity(
                physicalId = userProfileDto.physicalId,
                role = userProfileDto.role
            )
            ClientResponse(success = true, message = "Profile fetched successfully.", data = userProfileDto)

        } catch (e: HttpException) {
            ClientResponse(success = false, message = e.message() ?: "Failed to fetch profile (HTTP error).", data = null)
        } catch (e: IOException) {
            ClientResponse(success = false, message = "Network error while fetching profile.", data = null)
        } catch (e: Exception) {
            ClientResponse(success = false, message = e.message ?: "An unknown error occurred while fetching profile.", data = null)
        }
    }
}