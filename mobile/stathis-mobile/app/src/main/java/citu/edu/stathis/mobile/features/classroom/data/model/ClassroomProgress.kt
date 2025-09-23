package citu.edu.stathis.mobile.features.classroom.data.model

data class ClassroomProgress(
    val classroomId: String,
    val studentId: String,
    val completedTaskCount: Int,
    val totalTaskCount: Int,
    val averageScore: Float,
    val exerciseAccuracy: Float,
    val quizScores: Map<String, Int>,
    val completedExercises: List<String>,
    val completedLessons: List<String>,
    val lastActivityDate: String
) 