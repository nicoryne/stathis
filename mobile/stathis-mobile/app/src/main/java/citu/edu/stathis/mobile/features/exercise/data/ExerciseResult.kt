package citu.edu.stathis.mobile.features.exercise.data

import citu.edu.stathis.mobile.features.exercise.data.model.ExerciseState


data class ExerciseResult(
    val state: ExerciseState,
    val feedback: List<String> = emptyList(),
    val repCompleted: Boolean = false,
    val confidence: Float? = null,
    val repCount: Int = 0
)
