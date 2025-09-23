package citu.edu.stathis.mobile.features.exercise.domain.model

data class PostureAnalysis(
    val isCorrectPosture: Boolean,
    val confidenceScore: Float,
    val feedback: String,
    val landmarks: List<PoseLandmark>,
    val timestamp: Long = System.currentTimeMillis()
)

data class PoseLandmark(
    val type: LandmarkType,
    val position: Position,
    val inFrameLikelihood: Float
)

data class Position(
    val x: Float,
    val y: Float,
    val z: Float = 0f
)

enum class LandmarkType {
    NOSE,
    LEFT_EYE_INNER, LEFT_EYE, LEFT_EYE_OUTER,
    RIGHT_EYE_INNER, RIGHT_EYE, RIGHT_EYE_OUTER,
    LEFT_EAR, RIGHT_EAR,
    LEFT_MOUTH, RIGHT_MOUTH,
    LEFT_SHOULDER, RIGHT_SHOULDER,
    LEFT_ELBOW, RIGHT_ELBOW,
    LEFT_WRIST, RIGHT_WRIST,
    LEFT_PINKY, RIGHT_PINKY,
    LEFT_INDEX, RIGHT_INDEX,
    LEFT_THUMB, RIGHT_THUMB,
    LEFT_HIP, RIGHT_HIP,
    LEFT_KNEE, RIGHT_KNEE,
    LEFT_ANKLE, RIGHT_ANKLE,
    LEFT_HEEL, RIGHT_HEEL,
    LEFT_FOOT_INDEX, RIGHT_FOOT_INDEX
} 