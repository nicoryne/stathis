package citu.edu.stathis.mobile.features.exercise.domain.usecase

import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.exercise.data.model.PostureResponseDto
import citu.edu.stathis.mobile.features.exercise.data.ExerciseRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyzePostureWithBackendUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) {
    suspend operator fun invoke(landmarks: List<List<List<Float>>>): ClientResponse<PostureResponseDto> {
        return exerciseRepository.analyzePostureWithBackend(landmarks)
    }
}
