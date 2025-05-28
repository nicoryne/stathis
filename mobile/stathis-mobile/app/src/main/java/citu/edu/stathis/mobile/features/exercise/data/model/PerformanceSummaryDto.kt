package citu.edu.stathis.mobile.features.exercise.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PerformanceSummaryDto(
    val exerciseId: String,
    val exerciseName: String,
    val totalSessions: Int,
    val averageReps: Int,
    val averageAccuracy: Float?,
    val progressTrend: List<Float>?
)