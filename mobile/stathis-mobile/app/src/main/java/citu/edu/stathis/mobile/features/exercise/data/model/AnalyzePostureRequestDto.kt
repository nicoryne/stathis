package citu.edu.stathis.mobile.features.exercise.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AnalyzePostureRequestDto(
    val landmarks: List<List<List<Float>>>
)