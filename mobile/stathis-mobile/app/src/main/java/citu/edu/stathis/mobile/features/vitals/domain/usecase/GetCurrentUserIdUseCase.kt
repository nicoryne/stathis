package citu.edu.stathis.mobile.features.vitals.domain.usecase

import citu.edu.stathis.mobile.core.data.AuthTokenManager
import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.auth.data.models.UserResponseDTO
import citu.edu.stathis.mobile.features.auth.data.repository.AuthRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class GetCurrentUserIdUseCase @Inject constructor(
    private val authTokenManager: AuthTokenManager,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): String? {
        val storedPhysicalId = authTokenManager.physicalIdFlow.firstOrNull()
        if (!storedPhysicalId.isNullOrBlank()) {
            return storedPhysicalId
        }

        val profileResponse: ClientResponse<UserResponseDTO> = authRepository.getUserProfile()

        return if (profileResponse.success && profileResponse.data != null) {
            val physicalId = profileResponse.data.physicalId
            physicalId
        } else {
            null
        }
    }
}