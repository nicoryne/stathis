package citu.edu.stathis.mobile.features.tasks.data.model

// Aligns with backend Score schema; use Strings for dates to avoid Gson adapters issues
data class ScoreResponse(
    val id: String? = null,
    val physicalId: String? = null,
    val studentId: String? = null,
    val taskId: String? = null,
    val quizTemplateId: String? = null,
    val exerciseTemplateId: String? = null,
    val score: Int? = null,
    val maxScore: Int? = null,
    val attempts: Int? = null,
    val timeTaken: Long? = null,
    val accuracy: Double? = null,
    val startedAt: String? = null,
    val completedAt: String? = null,
    val teacherFeedback: String? = null,
    val manualScore: Int? = null,
    val completed: Boolean? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)