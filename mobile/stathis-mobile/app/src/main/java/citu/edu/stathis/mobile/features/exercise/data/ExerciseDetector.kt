package citu.edu.stathis.mobile.features.exercise.data

import android.graphics.PointF
import android.util.Log
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

enum class ExerciseType {
    SQUAT,
    PUSHUP
}

enum class ExerciseState {
    WAITING, // Waiting to start
    UP,      // Standing position for squats or up position for push-ups
    DOWN,    // Squat position or down position for push-ups
    INVALID  // Invalid form detected
}

data class ExerciseResult(
    val exerciseType: ExerciseType,
    val currentState: ExerciseState,
    val repCount: Int,
    val confidence: Float,
    val formIssues: List<String>,
    val angleData: Map<String, Double>
)

class ExerciseDetector {
    companion object {
        private const val TAG = "ExerciseDetector"

        // Thresholds for squat detection
        private const val SQUAT_KNEE_ANGLE_THRESHOLD_DOWN = 100.0 // degrees - smaller when squatting
        private const val SQUAT_KNEE_ANGLE_THRESHOLD_UP = 160.0 // degrees - larger when standing
        private const val SQUAT_HIP_KNEE_ALIGNMENT_THRESHOLD = 30.0 // degrees

        // Thresholds for push-up detection
        private const val PUSHUP_ELBOW_ANGLE_THRESHOLD_DOWN = 100.0 // degrees - smaller when down
        private const val PUSHUP_ELBOW_ANGLE_THRESHOLD_UP = 160.0 // degrees - larger when up
        private const val PUSHUP_BODY_ALIGNMENT_THRESHOLD = 20.0 // degrees
    }

    // State tracking
    private var lastExerciseState = ExerciseState.WAITING
    private var repCount = 0
    private var isRepInProgress = false

    // Debounce to prevent rapid state changes
    private var stateChangeDebounce = 0
    private val DEBOUNCE_THRESHOLD = 3

    /**
     * Analyze pose for squat detection
     */
    fun analyzeSquat(pose: Pose): ExerciseResult {
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
        val rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
        val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
        val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)

        // Check if we have all the landmarks we need
        if (leftHip == null || rightHip == null ||
            leftKnee == null || rightKnee == null ||
            leftAnkle == null || rightAnkle == null ||
            leftShoulder == null || rightShoulder == null) {
            return ExerciseResult(
                exerciseType = ExerciseType.SQUAT,
                currentState = ExerciseState.INVALID,
                repCount = repCount,
                confidence = 0.0f,
                formIssues = listOf("Not all required landmarks detected"),
                angleData = emptyMap()
            )
        }

        // Calculate knee angles
        val leftKneeAngle = calculateAngle(
            leftHip.position,
            leftKnee.position,
            leftAnkle.position
        )

        val rightKneeAngle = calculateAngle(
            rightHip.position,
            rightKnee.position,
            rightAnkle.position
        )

        // Average knee angle
        val kneeAngle = (leftKneeAngle + rightKneeAngle) / 2

        // Calculate hip-knee alignment
        val hipKneeAlignment = calculateHipKneeAlignment(
            leftHip.position, rightHip.position,
            leftKnee.position, rightKnee.position
        )

        // Determine form issues
        val formIssues = mutableListOf<String>()

        if (abs(hipKneeAlignment) > SQUAT_HIP_KNEE_ALIGNMENT_THRESHOLD) {
            formIssues.add("Knees not aligned with hips")
        }

        if (abs(leftKneeAngle - rightKneeAngle) > 20) {
            formIssues.add("Uneven knee bend")
        }

        // Calculate overall confidence based on landmark confidence
        val landmarkConfidence = listOf(
            leftHip, rightHip, leftKnee, rightKnee,
            leftAnkle, rightAnkle, leftShoulder, rightShoulder
        ).map { it.inFrameLikelihood }.average().toFloat()

        // Determine current state
        val currentState = when {
            landmarkConfidence < 0.7f -> ExerciseState.INVALID
            kneeAngle < SQUAT_KNEE_ANGLE_THRESHOLD_DOWN -> ExerciseState.DOWN
            kneeAngle > SQUAT_KNEE_ANGLE_THRESHOLD_UP -> ExerciseState.UP
            else -> lastExerciseState // Maintain previous state if in between
        }

        // Count reps with debounce to prevent jitter
        if (currentState != lastExerciseState) {
            stateChangeDebounce++
            if (stateChangeDebounce >= DEBOUNCE_THRESHOLD) {
                // State change confirmed
                if (currentState == ExerciseState.DOWN && lastExerciseState == ExerciseState.UP) {
                    // Started a rep
                    isRepInProgress = true
                } else if (currentState == ExerciseState.UP && lastExerciseState == ExerciseState.DOWN && isRepInProgress) {
                    // Completed a rep
                    repCount++
                    isRepInProgress = false
                }

                lastExerciseState = currentState
                stateChangeDebounce = 0
            }
        } else {
            stateChangeDebounce = 0
        }

        val angleData = mapOf(
            "leftKneeAngle" to leftKneeAngle,
            "rightKneeAngle" to rightKneeAngle,
            "hipKneeAlignment" to hipKneeAlignment
        )

        Log.d(TAG, "Squat analysis: state=$currentState, repCount=$repCount, " +
                "kneeAngle=$kneeAngle, issues=$formIssues")

        return ExerciseResult(
            exerciseType = ExerciseType.SQUAT,
            currentState = currentState,
            repCount = repCount,
            confidence = landmarkConfidence,
            formIssues = formIssues,
            angleData = angleData
        )
    }

    /**
     * Analyze pose for push-up detection
     */
    fun analyzePushup(pose: Pose): ExerciseResult {
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
        val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
        val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)

        // Check if we have all the landmarks we need
        if (leftShoulder == null || rightShoulder == null ||
            leftElbow == null || rightElbow == null ||
            leftWrist == null || rightWrist == null ||
            leftHip == null || rightHip == null ||
            leftAnkle == null || rightAnkle == null) {
            return ExerciseResult(
                exerciseType = ExerciseType.PUSHUP,
                currentState = ExerciseState.INVALID,
                repCount = repCount,
                confidence = 0.0f,
                formIssues = listOf("Not all required landmarks detected"),
                angleData = emptyMap()
            )
        }

        // Calculate elbow angles
        val leftElbowAngle = calculateAngle(
            leftShoulder.position,
            leftElbow.position,
            leftWrist.position
        )

        val rightElbowAngle = calculateAngle(
            rightShoulder.position,
            rightElbow.position,
            rightWrist.position
        )

        // Average elbow angle
        val elbowAngle = (leftElbowAngle + rightElbowAngle) / 2

        // Calculate body alignment (should be straight)
        val bodyAlignment = calculateBodyAlignment(
            leftShoulder.position, rightShoulder.position,
            leftHip.position, rightHip.position,
            leftAnkle.position, rightAnkle.position
        )

        // Determine form issues
        val formIssues = mutableListOf<String>()

        if (abs(bodyAlignment) > PUSHUP_BODY_ALIGNMENT_THRESHOLD) {
            formIssues.add("Body not straight")
        }

        if (abs(leftElbowAngle - rightElbowAngle) > 20) {
            formIssues.add("Uneven arm bend")
        }

        // Calculate overall confidence based on landmark confidence
        val landmarkConfidence = listOf(
            leftShoulder, rightShoulder, leftElbow, rightElbow,
            leftWrist, rightWrist, leftHip, rightHip,
            leftAnkle, rightAnkle
        ).map { it.inFrameLikelihood }.average().toFloat()

        // Determine current state
        val currentState = when {
            landmarkConfidence < 0.7f -> ExerciseState.INVALID
            elbowAngle < PUSHUP_ELBOW_ANGLE_THRESHOLD_DOWN -> ExerciseState.DOWN
            elbowAngle > PUSHUP_ELBOW_ANGLE_THRESHOLD_UP -> ExerciseState.UP
            else -> lastExerciseState // Maintain previous state if in between
        }

        // Count reps with debounce to prevent jitter
        if (currentState != lastExerciseState) {
            stateChangeDebounce++
            if (stateChangeDebounce >= DEBOUNCE_THRESHOLD) {
                // State change confirmed
                if (currentState == ExerciseState.DOWN && lastExerciseState == ExerciseState.UP) {
                    // Started a rep
                    isRepInProgress = true
                } else if (currentState == ExerciseState.UP && lastExerciseState == ExerciseState.DOWN && isRepInProgress) {
                    // Completed a rep
                    repCount++
                    isRepInProgress = false
                }

                lastExerciseState = currentState
                stateChangeDebounce = 0
            }
        } else {
            stateChangeDebounce = 0
        }

        val angleData = mapOf(
            "leftElbowAngle" to leftElbowAngle,
            "rightElbowAngle" to rightElbowAngle,
            "bodyAlignment" to bodyAlignment
        )

        Log.d(TAG, "Push-up analysis: state=$currentState, repCount=$repCount, " +
                "elbowAngle=$elbowAngle, issues=$formIssues")

        return ExerciseResult(
            exerciseType = ExerciseType.PUSHUP,
            currentState = currentState,
            repCount = repCount,
            confidence = landmarkConfidence,
            formIssues = formIssues,
            angleData = angleData
        )
    }

    /**
     * Reset exercise tracking state
     */
    fun resetExercise() {
        lastExerciseState = ExerciseState.WAITING
        repCount = 0
        isRepInProgress = false
        stateChangeDebounce = 0
    }

    /**
     * Calculate angle between three points
     */
    private fun calculateAngle(point1: PointF, point2: PointF, point3: PointF): Double {
        val angle1 = atan2((point1.y - point2.y).toDouble(), (point1.x - point2.x).toDouble())
        val angle2 = atan2((point3.y - point2.y).toDouble(), (point3.x - point2.x).toDouble())

        var result = Math.toDegrees(angle1 - angle2)
        if (result < 0) {
            result += 360.0
        }

        // Convert to 0-180 range
        if (result > 180) {
            result = 360.0 - result
        }

        return result
    }

    /**
     * Calculate hip-knee alignment for squats
     */
    private fun calculateHipKneeAlignment(
        leftHip: PointF, rightHip: PointF,
        leftKnee: PointF, rightKnee: PointF
    ): Double {
        // Calculate midpoints
        val midHip = PointF(
            (leftHip.x + rightHip.x) / 2,
            (leftHip.y + rightHip.y) / 2
        )

        val midKnee = PointF(
            (leftKnee.x + rightKnee.x) / 2,
            (leftKnee.y + rightKnee.y) / 2
        )

        // Calculate vertical alignment
        val deltaX = midKnee.x - midHip.x
        val deltaY = midKnee.y - midHip.y

        val angleRadians = atan2(deltaX.toDouble(), deltaY.toDouble())
        return Math.toDegrees(angleRadians)
    }

    /**
     * Calculate body alignment for push-ups
     */
    private fun calculateBodyAlignment(
        leftShoulder: PointF, rightShoulder: PointF,
        leftHip: PointF, rightHip: PointF,
        leftAnkle: PointF, rightAnkle: PointF
    ): Double {
        // Calculate midpoints
        val midShoulder = PointF(
            (leftShoulder.x + rightShoulder.x) / 2,
            (leftShoulder.y + rightShoulder.y) / 2
        )

        val midHip = PointF(
            (leftHip.x + rightHip.x) / 2,
            (leftHip.y + rightHip.y) / 2
        )

        val midAnkle = PointF(
            (leftAnkle.x + rightAnkle.x) / 2,
            (leftAnkle.y + rightAnkle.y) / 2
        )

        // Calculate angle between shoulder-hip and hip-ankle
        val angle1 = atan2(
            (midShoulder.y - midHip.y).toDouble(),
            (midShoulder.x - midHip.x).toDouble()
        )

        val angle2 = atan2(
            (midAnkle.y - midHip.y).toDouble(),
            (midAnkle.x - midHip.x).toDouble()
        )

        var result = Math.toDegrees(angle1 - angle2)
        if (result < 0) {
            result += 360.0
        }

        // Convert to 0-180 range
        if (result > 180) {
            result = 360.0 - result
        }

        return result
    }
}