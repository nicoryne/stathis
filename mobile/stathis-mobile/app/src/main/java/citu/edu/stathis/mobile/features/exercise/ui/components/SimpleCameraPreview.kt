package citu.edu.stathis.mobile.features.exercise.ui.components

import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executor

/**
 * A simple camera preview component that follows the reference implementation pattern
 * This uses direct camera binding instead of the PoseDetectionService
 */
@Composable
fun SimpleCameraPreview(
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA,
    imageAnalysisExecutor: Executor,
    onImageAnalyzerReady: (ImageAnalysis) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    AndroidView(
        factory = { context ->
            // Create PreviewView
            val previewView = PreviewView(context).apply {
                this.scaleType = PreviewView.ScaleType.FILL_CENTER
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            
            // Setup camera
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    
                    // First, unbind all existing use cases
                    cameraProvider.unbindAll()
                    
                    // Create preview use case
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    
                    // Create image analysis use case
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                    
                    // Pass the analyzer to the caller
                    onImageAnalyzerReady(imageAnalysis)
                    
                    // Bind use cases to camera
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                    
                    Log.d("SimpleCameraPreview", "✅ Camera setup successful")
                    
                } catch (e: Exception) {
                    Log.e("SimpleCameraPreview", "❌ Camera binding failed", e)
                }
            }, ContextCompat.getMainExecutor(context))
            
            previewView
        },
        modifier = modifier
    )
}
