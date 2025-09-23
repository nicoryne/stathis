package citu.edu.stathis.mobile.features.exercise.domain.model

import java.time.LocalDateTime

/**
 * Model representing a student's performance progress over time
 */
data class PerformanceProgress(
    val exerciseId: String,
    val sessionId: String?,
    val accuracy: List<AccuracyEntry>,
    val posture: List<PostureEntry>,
    val completion: List<CompletionEntry>
) {
    data class AccuracyEntry(
        val date: LocalDateTime,
        val value: Float // percentage from 0 to 100
    )
    
    data class PostureEntry(
        val date: LocalDateTime,
        val value: Float // score from 0 to 100
    )
    
    data class CompletionEntry(
        val date: LocalDateTime,
        val value: Float // percentage from 0 to 100
    )
}
