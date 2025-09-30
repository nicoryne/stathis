package citu.edu.stathis.mobile.features.tasks.data.model

import com.google.gson.annotations.SerializedName

data class Task(
    val physicalId: String,
    val name: String,
    val description: String,
    val submissionDate: String,
    val closingDate: String,
    val imageUrl: String?,
    val classroomPhysicalId: String,
    // Template references may be sent either as IDs or embedded objects depending on endpoint
    val exerciseTemplateId: String?,
    val lessonTemplateId: String?,
    val quizTemplateId: String?,
    @SerializedName("exerciseTemplate")
    val exerciseTemplate: ExerciseTemplate? = null,
    @SerializedName("lessonTemplate")
    val lessonTemplate: LessonTemplate? = null,
    @SerializedName("quizTemplate")
    val quizTemplate: QuizTemplate? = null,
    @SerializedName("active")
    val isActive: Boolean? = null,
    @SerializedName("started")
    val isStarted: Boolean? = null,
    val maxAttempts: Int,
    val createdAt: String,
    val updatedAt: String
) 