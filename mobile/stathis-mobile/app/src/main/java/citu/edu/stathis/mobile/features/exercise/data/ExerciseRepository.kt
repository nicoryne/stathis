package citu.edu.stathis.mobile.features.exercise.data

import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.exercise.data.model.PerformanceSummaryDto
import citu.edu.stathis.mobile.features.exercise.data.model.PostureResponseDto

interface ExerciseRepository {
    suspend fun getAvailableExercises(): ClientResponse<List<Exercise>>
    suspend fun getExerciseDetails(exerciseId: String): ClientResponse<Exercise>

    suspend fun analyzePostureWithBackend(
        landmarks: List<List<List<Float>>>
    ): ClientResponse<PostureResponseDto>

    suspend fun saveExerciseSession(sessionResult: ExerciseSessionResult): ClientResponse<Unit>
    suspend fun getExerciseHistory(userId: String): ClientResponse<List<ExerciseSessionResult>>
    suspend fun getPerformanceSummary(userId: String, exerciseId: String?): ClientResponse<List<PerformanceSummaryDto>>
}