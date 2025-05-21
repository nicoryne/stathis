package citu.edu.stathis.mobile.features.auth.domain.usecase


import citu.edu.stathis.mobile.features.auth.domain.model.AuthResult
import citu.edu.stathis.mobile.features.auth.domain.repository.IAuthRepository
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val repository: IAuthRepository
) {
    suspend operator fun invoke(email: String): AuthResult {
        return repository.resetPassword(email)
    }
}