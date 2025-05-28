package citu.edu.stathis.mobile.features.exercise.data

import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.Pose as MlKitPose

data class LandmarkPoint(
    val type: Int,
    val x: Float,
    val y: Float,
    val z: Float,
    val inFrameLikelihood: Float
)

data class PoseLandmarksData(
    val landmarkPoints: List<LandmarkPoint>
) {
    fun toFloatArrayForBackend(): List<List<List<Float>>> {
        return listOf(landmarkPoints.map { listOf(it.x, it.y, it.z) })
    }
}

fun MlKitPose.toPoseLandmarksData(): PoseLandmarksData {
    val points = mutableListOf<LandmarkPoint>()
    this.allPoseLandmarks.forEach { landmark ->
        points.add(
            LandmarkPoint(
                type = landmark.landmarkType,
                x = landmark.position3D.x,
                y = landmark.position3D.y,
                z = landmark.position3D.z,
                inFrameLikelihood = landmark.inFrameLikelihood
            )
        )
    }
    if (points.size < 33) {
        repeat(33 - points.size) {
            points.add(LandmarkPoint(PoseLandmark.NOSE, 0f,0f,0f,0f))
        }
    }
    return PoseLandmarksData(points.take(33))
}