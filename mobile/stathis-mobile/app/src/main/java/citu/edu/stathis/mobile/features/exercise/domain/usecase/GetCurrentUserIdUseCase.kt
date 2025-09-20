package citu.edu.stathis.mobile.features.exercise.domain.usecase

import citu.edu.stathis.mobile.features.auth.data.repository.AuthRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetCurrentUserIdUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): String {
        // Our improved AuthTokenManager now guarantees this will never be null
        return authRepository.getCurrentUserId() ?: "default_user"
    }
}
