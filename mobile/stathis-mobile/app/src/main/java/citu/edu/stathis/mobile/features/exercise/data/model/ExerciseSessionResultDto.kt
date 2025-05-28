package citu.edu.stathis.mobile.features.exercise.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ExerciseSessionResultDto(
    val sessionId: String,
    val userId: String,
    val exerciseId: String,
    val exerciseName: String,
    val startTime: String,
    val endTime: String,
    val durationMs: Long,
    val repCount: Int,
    val averageAccuracy: Float?,
    val issuesDetected: List<String>? = null,
    val classroomId: String? = null,
    val taskId: String? = null
)