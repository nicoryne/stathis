package citu.edu.stathis.mobile.features.task.domain.model

import java.time.LocalDateTime

/**
 * Data class representing a task assigned to a student
 */
data class Task(
    val id: String,
    val title: String,
    val description: String,
    val dueDate: LocalDateTime,
    val status: TaskStatus,
    val classroomId: String,
    val studentId: String,
    val teacherId: String,
    val type: TaskType,
    val points: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val exerciseId: String? = null,
    val lessonId: String? = null,
    val quizId: String? = null
)
