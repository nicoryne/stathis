package citu.edu.stathis.mobile.features.auth.data.repository

import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.auth.data.enums.UserRoles
import citu.edu.stathis.mobile.features.auth.data.models.LoginResponse
import citu.edu.stathis.mobile.features.auth.data.models.UserResponseDTO
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    suspend fun login(email: String, password: String): ClientResponse<LoginResponse>

    suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        userRole: UserRoles
    ): ClientResponse<Unit>

    suspend fun logout(): ClientResponse<Unit>

    suspend fun refreshToken(currentRefreshToken: String): ClientResponse<LoginResponse>

    suspend fun resendVerificationEmail(email: String): ClientResponse<Unit>

    fun isLoggedIn(): Flow<Boolean>

    suspend fun getCurrentUserId(): String?

    suspend fun getUserProfile(): ClientResponse<UserResponseDTO>
}