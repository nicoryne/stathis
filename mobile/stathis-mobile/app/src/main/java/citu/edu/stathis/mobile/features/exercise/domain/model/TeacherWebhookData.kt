package citu.edu.stathis.mobile.features.exercise.domain.model

/**
 * Domain model for sending notifications to teachers about student exercise performance
 */
data class TeacherWebhookData(
    val studentId: String,
    val exerciseId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val accuracy: Float,
    val postureIssues: List<String> = emptyList(),
    val teacherId: String? = null,
    val classroomId: String? = null,
    val taskId: String? = null
)
