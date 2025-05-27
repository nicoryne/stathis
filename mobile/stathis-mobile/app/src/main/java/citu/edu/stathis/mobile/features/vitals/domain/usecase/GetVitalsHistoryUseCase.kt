package citu.edu.stathis.mobile.features.vitals.domain.usecase

import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.vitals.data.model.VitalSigns // Correct model import
import citu.edu.stathis.mobile.features.vitals.data.repository.VitalsRepository // Correct repository import
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetVitalsHistoryUseCase @Inject constructor(
    private val vitalsRepository: VitalsRepository
) {
    operator fun invoke(userId: String): Flow<ClientResponse<List<VitalSigns>>> {
        return vitalsRepository.getVitalsHistory(userId)
    }
}