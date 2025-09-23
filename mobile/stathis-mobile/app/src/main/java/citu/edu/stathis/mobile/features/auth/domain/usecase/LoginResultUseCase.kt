package citu.edu.stathis.mobile.features.auth.domain.usecase

import citu.edu.stathis.mobile.features.auth.data.models.LoginResponse
import citu.edu.stathis.mobile.features.auth.data.repository.AuthRepository
import citu.edu.stathis.mobile.features.common.domain.Result
import citu.edu.stathis.mobile.features.common.domain.toResult
import citu.edu.stathis.mobile.features.auth.ui.utils.EmailValidator
import cit.edu.stathis.mobile.BuildConfig
import javax.inject.Inject

class LoginResultUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<LoginResponse> {
        if (BuildConfig.BYPASS_AUTH) {
            return Result.Success(LoginResponse(accessToken = "debug", refreshToken = "debug"))
        }
        if (email.isBlank() || password.isBlank()) {
            return Result.Error("Email and password cannot be empty.")
        }
        if (!EmailValidator.isValidEmail(email)) {
            return Result.Error("Invalid email format.")
        }
        return authRepository.login(email, password).toResult()
    }
}


