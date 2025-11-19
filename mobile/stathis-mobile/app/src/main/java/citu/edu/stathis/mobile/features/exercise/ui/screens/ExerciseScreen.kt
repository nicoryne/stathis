package citu.edu.stathis.mobile.features.exercise.ui.screens

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.view.Surface as AndroidSurface
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import citu.edu.stathis.mobile.features.exercise.data.posedetection.PoseAnalyzer
import citu.edu.stathis.mobile.features.exercise.recording.ScreenRecordService
import citu.edu.stathis.mobile.features.exercise.ui.components.PoseSkeletonOverlayView
import citu.edu.stathis.mobile.features.vitals.ui.HealthCompactIndicator
import com.google.mlkit.vision.pose.Pose
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun ExerciseScreen(
    navController: NavHostController,
    enableVitalsIndicator: Boolean = false,
    enablePostureAnalysis: Boolean = true
) {
    val exerciseViewModel: citu.edu.stathis.mobile.features.exercise.ui.viewmodel.ExerciseViewModel = hiltViewModel()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val exerciseState by exerciseViewModel.uiState.collectAsState()

    var latestPose by remember { mutableStateOf<Pose?>(null) }
    var frameWidth by remember { mutableStateOf(0) }
    var frameHeight by remember { mutableStateOf(0) }
    var rotation by remember { mutableStateOf(0) }
    var useFrontCamera by remember { mutableStateOf(true) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var torchOn by remember { mutableStateOf(false) }
    var linearZoom by remember { mutableStateOf(0.5f) }
    var isRecording by remember { mutableStateOf(false) }
    var exposureRange by remember { mutableStateOf(IntRange(0, 0)) }
    var exposureIndex by remember { mutableStateOf(0) }

    val mediaProjectionManager = remember { context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager }
    var mediaProjection by remember { mutableStateOf<MediaProjection?>(null) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var virtualDisplay by remember { mutableStateOf<VirtualDisplay?>(null) }
    var outputPfd by remember { mutableStateOf<ParcelFileDescriptor?>(null) }

    val stopScreenRecording = {
        try { mediaRecorder?.stop() } catch (_: Exception) {}
        try { mediaRecorder?.release() } catch (_: Exception) {}
        try { virtualDisplay?.release() } catch (_: Exception) {}
        try { mediaProjection?.stop() } catch (_: Exception) {}
        try { outputPfd?.close() } catch (_: Exception) {}
        mediaRecorder = null
        virtualDisplay = null
        mediaProjection = null
        outputPfd = null
        isRecording = false
    }

    val screenRecordLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val intent = Intent(context, ScreenRecordService::class.java).apply {
            putExtra("resultCode", result.resultCode)
            putExtra("data", result.data)
        }
        context.startForegroundService(intent)
        isRecording = true
    }

    DisposableEffect(lifecycleOwner, isRecording) {
        val lifecycle = lifecycleOwner.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            if (isRecording && (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP || event == Lifecycle.Event.ON_DESTROY)) {
                stopScreenRecording()
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    val cameraProvider = remember { ProcessCameraProvider.getInstance(context) }
    val analysisExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val configuration = LocalConfiguration.current
    val controlLengthDp = remember(configuration) {
        ((configuration.screenHeightDp * 0.35f).coerceIn(180f, 260f)).dp
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
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
                                onPoseDetected = { pose, w, h, flipped, rot ->
                                    latestPose = pose
                                    frameWidth = w
                                    frameHeight = h
                                    rotation = rot
                                    if (enablePostureAnalysis) {
                                        // Send to pose classification
                                        val poseLandmarks = (0 until 33).mapNotNull { idx ->
                                            val lm = pose.getPoseLandmark(idx) ?: return@mapNotNull null
                                            citu.edu.stathis.mobile.features.exercise.ui.util.Landmark(
                                                x = lm.position.x / w.toFloat(),
                                                y = lm.position.y / h.toFloat(),
                                                z = lm.position3D.z,
                                                v = lm.inFrameLikelihood
                                            )
                                        }
                                        if (poseLandmarks.size == 33) {
                                            exerciseViewModel.onFrame(poseLandmarks)
                                        }
                                    }
                                },
                                isImageFlipped = useFrontCamera
                            )
                        )
                    }

                try {
                    provider.unbindAll()
                    val boundCamera = provider.bindToLifecycle(
                        lifecycleOwner,
                        if (useFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                    camera = boundCamera
                    val z = boundCamera.cameraInfo.zoomState.value
                    if (z != null) {
                        linearZoom = z.linearZoom
                    }
                    val expState = boundCamera.cameraInfo.exposureState
                    exposureRange = IntRange(expState.exposureCompensationRange.lower, expState.exposureCompensationRange.upper)
                    exposureIndex = expState.exposureCompensationIndex
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        )

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx -> PoseSkeletonOverlayView(ctx) },
            update = { view ->
                view.updatePose(latestPose, frameWidth, frameHeight, rotation, useFrontCamera)
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(camera, linearZoom) {
                    detectTransformGestures { _, _, zoomChange, _ ->
                        val cam = camera ?: return@detectTransformGestures
                        val newZoom = (linearZoom * zoomChange).coerceIn(0f, 1f)
                        cam.cameraControl.setLinearZoom(newZoom)
                        linearZoom = newZoom
                    }
                }
        )

        // Top controls - only camera controls, no back button for fullscreen experience
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                ) {
                    IconButton(onClick = {
                        if (!isRecording) useFrontCamera = !useFrontCamera
                    }, enabled = !isRecording) {
                        Icon(
                            imageVector = Icons.Filled.Cameraswitch,
                            contentDescription = "Switch camera",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                ) {
                    val hasFlash = camera?.cameraInfo?.hasFlashUnit() == true
                    IconButton(
                        enabled = hasFlash,
                        onClick = {
                            val cam = camera
                            if (cam != null && hasFlash) {
                                torchOn = !torchOn
                                cam.cameraControl.enableTorch(torchOn)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (torchOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                            contentDescription = if (torchOn) "Torch on" else "Torch off",
                            tint = if (hasFlash) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    color = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                ) {
                    IconButton(onClick = {
                        if (!isRecording) {
                            screenRecordLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
                        } else {
                            try {
                                context.stopService(
                                    Intent(
                                        context,
                                        ScreenRecordService::class.java
                                    ).setAction("STOP"))
                            } catch (_: Exception) {}
                            stopScreenRecording()
                        }
                    }) {
                        Icon(
                            imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.FiberManualRecord,
                            contentDescription = if (isRecording) "Stop recording" else "Start recording",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }

        val screenHeight = LocalConfiguration.current.screenHeightDp.dp

        // Vitals indicator positioned between header and zoom scale
        if (enableVitalsIndicator) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 88.dp, end = 8.dp)
            ) {
                HealthCompactIndicator()
            }
        }

        Column(
            modifier = Modifier
                .heightIn(min = 160.dp, max = (screenHeight * 0.45f))
                .width(64.dp)
                .align(Alignment.CenterEnd)
                .padding(
                    end = 8.dp, 
                    top = if (enableVitalsIndicator) 160.dp else 0.dp,
                    bottom = 16.dp
                )
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.WbSunny,
                        contentDescription = "Exposure",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (exposureRange.first != exposureRange.last) {
                        val minIdx = exposureRange.first
                        val maxIdx = exposureRange.last
                        val sliderValue = remember(exposureIndex, minIdx, maxIdx) {
                            (exposureIndex - minIdx).toFloat() / (maxIdx - minIdx).toFloat()
                        }
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Slider(
                                value = sliderValue,
                                onValueChange = { f ->
                                    val cam = camera ?: return@Slider
                                    val idx = (minIdx + f * (maxIdx - minIdx)).toInt()
                                        .coerceIn(minIdx, maxIdx)
                                    exposureIndex = idx
                                    cam.cameraControl.setExposureCompensationIndex(idx)
                                },
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .graphicsLayer { rotationZ = -90f }
                            )
                        }
                    }
                }
            }

            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ZoomIn,
                        contentDescription = "Zoom",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Slider(
                            value = linearZoom,
                            onValueChange = { v ->
                                linearZoom = v
                                camera?.cameraControl?.setLinearZoom(v)
                            },
                            modifier = Modifier
                                .fillMaxHeight()
                                .graphicsLayer { rotationZ = -90f }
                        )
                    }
                }
            }
        }

        // Pose classification results overlay
        if (exerciseState.predictedClass.isNotEmpty()) {
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp, bottom = 12.dp)
                    .navigationBarsPadding()
                    .widthIn(max = 300.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    // Main classification result
                    Text(
                        text = "Pose: ${exerciseState.predictedClass}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (exerciseState.score > 0) {
                        Text(
                            text = "Confidence: ${(exerciseState.score * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Show all messages
                    if (exerciseState.messages.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        exerciseState.messages.forEach { message ->
                            Text(
                                text = "• $message",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    // Show flags if any
                    if (exerciseState.flags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Flags: ${exerciseState.flags.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    
                    // Show top probabilities if available
                    if (exerciseState.probabilities.isNotEmpty() && exerciseState.classNames.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Top probabilities:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val topResults = exerciseState.probabilities
                            .zip(exerciseState.classNames)
                            .sortedByDescending { it.first }
                            .take(3)
                        topResults.forEach { (prob, className) ->
                            Text(
                                text = "  ${className}: ${(prob * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}


