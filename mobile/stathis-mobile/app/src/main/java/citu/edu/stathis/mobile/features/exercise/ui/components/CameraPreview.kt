package citu.edu.stathis.mobile.features.exercise.ui.components

import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import timber.log.Timber
import java.util.concurrent.Executor

/**
 * A composable that displays the camera preview for exercise pose detection
 */
@Composable
fun CameraPreview(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    cameraSelector: CameraSelector,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            // Create PreviewView
            val previewView = PreviewView(context).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_CENTER
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            // Get the executor
            val executor = ContextCompat.getMainExecutor(context)

            // Setup camera
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                bindCameraPreview(
                    lifecycleOwner = lifecycleOwner,
                    previewView = previewView,
                    cameraProvider = cameraProvider,
                    cameraSelector = cameraSelector,
                    executor = executor
                )
            }, executor)

            previewView
        },
        update = { previewView ->
            // Update camera selector when it changes
            val executor = ContextCompat.getMainExecutor(previewView.context)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(previewView.context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                // Unbind all use cases before binding again
                cameraProvider.unbindAll()

                // Re-bind with the new camera selector
                bindCameraPreview(
                    lifecycleOwner = lifecycleOwner,
                    previewView = previewView,
                    cameraProvider = cameraProvider,
                    cameraSelector = cameraSelector,
                    executor = executor
                )
            }, executor)
        }
    )
}

private fun bindCameraPreview(
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    cameraProvider: ProcessCameraProvider,
    cameraSelector: CameraSelector,
    executor: Executor
) {
    try {
        // Unbind all use cases
        cameraProvider.unbindAll()

        // Create Preview use case
        val preview = Preview.Builder()
            .build()
            .also { it.setSurfaceProvider(previewView.surfaceProvider) }

        // Bind use cases to camera
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview
        )
    } catch (e: Exception) {
        Timber.e(e, "Failed to bind camera use cases")
    }
}


