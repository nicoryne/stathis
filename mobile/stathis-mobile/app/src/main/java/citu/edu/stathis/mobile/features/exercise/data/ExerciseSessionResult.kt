package citu.edu.stathis.mobile.features.exercise.data

import java.time.LocalDateTime

data class ExerciseSessionResult(
    val sessionId: String, // Could be generated on client or server
    val userId: String,
    val exerciseId: String, // Link to the Exercise definition
    val exerciseName: String, // Denormalized for easier display
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val durationMs: Long,
    val repCount: Int,
    val averageAccuracy: Float?, // Could be derived from multiple backend scores or on-device logic
    val issuesDetected: List<String>? = null, // Summary of common issues
    // Store raw landmarks if needed for detailed review/re-analysis (can be large)
    // val landmarkSnapshots: List<PoseLandmarksData>? = null
    val classroomId: String? = null, // For class tasks
    val taskId: String? = null      // For class tasks
)