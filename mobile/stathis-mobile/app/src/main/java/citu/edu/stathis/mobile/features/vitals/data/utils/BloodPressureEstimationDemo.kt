package citu.edu.stathis.mobile.features.vitals.data.utils

/**
 * Test class to demonstrate blood pressure estimation from heart rate
 * This shows how the algorithm works with different heart rate values
 */
object BloodPressureEstimationDemo {
    
    fun demonstrateEstimation() {
        val estimator = BloodPressureEstimator()
        
        println("=== Blood Pressure Estimation Demo ===")
        println()
        
        // Test different heart rate scenarios
        val testCases = listOf(
            Triple(65, 25, "Resting (Young Adult)"),
            Triple(85, 25, "Light Activity (Young Adult)"),
            Triple(120, 25, "Moderate Exercise (Young Adult)"),
            Triple(150, 25, "Intense Exercise (Young Adult)"),
            Triple(75, 45, "Resting (Middle-aged)"),
            Triple(95, 45, "Light Activity (Middle-aged)"),
            Triple(80, 70, "Resting (Senior)")
        )
        
        testCases.forEach { (heartRate, age, scenario) ->
            val (systolic, diastolic) = estimator.estimateBloodPressure(
                heartRate = heartRate,
                age = age,
                isResting = heartRate <= 100
            )
            
            val confidence = estimator.getEstimationConfidence(heartRate, age)
            
            println("$scenario:")
            println("  Heart Rate: $heartRate BPM")
            println("  Estimated BP: $systolic/$diastolic mmHg")
            println("  Confidence: $confidence%")
            println()
        }
        
        println("=== Exercise-Specific Estimation ===")
        println()
        
        // Test exercise-specific estimation
        val exerciseCases = listOf(
            Triple(100, 0.3f, "Light Exercise"),
            Triple(130, 0.6f, "Moderate Exercise"),
            Triple(160, 0.9f, "Intense Exercise")
        )
        
        exerciseCases.forEach { (heartRate, intensity, scenario) ->
            val (systolic, diastolic) = estimator.estimateExerciseBloodPressure(
                heartRate = heartRate,
                age = 25,
                exerciseIntensity = intensity
            )
            
            println("$scenario:")
            println("  Heart Rate: $heartRate BPM")
            println("  Exercise Intensity: ${(intensity * 100).toInt()}%")
            println("  Estimated BP: $systolic/$diastolic mmHg")
            println()
        }
    }
}

