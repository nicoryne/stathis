package citu.edu.stathis.mobile.features.auth.domain.usecase

import android.util.Log
import citu.edu.stathis.mobile.core.auth.BiometricHelper
import citu.edu.stathis.mobile.features.auth.domain.model.AuthResult
import citu.edu.stathis.mobile.features.auth.domain.model.BiometricState
import citu.edu.stathis.mobile.features.auth.domain.repository.IAuthRepository
import javax.inject.Inject

class BiometricAuthUseCase @Inject constructor(
    private val repository: IAuthRepository,
) {

    suspend fun checkBiometricAvailability(): BiometricState {
        val isRefreshTokenValid = repository.isRefreshTokenValid()

        return when {
            !isRefreshTokenValid -> BiometricState.TokenExpired
            else -> BiometricState.Available
        }
    }

    suspend fun authenticateWithBiometrics(): AuthResult {
        return repository.refreshSession()
    }
}