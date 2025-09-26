package citu.edu.stathis.mobile.features.tasks.data.model

data class Task(
    val physicalId: String,
    val name: String,
    val description: String,
    val submissionDate: String,
    val closingDate: String,
    val imageUrl: String?,
    val classroomPhysicalId: String,
    val exerciseTemplateId: String?,
    val lessonTemplateId: String?,
    val quizTemplateId: String?,
    val isActive: Boolean,
    val isStarted: Boolean,
    val maxAttempts: Int,
    val createdAt: String,
    val updatedAt: String
) 