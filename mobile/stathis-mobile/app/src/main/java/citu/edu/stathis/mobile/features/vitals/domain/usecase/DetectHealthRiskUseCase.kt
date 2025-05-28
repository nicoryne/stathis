package citu.edu.stathis.mobile.features.vitals.domain.usecase

import citu.edu.stathis.mobile.features.vitals.data.model.HealthRiskAlert // Correct model import
import citu.edu.stathis.mobile.features.vitals.data.model.VitalSigns // Correct model import
import citu.edu.stathis.mobile.features.vitals.data.model.VitalsThresholds // Correct model import
import javax.inject.Inject

class DetectHealthRiskUseCase @Inject constructor() {
    operator fun invoke(
        vitalSigns: VitalSigns,
        thresholds: VitalsThresholds,
        isExercising: Boolean = false // Context for different thresholds
    ): HealthRiskAlert? {
        // Example: Heart Rate Check
        val heartRateMax = if (isExercising) thresholds.exerciseHeartRateMax else thresholds.restingHeartRateMax
        if (vitalSigns.heartRate > heartRateMax) {
            return HealthRiskAlert(
                riskType = "High Heart Rate",
                message = "Heart rate (${vitalSigns.heartRate} BPM) is above the recommended maximum of $heartRateMax BPM.",
                suggestedAction = if (isExercising) "Consider slowing down or pausing exercise." else "Consult a healthcare professional if persistent."
            )
        }
        if (!isExercising && vitalSigns.heartRate < thresholds.restingHeartRateMin) {
            return HealthRiskAlert(
                riskType = "Low Heart Rate",
                message = "Resting heart rate (${vitalSigns.heartRate} BPM) is below the recommended minimum of ${thresholds.restingHeartRateMin} BPM.",
                suggestedAction = "Monitor and consult a healthcare professional if accompanied by symptoms."
            )
        }

        // Example: Oxygen Saturation Check
        if (vitalSigns.oxygenSaturation < thresholds.oxygenSaturationMin) {
            return HealthRiskAlert(
                riskType = "Low Oxygen Saturation",
                message = "Oxygen saturation (${vitalSigns.oxygenSaturation}%) is below the recommended minimum of ${thresholds.oxygenSaturationMin}%.",
                suggestedAction = "Ensure good ventilation. If persistent or severe, seek medical attention."
            )
        }

        // Add checks for other vitals like blood pressure, temperature etc.

        return null // No immediate risk detected
    }
}