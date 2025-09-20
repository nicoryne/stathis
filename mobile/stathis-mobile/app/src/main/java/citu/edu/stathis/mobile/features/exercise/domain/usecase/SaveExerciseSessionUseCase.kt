package citu.edu.stathis.mobile.features.exercise.domain.usecase

import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.exercise.data.Exercise
import citu.edu.stathis.mobile.features.exercise.data.ExerciseSessionResult
import citu.edu.stathis.mobile.features.exercise.domain.repository.ExerciseRepository
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaveExerciseSessionUseCase @Inject constructor(
    private val repository: ExerciseRepository
) {
    suspend operator fun invoke(
        userId: String,
        exercise: Exercise,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        durationMs: Long,
        repCount: Int,
        averageAccuracy: Float,
        formIssues: List<String>? = null,
        classroomId: String? = null,
        taskId: String? = null
    ): ClientResponse<Unit> {
        val sessionResult = ExerciseSessionResult(
            sessionId = "${exercise.id}_${System.currentTimeMillis()}",
            userId = userId,
            exerciseId = exercise.id,
            exerciseName = exercise.name,
            startTime = startTime,
            endTime = endTime,
            durationMs = durationMs,
            repCount = repCount,
            averageAccuracy = averageAccuracy,
            issuesDetected = formIssues ?: emptyList(),
            classroomId = classroomId,
            taskId = taskId
        )
        
        return repository.saveExerciseSession(sessionResult)
    }
}
