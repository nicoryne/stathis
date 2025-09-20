package citu.edu.stathis.mobile.features.progress.domain.usecase

import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.progress.data.model.ClassroomProgressSummary
import citu.edu.stathis.mobile.features.progress.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving detailed progress for a specific classroom
 */
class GetClassroomProgressUseCase @Inject constructor(
    private val progressRepository: ProgressRepository
) {
    /**
     * Executes the use case to retrieve classroom progress
     * @param classroomId The ID of the classroom to get progress for
     */
    suspend operator fun invoke(classroomId: String): Flow<ClientResponse<ClassroomProgressSummary>> {
        return progressRepository.getClassroomProgress(classroomId)
    }
}
