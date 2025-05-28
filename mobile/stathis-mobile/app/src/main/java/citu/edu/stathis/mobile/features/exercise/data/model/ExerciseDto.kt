package citu.edu.stathis.mobile.features.exercise.data.model

import citu.edu.stathis.mobile.features.exercise.data.ExerciseType
import kotlinx.serialization.Serializable

@Serializable
data class ExerciseDto(
    val id: String,
    val name: String,
    val description: String,
    val instructions: List<String>,
    val type: String,
    val targetMuscles: List<String>? = null,
    val difficulty: String? = null
)