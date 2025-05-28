package citu.edu.stathis.mobile.features.exercise.domain.usecase

import citu.edu.stathis.mobile.core.data.AuthTokenManager
import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.auth.data.models.UserResponseDTO
import citu.edu.stathis.mobile.features.auth.data.repository.AuthRepository
import citu.edu.stathis.mobile.features.exercise.data.Exercise
import citu.edu.stathis.mobile.features.exercise.data.ExerciseRepository
import citu.edu.stathis.mobile.features.exercise.data.ExerciseSessionResult
import citu.edu.stathis.mobile.features.exercise.data.model.PostureResponseDto
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDateTime
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
            val role = profileResponse.data.role
            authTokenManager.updateUserIdentity(physicalId, role)
            physicalId
        } else {
            null
        }
    }
}

class GetAvailableExercisesUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) {
    suspend operator fun invoke(): ClientResponse<List<Exercise>> {
        return exerciseRepository.getAvailableExercises()
    }
}

class AnalyzePostureWithBackendUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) {
    suspend operator fun invoke(landmarks: List<List<List<Float>>>): ClientResponse<PostureResponseDto> {
        if (landmarks.isEmpty() || landmarks.firstOrNull()?.size != 33 || landmarks.firstOrNull()?.firstOrNull()?.size != 3) {
            return ClientResponse(success = false, message = "Invalid landmark data format for backend analysis.", data = null)
        }
        return exerciseRepository.analyzePostureWithBackend(landmarks)
    }
}

class SaveExerciseSessionUseCase @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) {
    suspend operator fun invoke(
        userId: String,
        exercise: Exercise,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        durationMs: Long,
        repCount: Int,
        averageAccuracy: Float?,
        formIssues: List<String>?,
        classroomId: String? = null,
        taskId: String? = null
    ): ClientResponse<Unit> {
        if (userId.isBlank()) {
            return ClientResponse(success = false, message = "User ID is required to save session.", data = null)
        }

        val sessionResult = ExerciseSessionResult(
            sessionId = java.util.UUID.randomUUID().toString(),
            userId = userId,
            exerciseId = exercise.id,
            exerciseName = exercise.name,
            startTime = startTime,
            endTime = endTime,
            durationMs = durationMs,
            repCount = repCount,
            averageAccuracy = averageAccuracy,
            issuesDetected = formIssues,
            classroomId = classroomId,
            taskId = taskId
        )
        return exerciseRepository.saveExerciseSession(sessionResult)
    }
}
