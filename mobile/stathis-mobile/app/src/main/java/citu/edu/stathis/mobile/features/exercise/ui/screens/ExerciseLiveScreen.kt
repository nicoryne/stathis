package citu.edu.stathis.mobile.features.exercise.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import citu.edu.stathis.mobile.features.exercise.ui.viewmodel.ExerciseViewModel
import androidx.camera.core.AspectRatio
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import citu.edu.stathis.mobile.features.exercise.data.posedetection.PoseAnalyzer
import citu.edu.stathis.mobile.features.exercise.ui.util.Landmark
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun ExerciseLiveScreen(
    viewModel: ExerciseViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProvider = remember { ProcessCameraProvider.getInstance(context) }
    val analysisExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    var frameWidth by remember { mutableStateOf(0) }
    var frameHeight by remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                previewView
            },
            update = { previewView ->
                val provider = cameraProvider.get()
                val preview = Preview.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                    .build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                    .setTargetRotation(previewView.display?.rotation ?: android.view.Surface.ROTATION_0)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build().also { analysis ->
                        analysis.setAnalyzer(
                            analysisExecutor,
                            PoseAnalyzer(
                                executor = analysisExecutor,
                                onPoseDetected = { pose, w, h, _, _ ->
                                    frameWidth = w
                                    frameHeight = h
                                    // Build landmarks and forward to ViewModel
                                    val landmarks: List<Landmark> = (0 until 33).mapNotNull { idx ->
                                        val lm = pose.getPoseLandmark(idx) ?: return@mapNotNull null
                                        Landmark(
                                            x = lm.position.x / w.toFloat(),
                                            y = lm.position.y / h.toFloat(),
                                            z = lm.position3D.z,
                                            v = lm.inFrameLikelihood
                                        )
                                    }
                                    if (landmarks.size == 33) {
                                        viewModel.onFrame(landmarks)
                                    }
                                },
                                isImageFlipped = true
                            )
                        )
                    }

                try {
                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        androidx.camera.core.CameraSelector.DEFAULT_FRONT_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            try { analysisExecutor.shutdown() } catch (_: Exception) {}
        }
    }
}

