package citu.edu.stathis.mobile.features.auth.domain.usecase

import citu.edu.stathis.mobile.features.auth.domain.model.AuthResult
import citu.edu.stathis.mobile.features.auth.domain.repository.IAuthRepository
import javax.inject.Inject

class SocialSignInUseCase @Inject constructor(
    private val repository: IAuthRepository
) {
    suspend fun signInWithGoogle(): AuthResult {
        return repository.signInWithGoogle()
    }

    suspend fun signInWithMicrosoft(): AuthResult {
        return repository.signInWithMicrosoft()
    }
}