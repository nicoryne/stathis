package citu.edu.stathis.mobile.features.auth.domain.usecase

import citu.edu.stathis.mobile.core.data.AuthTokenManager
import citu.edu.stathis.mobile.features.auth.data.repository.AuthRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import cit.edu.stathis.mobile.BuildConfig

class HandleAuthStateUseCase @Inject constructor(
    private val authTokenManager: AuthTokenManager,
    private val authRepository: AuthRepository,
    private val tokenValidationUseCase: TokenValidationUseCase
) {
    suspend fun execute(): AuthState {
        val accessToken = authTokenManager.accessTokenFlow.first()
        val refreshToken = authTokenManager.refreshTokenFlow.first()
        
        // If no tokens exist, user needs to login
        if (accessToken.isNullOrBlank() || refreshToken.isNullOrBlank()) {
            return AuthState.NEEDS_LOGIN
        }

        // Check if access token is expired
        if (tokenValidationUseCase.isTokenExpired(accessToken)) {
            // Try to refresh the token
            val refreshResult = authRepository.refreshToken(refreshToken)
            return if (refreshResult.success) {
                AuthState.AUTHENTICATED
            } else {
                authTokenManager.clearAuthData()
                AuthState.NEEDS_LOGIN
            }
        }

        return AuthState.AUTHENTICATED
    }
}

enum class AuthState {
    NEEDS_LOGIN,
    AUTHENTICATED
} 