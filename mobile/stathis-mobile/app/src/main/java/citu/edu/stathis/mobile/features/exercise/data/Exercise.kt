package citu.edu.stathis.mobile.features.exercise.data

data class Exercise(
    val id: String,
    val name: String,
    val description: String,
    val instructions: List<String>,
    val type: ExerciseType,
    val targetMuscles: List<String>? = null,
    val difficulty: String? = null
)