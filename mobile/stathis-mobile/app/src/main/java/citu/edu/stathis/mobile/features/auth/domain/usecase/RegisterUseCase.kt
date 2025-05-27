package citu.edu.stathis.mobile.features.auth.domain.usecase

import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.auth.data.repository.AuthRepository
import citu.edu.stathis.mobile.features.auth.ui.utils.EmailValidator
import citu.edu.stathis.mobile.features.auth.ui.utils.PasswordValidator // Assuming this util exists
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String,
    ): ClientResponse<Unit> {
        if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank()) {
            return ClientResponse(success = false, message = "All fields are required.", data = null)
        }
        if (!EmailValidator.isValidEmail(email)) {
            return ClientResponse(success = false, message = "Invalid email format.", data = null)
        }
        if (!PasswordValidator.isValidPassword(password)) {
            return ClientResponse(success = false, message = "Password does not meet requirements.", data = null)
        }
        if (!PasswordValidator.doPasswordsMatch(password, confirmPassword)) {
            return ClientResponse(success = false, message = "Passwords do not match.", data = null)
        }

        return authRepository.register(
            email = email,
            password = password,
            firstName = firstName,
            lastName = lastName,
        )
    }

    suspend fun resendVerificationEmail(email: String): ClientResponse<Unit> {
        if (!EmailValidator.isValidEmail(email)) {
            return ClientResponse(success = false, message = "Invalid email format.", data = null)
        }
        return authRepository.resendVerificationEmail(email)
    }
}