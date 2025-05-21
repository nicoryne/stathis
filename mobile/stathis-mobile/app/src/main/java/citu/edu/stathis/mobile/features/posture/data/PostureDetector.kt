package citu.edu.stathis.mobile.features.posture.data

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PostureDetector(
    context: Context,
    private val executor: Executor
) {
    private val TAG = "PostureDetector"

    // Create pose detector with accurate model
    private val options = AccuratePoseDetectorOptions.Builder()
        .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
        .build()

    private val poseDetector: PoseDetector = PoseDetection.getClient(options)
    private val postureAnalyzer = PostureAnalyzer()

    /**
     * Process an image and detect pose
     */
    @OptIn(ExperimentalGetImage::class)
    suspend fun processImageProxy(imageProxy: ImageProxy): PostureResult? {
        return try {
            val mediaImage = imageProxy.image ?: return null
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            val pose = detectPose(image)
            val result = postureAnalyzer.analyzePose(pose)
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error processing image", e)
            null
        } finally {
            imageProxy.close()
        }
    }

    /**
     * Process a bitmap and detect pose
     */
    suspend fun processBitmap(bitmap: Bitmap): PostureResult? {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val pose = detectPose(image)
            val result = postureAnalyzer.analyzePose(pose)
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error processing bitmap", e)
            null
        }
    }

    /**
     * Detect pose from input image
     */
    suspend fun detectPose(image: InputImage): Pose = suspendCancellableCoroutine { continuation ->
        poseDetector.process(image)
            .addOnSuccessListener(executor) { pose ->
                if (continuation.isActive) {
                    continuation.resume(pose)
                }
            }
            .addOnFailureListener(executor) { e ->
                if (continuation.isActive) {
                    continuation.resumeWithException(e)
                }
            }
    }

    /**
     * Close the detector when no longer needed
     */
    fun close() {
        poseDetector.close()
    }
}