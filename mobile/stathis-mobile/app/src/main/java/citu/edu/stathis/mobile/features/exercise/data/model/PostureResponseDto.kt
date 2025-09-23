package citu.edu.stathis.mobile.features.exercise.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PostureResponseDto(
    val exerciseName: String,
    val postureScore: Float
)