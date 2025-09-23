package citu.edu.stathis.mobile.features.exercise.data.analysis

import citu.edu.stathis.mobile.features.exercise.data.ExerciseDetector
import citu.edu.stathis.mobile.features.exercise.data.ExerciseResult
import citu.edu.stathis.mobile.features.exercise.data.ExerciseType
import citu.edu.stathis.mobile.features.exercise.data.OnDeviceFeedback
import com.google.mlkit.vision.pose.Pose
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnDeviceExerciseAnalyzer @Inject constructor() {
    private val exerciseDetector = ExerciseDetector()

    fun analyzePose(pose: Pose, exerciseType: ExerciseType): OnDeviceFeedback {
        val exerciseResult: ExerciseResult = when (exerciseType) {
            ExerciseType.SQUAT -> exerciseDetector.analyzeSquat(pose)
            ExerciseType.PUSHUP -> exerciseDetector.analyzePushup(pose)
        }

        return OnDeviceFeedback(
            exerciseType = exerciseType,
            exerciseState = exerciseResult.state,
            repCount = exerciseResult.repCount,
            formIssues = exerciseResult.feedback,
            confidence = exerciseResult.confidence ?: 0.0f,
            angleData = emptyMap()
        )
    }

    fun resetExerciseState() {
        exerciseDetector.resetExercise()
    }
}
