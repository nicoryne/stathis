package citu.edu.stathis.mobile.features.vitals.domain.model

import java.time.LocalDateTime

data class VitalSigns(
    val id: String? = null,
    val userId: String,
    val systolicBP: Int,
    val diastolicBP: Int,
    val heartRate: Int,
    val respirationRate: Int,
    val temperature: Float,
    val oxygenSaturation: Float,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val deviceName: String? = null
)