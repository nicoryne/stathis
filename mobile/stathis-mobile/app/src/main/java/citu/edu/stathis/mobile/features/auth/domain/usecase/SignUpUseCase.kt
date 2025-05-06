package citu.edu.stathis.mobile.features.auth.domain.usecase

import citu.edu.stathis.mobile.features.auth.domain.model.AuthResult
import citu.edu.stathis.mobile.features.auth.domain.repository.IAuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val repository: IAuthRepository
) {
    suspend operator fun invoke(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ): AuthResult {
        return repository.signUp(firstName, lastName, email, password)
    }

    suspend fun resendVerificationEmail(email: String) {
        repository.resendVerificationEmail(email)
    }

}