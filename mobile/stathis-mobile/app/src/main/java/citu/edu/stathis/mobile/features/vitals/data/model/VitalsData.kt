package citu.edu.stathis.mobile.features.vitals.data.model

/**
 * Represents vital signs data collected during exercise sessions
 */
data class VitalsData(
    val heartRate: Int = 0,
    val oxygenSaturation: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
