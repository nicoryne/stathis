package citu.edu.stathis.mobile.features.posture.data

import android.graphics.PointF
import android.util.Log
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2

class PostureAnalyzer {
    companion object {
        private const val TAG = "PostureAnalyzer"

        // Thresholds for good posture
        private const val SHOULDER_ANGLE_THRESHOLD = 10.0 // degrees
        private const val NECK_ANGLE_THRESHOLD = 15.0 // degrees
        private const val HIP_SHOULDER_ALIGNMENT_THRESHOLD = 15.0 // degrees
    }

    /**
     * Analyzes a pose to determine if the posture is good or bad
     * @return PostureResult containing the analysis
     */
    fun analyzePose(pose: Pose): PostureResult {
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val leftEar = pose.getPoseLandmark(PoseLandmark.LEFT_EAR)
        val rightEar = pose.getPoseLandmark(PoseLandmark.RIGHT_EAR)
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        val nose = pose.getPoseLandmark(PoseLandmark.NOSE)

        // Check if we have all the landmarks we need
        if (leftShoulder == null || rightShoulder == null ||
            leftEar == null || rightEar == null ||
            leftHip == null || rightHip == null || nose == null) {
            return PostureResult(
                isGoodPosture = false,
                confidence = 0.0f,
                issues = listOf("Not all required landmarks detected"),
                shoulderAngle = 0.0,
                neckAngle = 0.0,
                hipShoulderAlignment = 0.0
            )
        }

        // Calculate shoulder angle (horizontal alignment)
        val shoulderAngle = calculateAngle(
            leftShoulder.position,
            rightShoulder.position,
            true
        )

        // Calculate neck angle (head tilt)
        val midShoulder = PointF(
            (leftShoulder.position.x + rightShoulder.position.x) / 2,
            (leftShoulder.position.y + rightShoulder.position.y) / 2
        )
        val neckAngle = calculateAngle(
            midShoulder,
            nose.position,
            false
        )

        // Calculate hip-shoulder alignment
        val midHip = PointF(
            (leftHip.position.x + rightHip.position.x) / 2,
            (leftHip.position.y + rightHip.position.y) / 2
        )
        val hipShoulderAlignment = calculateAngle(
            midHip,
            midShoulder,
            false
        )

        // Determine issues
        val issues = mutableListOf<String>()

        if (abs(shoulderAngle) > SHOULDER_ANGLE_THRESHOLD) {
            issues.add("Shoulders not level")
        }

        if (abs(neckAngle) > NECK_ANGLE_THRESHOLD) {
            issues.add("Head tilted too much")
        }

        if (abs(hipShoulderAlignment) > HIP_SHOULDER_ALIGNMENT_THRESHOLD) {
            issues.add("Spine not straight")
        }

        // Calculate overall confidence based on landmark confidence
        val landmarkConfidence = listOf(
            leftShoulder, rightShoulder, leftEar, rightEar,
            leftHip, rightHip, nose
        ).map { it.inFrameLikelihood }.average().toFloat()

        // Determine if posture is good
        val isGoodPosture = issues.isEmpty()

        Log.d(TAG, "Posture analysis: isGood=$isGoodPosture, issues=$issues, " +
                "shoulderAngle=$shoulderAngle, neckAngle=$neckAngle, " +
                "hipShoulderAlignment=$hipShoulderAlignment")

        return PostureResult(
            isGoodPosture = isGoodPosture,
            confidence = landmarkConfidence,
            issues = issues,
            shoulderAngle = shoulderAngle,
            neckAngle = neckAngle,
            hipShoulderAlignment = hipShoulderAlignment
        )
    }

    /**
     * Calculate angle between two points
     * @param horizontal if true, calculates angle from horizontal, otherwise from vertical
     */
    private fun calculateAngle(point1: PointF, point2: PointF, horizontal: Boolean): Double {
        val deltaX = point2.x - point1.x
        val deltaY = point2.y - point1.y

        val angleRadians = atan2(deltaY.toDouble(), deltaX.toDouble())
        var angleDegrees = Math.toDegrees(angleRadians)

        // Adjust based on reference (horizontal or vertical)
        if (horizontal) {
            // Angle from horizontal
            angleDegrees = if (angleDegrees > 90) angleDegrees - 180 else angleDegrees
        } else {
            // Angle from vertical
            angleDegrees = if (angleDegrees > 0) 90 - angleDegrees else 90 + abs(angleDegrees)
            if (angleDegrees > 90) angleDegrees = angleDegrees - 180
        }

        return angleDegrees
    }
}

data class PostureResult(
    val isGoodPosture: Boolean,
    val confidence: Float,
    val issues: List<String>,
    val shoulderAngle: Double,
    val neckAngle: Double,
    val hipShoulderAlignment: Double
)