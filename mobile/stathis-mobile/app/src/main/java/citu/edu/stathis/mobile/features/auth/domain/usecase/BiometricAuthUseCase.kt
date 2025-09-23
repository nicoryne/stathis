package citu.edu.stathis.mobile.features.auth.domain.usecase

import android.content.Context
import androidx.biometric.BiometricManager
import citu.edu.stathis.mobile.core.data.AuthTokenManager
import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.auth.data.models.BiometricState
import citu.edu.stathis.mobile.features.auth.data.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import cit.edu.stathis.mobile.BuildConfig

class BiometricAuthUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val authTokenManager: AuthTokenManager
) {
    suspend fun hasRefreshToken(): Boolean {
        return authTokenManager.refreshTokenFlow.firstOrNull().isNullOrBlank().not()
    }

    suspend fun canPromptBiometrics(): Boolean {
        if (BuildConfig.BYPASS_AUTH) return false
        val availability = checkBiometricAvailability()
        val ok = availability == BiometricState.Available
        val hasToken = hasRefreshToken()
        return ok && hasToken
    }
    fun checkBiometricAvailability(): BiometricState {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricState.Available
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricState.NotAvailable
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricState.NotAvailable
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricState.NotEnrolled
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricState.Error("Security update required for biometrics.")
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricState.Error("Biometrics are unsupported on this device.")
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> BiometricState.Error("Biometric status is unknown.")
            else -> BiometricState.Error("An unknown biometric availability error occurred.")
        }
    }


    suspend fun authenticateWithBiometrics(): ClientResponse<Unit> {
        val refreshToken = authTokenManager.refreshTokenFlow.firstOrNull()

        if (refreshToken.isNullOrBlank()) {
            return ClientResponse(
                success = false,
                message = "No active session found for biometric login. Please log in with your credentials.",
                data = null
            )
        }

        val refreshResult = authRepository.refreshToken(refreshToken)

        return if (refreshResult.success) {
            ClientResponse(success = true, message = "Biometric authentication successful. Session validated.", data = Unit)
        } else {
            ClientResponse(
                success = false,
                message = "Biometric authentication succeeded, but session validation failed: ${refreshResult.message} Please log in with your credentials.",
                data = null
            )
        }
    }
}