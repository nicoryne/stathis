package citu.edu.stathis.mobile.features.exercise.data

import citu.edu.stathis.mobile.features.exercise.data.model.ExerciseState

data class OnDeviceFeedback(
    val exerciseType: ExerciseType,
    val exerciseState: ExerciseState,
    val repCount: Int,
    val formIssues: List<String>,
    val confidence: Float,
    val angleData: Map<String, Double>
)