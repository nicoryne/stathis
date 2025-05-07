package citu.edu.stathis.mobile.features.auth.domain.usecase

import citu.edu.stathis.mobile.features.auth.domain.model.AuthResult
import citu.edu.stathis.mobile.features.auth.domain.repository.IAuthRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val repository: IAuthRepository,
) {

    suspend operator fun invoke(email: String, password: String): AuthResult {
        return repository.signIn(email, password)
    }


    suspend fun refreshSession(): AuthResult {
        return repository.refreshSession()
    }
}