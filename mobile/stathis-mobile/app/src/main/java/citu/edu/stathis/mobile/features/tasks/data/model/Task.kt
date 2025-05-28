package citu.edu.stathis.mobile.features.tasks.data.model

import java.time.OffsetDateTime

data class Task(
    val physicalId: String,
    val name: String,
    val description: String,
    val submissionDate: OffsetDateTime,
    val closingDate: OffsetDateTime,
    val imageUrl: String?,
    val classroomPhysicalId: String,
    val exerciseTemplateId: String?,
    val lessonTemplateId: String?,
    val quizTemplateId: String?,
    val isActive: Boolean,
    val isStarted: Boolean,
    val maxAttempts: Int,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
) 