package citu.edu.stathis.mobile.features.exercise.data

import citu.edu.stathis.mobile.features.exercise.data.model.ExerciseState
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.abs


class ExerciseDetector {

    // --- Squat ---
    private var squatState: ExerciseState = ExerciseState.WAITING
    private var squatRepCount: Int = 0
    private var squatInDownPosition: Boolean = false
    private val squatHipKneeVerticalThresholdFactor = 0.1f
    private val squatMovementThreshold = 0.05f

    // --- Push-up ---
    private var pushupState: ExerciseState = ExerciseState.WAITING
    private var pushupRepCount: Int = 0
    private var pushupInDownPosition: Boolean = false
    private val pushupVerticalMovementThresholdFactor = 0.15f
    private var initialShoulderYPushup: Float? = null


    fun analyzeSquat(pose: Pose): ExerciseResult {
        val feedback = mutableListOf<String>()
        var repCompletedThisFrame = false

        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
        val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
        val rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
        // For vertical reference, maybe shoulders and ankles
        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
        val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)


        if (leftHip == null || rightHip == null || leftKnee == null || rightKnee == null || leftShoulder == null || rightShoulder == null || leftAnkle == null || rightAnkle == null) {
            feedback.add("Ensure major body parts are visible.")
            resetSquatStateInternals()
            return ExerciseResult(ExerciseState.INVALID, feedback, repCount = squatRepCount)
        }

        val avgHipY = (leftHip.position.y + rightHip.position.y) / 2f
        val avgKneeY = (leftKnee.position.y + rightKnee.position.y) / 2f
        val avgShoulderY = (leftShoulder.position.y + rightShoulder.position.y) / 2f
        val avgAnkleY = (leftAnkle.position.y + rightAnkle.position.y) / 2f
        val bodyHeightEstimate = abs(avgShoulderY - avgAnkleY)


        when (squatState) {
            ExerciseState.WAITING, ExerciseState.UP -> {
                if (avgHipY > avgKneeY + (bodyHeightEstimate * squatHipKneeVerticalThresholdFactor)) {
                    squatState = ExerciseState.DOWN
                    squatInDownPosition = true
                } else {
                    squatState = ExerciseState.UP
                }
            }
            ExerciseState.DOWN -> {
                if (avgHipY < avgKneeY) {
                    squatState = ExerciseState.UP
                    if (squatInDownPosition) {
                        squatRepCount++
                        repCompletedThisFrame = true
                    }
                    squatInDownPosition = false
                }
            }
            ExerciseState.INVALID -> {
                squatState = ExerciseState.WAITING
            }
        }
        val confidence = (leftHip.inFrameLikelihood + rightHip.inFrameLikelihood + leftKnee.inFrameLikelihood + rightKnee.inFrameLikelihood) / 4f

        return ExerciseResult(squatState, feedback, repCompletedThisFrame, confidence, squatRepCount)
    }

    fun analyzePushup(pose: Pose): ExerciseResult {
        val feedback = mutableListOf<String>()
        var repCompletedThisFrame = false

        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
         val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
         val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)


        if (leftShoulder == null || rightShoulder == null || leftElbow == null || rightElbow == null) {
            feedback.add("Ensure shoulders and elbows are visible.")
            resetPushupStateInternals()
            initialShoulderYPushup = null
            return ExerciseResult(ExerciseState.INVALID, feedback, repCount = pushupRepCount)
        }

        val avgShoulderY = (leftShoulder.position.y + rightShoulder.position.y) / 2f

        if (initialShoulderYPushup == null && (pushupState == ExerciseState.WAITING || pushupState == ExerciseState.UP)) {
            initialShoulderYPushup = avgShoulderY
        }


        when (pushupState) {
            ExerciseState.WAITING, ExerciseState.UP -> {
                initialShoulderYPushup?.let { initialY ->

                    if (avgShoulderY > initialY + (abs(initialY) * pushupVerticalMovementThresholdFactor)) {
                        pushupState = ExerciseState.DOWN
                        pushupInDownPosition = true
                    } else {
                        pushupState = ExerciseState.UP
                    }
                }
                if (initialShoulderYPushup == null) pushupState = ExerciseState.WAITING

            }
            ExerciseState.DOWN -> {
                initialShoulderYPushup?.let { initialY ->
                    if (avgShoulderY <= initialY + (abs(initialY) * pushupVerticalMovementThresholdFactor * 0.5f)) {
                        pushupState = ExerciseState.UP
                        if (pushupInDownPosition) {
                            pushupRepCount++
                            repCompletedThisFrame = true
                        }
                        pushupInDownPosition = false
//                         initialShoulderYPushup = null
                        initialShoulderYPushup = avgShoulderY
                    }
                }
            }
            ExerciseState.INVALID -> {
                pushupState = ExerciseState.WAITING
                initialShoulderYPushup = null
            }
        }

        val confidence = (leftShoulder.inFrameLikelihood + rightShoulder.inFrameLikelihood + leftElbow.inFrameLikelihood + rightElbow.inFrameLikelihood) / 4f

        return ExerciseResult(pushupState, feedback, repCompletedThisFrame, confidence, pushupRepCount)
    }
    private fun resetSquatStateInternals() {
        squatState = ExerciseState.WAITING
        squatInDownPosition = false
    }

    private fun resetPushupStateInternals() {
        pushupState = ExerciseState.WAITING
        pushupInDownPosition = false
        initialShoulderYPushup = null
    }


    fun resetExercise() {
        squatState = ExerciseState.WAITING
        squatRepCount = 0
        squatInDownPosition = false

        pushupState = ExerciseState.WAITING
        pushupRepCount = 0
        pushupInDownPosition = false
        initialShoulderYPushup = null
    }

}
