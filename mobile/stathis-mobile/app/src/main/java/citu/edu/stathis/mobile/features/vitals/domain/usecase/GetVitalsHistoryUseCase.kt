package citu.edu.stathis.mobile.features.vitals.domain.usecase

import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.vitals.data.model.VitalSigns
import citu.edu.stathis.mobile.features.vitals.data.repository.VitalsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetVitalsHistoryUseCase @Inject constructor(
    private val vitalsRepository: VitalsRepository,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) {
    suspend operator fun invoke(): Flow<ClientResponse<List<VitalSigns>>>? {
        val userId = getCurrentUserIdUseCase() ?: return null
        return vitalsRepository.getVitalsHistory(userId)
    }
}