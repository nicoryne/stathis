package citu.edu.stathis.mobile.features.vitals.data.model

import java.time.LocalDateTime

/**
 * Data transfer object representing a summary of vital signs data collected during an exercise session
 */
data class VitalsSessionSummaryDto(
    val sessionId: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val averageHeartRate: Int,
    val maxHeartRate: Int,
    val minHeartRate: Int,
    val averageOxygenSaturation: Int,
    val minOxygenSaturation: Int,
    val healthRisks: List<String>
)
