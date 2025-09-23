package citu.edu.stathis.mobile.features.exercise.data.model

/**
 * Data class for sending notifications to teachers about student exercise performance
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
