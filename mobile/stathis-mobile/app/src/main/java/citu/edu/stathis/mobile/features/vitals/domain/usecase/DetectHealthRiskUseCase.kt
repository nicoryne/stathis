package citu.edu.stathis.mobile.features.vitals.domain.usecase

import citu.edu.stathis.mobile.features.vitals.data.model.HealthRiskAlert
import citu.edu.stathis.mobile.features.vitals.data.model.VitalSigns
import citu.edu.stathis.mobile.features.vitals.data.model.VitalsThresholds
import javax.inject.Inject

class DetectHealthRiskUseCase @Inject constructor() {
    operator fun invoke(
        vitalSigns: VitalSigns,
        thresholds: VitalsThresholds,
        isExercising: Boolean = false
    ): HealthRiskAlert? {
        vitalSigns.heartRate?.let { hr ->
            val (minHr, maxHr) = if (isExercising) {
                Pair(null, thresholds.exerciseHeartRateMax)
            } else {
                Pair(thresholds.restingHeartRateMin, thresholds.restingHeartRateMax)
            }

            if (hr > maxHr) {
                return HealthRiskAlert(
                    riskType = "High Heart Rate",
                    message = "Heart rate ($hr BPM) is above the recommended maximum of $maxHr BPM.",
                    suggestedAction = if (isExercising) "Consider slowing down or pausing activity." else "Consult a healthcare professional if persistent."
                )
            }
            minHr?.let {
                if (hr < it && !isExercising) {
                    return HealthRiskAlert(
                        riskType = "Low Heart Rate",
                        message = "Resting heart rate ($hr BPM) is below the recommended minimum of $it BPM.",
                        suggestedAction = "Monitor and consult a healthcare professional if accompanied by symptoms."
                    )
                }
            }
        }

        vitalSigns.oxygenSaturation?.let { o2sat ->
            if (o2sat < thresholds.oxygenSaturationMin) {
                return HealthRiskAlert(
                    riskType = "Low Oxygen Saturation",
                    message = "Oxygen saturation (${o2sat}%) is below the recommended minimum of ${thresholds.oxygenSaturationMin}%.",
                    suggestedAction = "Ensure good ventilation. If persistent or severe, seek medical attention."
                )
            }
        }
        return null
    }
}