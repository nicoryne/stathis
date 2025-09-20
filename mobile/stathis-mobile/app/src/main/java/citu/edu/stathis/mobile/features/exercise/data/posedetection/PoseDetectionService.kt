package citu.edu.stathis.mobile.features.exercise.data.posedetection

import android.content.Context
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PoseDetectionService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val options = AccuratePoseDetectorOptions.Builder()
        .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
        .build()
    
    private val poseDetector: PoseDetector = PoseDetection.getClient(options)
    private val mlExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    @OptIn(ExperimentalGetImage::class)
    suspend fun processImageProxy(imageProxy: ImageProxy): Pose? = withContext(Dispatchers.Default) {
        val mediaImage = imageProxy.image ?: return@withContext null
        
        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )
        
        try {
            poseDetector.process(image).await()
        } catch (e: Exception) {
            null
        } finally {
            withContext(Dispatchers.Main) {
                imageProxy.close()
            }
        }
    }

    fun close() {
        poseDetector.close()
        mlExecutor.shutdown()
    }
}