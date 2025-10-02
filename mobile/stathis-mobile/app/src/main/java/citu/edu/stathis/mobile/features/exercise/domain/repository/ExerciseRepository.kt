package citu.edu.stathis.mobile.features.exercise.domain.repository

import citu.edu.stathis.mobile.features.exercise.data.remote.dto.ClassificationResultDto
import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.exercise.data.Exercise
import citu.edu.stathis.mobile.features.exercise.data.ExerciseSessionResult
import citu.edu.stathis.mobile.features.exercise.data.model.PerformanceSummaryDto
import citu.edu.stathis.mobile.features.exercise.data.model.PostureResponseDto
import citu.edu.stathis.mobile.features.exercise.domain.model.PostureAnalysis
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    // Real-time analysis methods
    fun analyzePostureInRealTime(exerciseId: String): Flow<PostureAnalysis>
    suspend fun saveAnalysisResult(exerciseId: String, analysis: PostureAnalysis)
    suspend fun startExerciseSession(exerciseId: String)
    suspend fun endExerciseSession(exerciseId: String)
    
    // API-based methods
    suspend fun getAvailableExercises(): ClientResponse<List<Exercise>>
    suspend fun getExerciseDetails(exerciseId: String): ClientResponse<Exercise>
    suspend fun analyzePostureWithBackend(landmarks: List<List<List<Float>>>): ClientResponse<PostureResponseDto>
    suspend fun saveExerciseSession(sessionResult: ExerciseSessionResult): ClientResponse<Unit>
    suspend fun getExerciseHistory(userId: String): ClientResponse<List<ExerciseSessionResult>>
    suspend fun getPerformanceSummary(userId: String, exerciseId: String?): ClientResponse<List<PerformanceSummaryDto>>

    // Pose classification
    suspend fun classify(window: Array<Array<FloatArray>>): ClassificationResultDto
}