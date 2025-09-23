package citu.edu.stathis.mobile.features.exercise.data.posedetection

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import java.util.concurrent.Executor

/**
 * Image analyzer for detecting human poses in camera frames using ML Kit.
 * This class processes each frame from the camera and detects poses.
 */
class PoseAnalyzer(
    private val executor: Executor,
    private val onPoseDetected: (Pose, Int, Int, Boolean) -> Unit,
    private val isImageFlipped: Boolean = false
) : ImageAnalysis.Analyzer {

    // Configure the pose detector
    private val options = PoseDetectorOptions.Builder()
        .setDetectorMode(PoseDetectorOptions.STREAM_MODE) // For real-time detection
        .build()

    private val poseDetector: PoseDetector = PoseDetection.getClient(options)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            // Process the image with ML Kit pose detector
            poseDetector.process(image)
                .addOnSuccessListener(executor) { pose ->
                    // Pass the detected pose along with image dimensions to the callback
                    onPoseDetected(
                        pose,
                        imageProxy.width,
                        imageProxy.height,
                        isImageFlipped
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
