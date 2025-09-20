package citu.edu.stathis.mobile.features.exercise.data

import citu.edu.stathis.mobile.features.exercise.data.model.ExerciseState

data class PostureFeedback(
    val exerciseState: ExerciseState,
    val repCount: Int,
    val formIssues: List<String>,
    val confidence: Float,
    val backendPostureScore: Float? = null,
    val backendIdentifiedExercise: String? = null
)