package citu.edu.stathis.mobile.features.vitals.domain.usecase

import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.vitals.data.model.VitalSigns // Correct model import
import citu.edu.stathis.mobile.features.vitals.data.repository.VitalsRepository // Correct repository import
import javax.inject.Inject

class SaveVitalsUseCase @Inject constructor(
    private val vitalsRepository: VitalsRepository
) {
    suspend operator fun invoke(vitalSigns: VitalSigns): ClientResponse<Unit> {
        // Here you could add logic for caching or deciding if it's time to upload.
        // For now, it directly saves.
        // Ensure the vitalSigns object has all necessary fields for the backend DTO,
        // like userId, and potentially classroomId, taskId if relevant for this save operation.
        return vitalsRepository.saveVitals(vitalSigns)
    }
}