package citu.edu.stathis.mobile.features.vitals.domain.model

import java.time.LocalDateTime

data class VitalsData(
    val heartRate: Int, // BPM
    val oxygenSaturation: Int, // SpO2 percentage
    val timestamp: LocalDateTime,
    val isExerciseSession: Boolean = false,
    val sessionId: String? = null
)

data class VitalsThresholds(
    val minRestingHeartRate: Int = 60,
    val maxRestingHeartRate: Int = 100,
    val minExerciseHeartRate: Int = 100,
    val maxExerciseHeartRate: Int = 150,
    val minOxygenSaturation: Int = 95
)

data class HealthRiskAlert(
    val type: RiskType,
    val message: String,
    val timestamp: LocalDateTime,
    val vitalsData: VitalsData,
    val severity: RiskSeverity,
    val sessionId: String? = null
)

enum class RiskType {
    HIGH_HEART_RATE,
    LOW_HEART_RATE,
    LOW_OXYGEN,
    PERSISTENT_HIGH_HEART_RATE
}

enum class RiskSeverity {
    WARNING,
    CRITICAL
}

data class VitalsSessionSummary(
    val sessionId: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val averageHeartRate: Int,
    val maxHeartRate: Int,
    val minHeartRate: Int,
    val averageOxygenSaturation: Int,
    val minOxygenSaturation: Int,
    val healthRisks: List<HealthRiskAlert>
) 