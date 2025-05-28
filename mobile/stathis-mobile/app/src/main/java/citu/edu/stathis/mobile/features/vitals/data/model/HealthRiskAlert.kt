package citu.edu.stathis.mobile.features.vitals.data.model

data class HealthRiskAlert(
    val riskType: String,
    val message: String,
    val suggestedAction: String? = null
)