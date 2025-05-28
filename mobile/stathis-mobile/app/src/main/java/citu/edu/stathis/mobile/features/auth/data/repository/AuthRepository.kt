package citu.edu.stathis.mobile.features.auth.data.repository

import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.auth.data.models.LoginResponse
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    suspend fun login(email: String, password: String): ClientResponse<LoginResponse>

    suspend fun register(email: String, password: String, firstName: String, lastName: String): ClientResponse<Unit>

    suspend fun logout(): ClientResponse<Unit>

    suspend fun refreshToken(currentRefreshToken: String): ClientResponse<Unit>

    suspend fun resendVerificationEmail(email: String): ClientResponse<Unit>

    fun isLoggedIn(): Flow<Boolean>
}