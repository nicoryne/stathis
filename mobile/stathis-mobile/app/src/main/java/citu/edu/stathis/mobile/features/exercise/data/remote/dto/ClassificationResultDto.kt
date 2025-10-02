package citu.edu.stathis.mobile.features.exercise.data.remote.dto

data class ClassificationResultDto(
    val predictedClass: String,
    val score: Float,
    val probabilities: List<Float>,
    val classNames: List<String>,
    val flags: List<String>?,
    val messages: List<String>?
)


