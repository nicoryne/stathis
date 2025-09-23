package citu.edu.stathis.mobile.features.auth.domain.usecase

import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.auth.data.models.LoginResponse
import citu.edu.stathis.mobile.features.auth.data.repository.AuthRepository
import citu.edu.stathis.mobile.features.auth.ui.utils.EmailValidator
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): ClientResponse<LoginResponse> {
        if (email.isBlank() || password.isBlank()) {
            return ClientResponse(success = false, message = "Email and password cannot be empty.", data = null)
        }
        if (!EmailValidator.isValidEmail(email)) {
            return ClientResponse(success = false, message = "Invalid email format.", data = null)
        }
        return authRepository.login(email, password)
    }
}