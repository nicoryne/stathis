package citu.edu.stathis.mobile.features.exercise.data.model

/**
 * Represents metrics collected during exercise performance
 */
data class PerformanceMetrics(
    val exerciseId: String,
    val userId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val accuracy: Float,
    val repetitionCount: Int,
    val sessionDurationMs: Long,
    val postureIssues: List<String> = emptyList()
)
