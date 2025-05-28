package citu.edu.stathis.mobile.features.vitals.domain.usecase

import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.vitals.data.repository.VitalsRepository // Correct repository import
import javax.inject.Inject

class DeleteVitalRecordUseCase @Inject constructor(
    private val vitalsRepository: VitalsRepository
) {
    suspend operator fun invoke(recordId: String): ClientResponse<Unit> {
        if (recordId.isBlank()) {
            return ClientResponse(success = false, message = "Record ID cannot be empty.", data = null)
        }
        return vitalsRepository.deleteVitalRecord(recordId)
    }
}