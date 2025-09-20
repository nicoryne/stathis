package citu.edu.stathis.mobile.features.exercise.data.model

/**
 * Data transfer object for sending posture analysis requests to the backend
 */
data class PostureRequestDto(
    val landmarks: List<List<List<Float>>>,
    val sessionId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val clientVersion: String = "1.0.0"
)
