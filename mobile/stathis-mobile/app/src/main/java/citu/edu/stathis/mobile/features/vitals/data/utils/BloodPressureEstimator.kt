package citu.edu.stathis.mobile.features.vitals.data.utils

import kotlin.math.max
import kotlin.math.min

/**
 * Utility class for estimating blood pressure from heart rate
 * Uses established medical correlations and age-based adjustments
 */
class BloodPressureEstimator {
    
    companion object {
        // Age-based baseline adjustments
        private const val YOUNG_ADULT_AGE = 25
        private const val MIDDLE_AGE = 50
        private const val SENIOR_AGE = 65
        
        // Heart rate ranges for different activity levels
        private const val RESTING_HR_MIN = 60
        private const val RESTING_HR_MAX = 100
        private const val MODERATE_EXERCISE_HR_MIN = 100
        private const val MODERATE_EXERCISE_HR_MAX = 140
        private const val INTENSE_EXERCISE_HR_MIN = 140
        private const val INTENSE_EXERCISE_HR_MAX = 180
        
        // Blood pressure estimation factors
        private const val SYSTOLIC_BASE = 120
        private const val DIASTOLIC_BASE = 80
        private const val SYSTOLIC_HR_FACTOR = 0.5f // mmHg per BPM above resting
        private const val DIASTOLIC_HR_FACTOR = 0.2f // mmHg per BPM above resting
    }
    
    /**
     * Estimates blood pressure from heart rate with age consideration
     * @param heartRate Current heart rate in BPM
     * @param age User's age (optional, defaults to young adult)
     * @param isResting Whether the measurement is taken at rest
     * @return Pair of (systolic, diastolic) blood pressure in mmHg
     */
    fun estimateBloodPressure(
        heartRate: Int,
        age: Int = YOUNG_ADULT_AGE,
        isResting: Boolean = false
    ): Pair<Int, Int> {
        // Get age-adjusted baseline
        val ageAdjustment = getAgeAdjustment(age)
        val baselineSystolic = SYSTOLIC_BASE + ageAdjustment.first
        val baselineDiastolic = DIASTOLIC_BASE + ageAdjustment.second
        
        // Determine activity level and calculate adjustments
        val activityLevel = determineActivityLevel(heartRate, isResting)
        val hrAdjustment = calculateHeartRateAdjustment(heartRate, activityLevel)
        
        // Calculate final blood pressure
        val systolic = (baselineSystolic + hrAdjustment.first).toInt()
        val diastolic = (baselineDiastolic + hrAdjustment.second).toInt()
        
        // Ensure values are within reasonable physiological ranges
        return Pair(
            systolic.coerceIn(90, 200),
            diastolic.coerceIn(60, 120)
        )
    }
    
    /**
     * Estimates blood pressure specifically for exercise scenarios
     * @param heartRate Current heart rate during exercise
     * @param age User's age
     * @param exerciseIntensity Estimated exercise intensity (0.0 = rest, 1.0 = max)
     * @return Pair of (systolic, diastolic) blood pressure in mmHg
     */
    fun estimateExerciseBloodPressure(
        heartRate: Int,
        age: Int = YOUNG_ADULT_AGE,
        exerciseIntensity: Float = 0.5f
    ): Pair<Int, Int> {
        val ageAdjustment = getAgeAdjustment(age)
        val baselineSystolic = SYSTOLIC_BASE + ageAdjustment.first
        val baselineDiastolic = DIASTOLIC_BASE + ageAdjustment.second
        
        // Exercise-specific adjustments
        val exerciseSystolicAdjustment = exerciseIntensity * 40 // Up to 40 mmHg increase
        val exerciseDiastolicAdjustment = exerciseIntensity * 10 // Up to 10 mmHg increase
        
        // Heart rate correlation
        val hrAboveResting = max(0, heartRate - RESTING_HR_MAX)
        val hrSystolicAdjustment = hrAboveResting * SYSTOLIC_HR_FACTOR
        val hrDiastolicAdjustment = hrAboveResting * DIASTOLIC_HR_FACTOR
        
        val systolic = (baselineSystolic + exerciseSystolicAdjustment + hrSystolicAdjustment).toInt()
        val diastolic = (baselineDiastolic + exerciseDiastolicAdjustment + hrDiastolicAdjustment).toInt()
        
        return Pair(
            systolic.coerceIn(90, 200),
            diastolic.coerceIn(60, 120)
        )
    }
    
    /**
     * Gets age-based blood pressure adjustments
     */
    private fun getAgeAdjustment(age: Int): Pair<Int, Int> {
        return when {
            age < YOUNG_ADULT_AGE -> Pair(-5, -3) // Younger adults tend to have lower BP
            age < MIDDLE_AGE -> Pair(0, 0) // Baseline
            age < SENIOR_AGE -> Pair(5, 3) // Middle-aged adults
            else -> Pair(10, 5) // Seniors tend to have higher BP
        }
    }
    
    /**
     * Determines activity level based on heart rate
     */
    private fun determineActivityLevel(heartRate: Int, isResting: Boolean): ActivityLevel {
        return when {
            isResting || heartRate <= RESTING_HR_MAX -> ActivityLevel.RESTING
            heartRate <= MODERATE_EXERCISE_HR_MAX -> ActivityLevel.MODERATE_EXERCISE
            heartRate <= INTENSE_EXERCISE_HR_MAX -> ActivityLevel.INTENSE_EXERCISE
            else -> ActivityLevel.MAXIMUM_EFFORT
        }
    }
    
    /**
     * Calculates heart rate-based blood pressure adjustments
     */
    private fun calculateHeartRateAdjustment(heartRate: Int, activityLevel: ActivityLevel): Pair<Float, Float> {
        val restingHR = (RESTING_HR_MIN + RESTING_HR_MAX) / 2
        val hrAboveResting = max(0, heartRate - restingHR)
        
        return when (activityLevel) {
            ActivityLevel.RESTING -> Pair(0f, 0f)
            ActivityLevel.MODERATE_EXERCISE -> Pair(
                hrAboveResting * SYSTOLIC_HR_FACTOR * 0.7f,
                hrAboveResting * DIASTOLIC_HR_FACTOR * 0.7f
            )
            ActivityLevel.INTENSE_EXERCISE -> Pair(
                hrAboveResting * SYSTOLIC_HR_FACTOR,
                hrAboveResting * DIASTOLIC_HR_FACTOR
            )
            ActivityLevel.MAXIMUM_EFFORT -> Pair(
                hrAboveResting * SYSTOLIC_HR_FACTOR * 1.2f,
                hrAboveResting * DIASTOLIC_HR_FACTOR * 1.2f
            )
        }
    }
    
    /**
     * Provides a confidence level for the blood pressure estimate
     * @param heartRate Current heart rate
     * @param age User's age
     * @param hasPreviousReadings Whether we have previous BP readings for comparison
     * @return Confidence level as a percentage (0-100)
     */
    fun getEstimationConfidence(
        heartRate: Int,
        age: Int = YOUNG_ADULT_AGE,
        hasPreviousReadings: Boolean = false
    ): Int {
        var confidence = 60 // Base confidence for HR-based estimation
        
        // Adjust confidence based on heart rate range
        confidence += when {
            heartRate in RESTING_HR_MIN..RESTING_HR_MAX -> 20 // More reliable at rest
            heartRate in MODERATE_EXERCISE_HR_MIN..MODERATE_EXERCISE_HR_MAX -> 10 // Moderate exercise
            else -> 0 // Less reliable at extreme heart rates
        }
        
        // Adjust confidence based on age
        confidence += when {
            age in 20..40 -> 10 // Most reliable age range
            age in 40..60 -> 5 // Still reliable
            else -> 0 // Less reliable for very young or elderly
        }
        
        // Boost confidence if we have previous readings
        if (hasPreviousReadings) {
            confidence += 10
        }
        
        return confidence.coerceIn(0, 100)
    }
    
    private enum class ActivityLevel {
        RESTING,
        MODERATE_EXERCISE,
        INTENSE_EXERCISE,
        MAXIMUM_EFFORT
    }
}

