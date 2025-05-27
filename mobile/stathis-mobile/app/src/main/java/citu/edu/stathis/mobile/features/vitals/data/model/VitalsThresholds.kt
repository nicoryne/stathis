package citu.edu.stathis.mobile.features.vitals.data.model

data class VitalsThresholds(
    val restingHeartRateMin: Int = 60,
    val restingHeartRateMax: Int = 100,
    val exerciseHeartRateMax: Int = 150,
    val oxygenSaturationMin: Float = 95.0f
)