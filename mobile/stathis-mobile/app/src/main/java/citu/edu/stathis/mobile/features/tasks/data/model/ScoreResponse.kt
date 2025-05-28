package citu.edu.stathis.mobile.features.tasks.data.model

import java.time.OffsetDateTime

data class ScoreResponse(
    val physicalId: String,
    val studentId: String,
    val taskId: String,
    val templateId: String,
    val score: Int,
    val isQuiz: Boolean,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime
) 