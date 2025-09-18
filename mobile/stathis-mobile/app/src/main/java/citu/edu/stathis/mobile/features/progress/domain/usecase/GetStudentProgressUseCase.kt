package citu.edu.stathis.mobile.features.progress.domain.usecase

import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.progress.data.model.StudentProgress
import citu.edu.stathis.mobile.features.progress.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving the overall student progress
 */
class GetStudentProgressUseCase @Inject constructor(
    private val progressRepository: ProgressRepository
) {
    /**
     * Executes the use case to retrieve student progress
     */
    suspend operator fun invoke(): Flow<ClientResponse<StudentProgress>> {
        return progressRepository.getStudentProgress()
    }
}
