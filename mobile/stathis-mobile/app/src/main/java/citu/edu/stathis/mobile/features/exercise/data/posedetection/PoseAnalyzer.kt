package citu.edu.stathis.mobile.features.exercise.data.posedetection

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import com.google.mlkit.vision.pose.PoseLandmark
import cit.edu.stathis.mobile.BuildConfig
import java.util.concurrent.Executor
import android.os.SystemClock

/**
 * Image analyzer for detecting human poses in camera frames using ML Kit.
 * This class processes each frame from the camera and detects poses.
 */
class PoseAnalyzer(
    private val executor: Executor,
    private val onPoseDetected: (Pose, Int, Int, Boolean, Int) -> Unit,
    private val isImageFlipped: Boolean = false,
    private val minAnalysisIntervalMs: Long = 100L // throttle to ~10 FPS by default
) : ImageAnalysis.Analyzer {

    // Configure the pose detector
    private val options = PoseDetectorOptions.Builder()
        .setDetectorMode(PoseDetectorOptions.STREAM_MODE) // For real-time detection
        .build()

    private val poseDetector: PoseDetector = PoseDetection.getClient(options)

    private var lastAnalysisTimestampMs: Long = 0L

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val now = SystemClock.uptimeMillis()
        val elapsed = now - lastAnalysisTimestampMs
        if (elapsed < minAnalysisIntervalMs) {
            imageProxy.close()
            return
        }
        lastAnalysisTimestampMs = now
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val rotation = imageProxy.imageInfo.rotationDegrees
            val image = InputImage.fromMediaImage(mediaImage, rotation)

            // Process the image with ML Kit pose detector
            poseDetector.process(image)
                .addOnSuccessListener(executor) { pose ->
                    if (BuildConfig.APP_ENV == "local") {
                        val landmarks = pose.allPoseLandmarks
                        val count = landmarks.size
                        val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)?.position
                        val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)?.position
                        val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)?.position
                        val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)?.position
                        Log.d(
                            "PoseAnalyzer",
                            "Pose: count=" + count +
                                ", LS=" + (leftShoulder?.x?.toInt()?.toString() + "," + leftShoulder?.y?.toInt()?.toString()) +
                                ", RS=" + (rightShoulder?.x?.toInt()?.toString() + "," + rightShoulder?.y?.toInt()?.toString()) +
                                ", LH=" + (leftHip?.x?.toInt()?.toString() + "," + leftHip?.y?.toInt()?.toString()) +
                                ", RH=" + (rightHip?.x?.toInt()?.toString() + "," + rightHip?.y?.toInt()?.toString())
                        )
                    }
                    // Pass the detected pose along with image dimensions to the callback
                    onPoseDetected(
                        pose,
                        imageProxy.width,
                        imageProxy.height,
                        isImageFlipped,
                        rotation
                    )
                }
                .addOnFailureListener(executor) { e ->
                    // Handle any errors
                    e.printStackTrace()
                }
                .addOnCompleteListener {
                    // Always close the image proxy to release resources
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    /**
     * Releases resources used by the pose detector.
     * Call this method when the analyzer is no longer needed.
     */
    fun shutdown() {
        poseDetector.close()
    }
}
