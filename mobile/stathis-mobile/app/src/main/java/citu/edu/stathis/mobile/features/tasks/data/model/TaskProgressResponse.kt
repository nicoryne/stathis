package citu.edu.stathis.mobile.features.tasks.data.model

data class TaskProgressResponse(
    val taskId: String? = null,
    val studentId: String? = null,
    val progress: Float? = null,
    val completedLessons: List<String>? = null,
    val completedExercises: List<String>? = null,
    val quizScores: Map<String, Int>? = null,
    val isCompleted: Boolean,
    val submittedForReview: Boolean,
    // Additional fields from backend TaskProgressDTO
    val lessonCompleted: Boolean? = null,
    val exerciseCompleted: Boolean? = null,
    val quizCompleted: Boolean? = null,
    val quizScore: Int? = null,
    val maxQuizScore: Int? = null,
    val quizAttempts: Int? = null,
    val totalTimeTaken: Long? = null,
    val startedAt: String? = null,
    val completedAt: String? = null,
    val submittedAt: String? = null
) 