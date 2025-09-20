package citu.edu.stathis.mobile.features.tasks.data.model

data class TaskProgressResponse(
    val taskId: String,
    val studentId: String,
    val progress: Float,
    val completedLessons: List<String>,
    val completedExercises: List<String>,
    val quizScores: Map<String, Int>,
    val isCompleted: Boolean,
    val submittedForReview: Boolean
) 