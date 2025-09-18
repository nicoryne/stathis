package citu.edu.stathis.mobile.features.exercise.data.posedetection

import com.google.mlkit.vision.pose.Pose
import citu.edu.stathis.mobile.features.exercise.data.Exercise
import citu.edu.stathis.mobile.features.exercise.data.PoseLandmarksData

/**
 * Data class to hold both the raw pose and processed landmarks
 * This ensures we use the same data for both analysis and UI rendering
 */
data class PoseDetectionResult(
    val pose: Pose?,
    val landmarksData: PoseLandmarksData?,
    val exercise: Exercise,
    val error: String? = null
)
