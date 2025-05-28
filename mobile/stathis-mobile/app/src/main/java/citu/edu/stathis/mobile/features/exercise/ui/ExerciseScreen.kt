package citu.edu.stathis.mobile.features.exercise.ui

import android.Manifest
import android.graphics.RenderEffect
import android.graphics.Shader
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.ViewGroup
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview as CameraXPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import citu.edu.stathis.mobile.core.theme.BrandColors
import citu.edu.stathis.mobile.features.exercise.data.Exercise
import citu.edu.stathis.mobile.features.exercise.data.OnDeviceFeedback
import citu.edu.stathis.mobile.features.exercise.data.PoseLandmarksData
import citu.edu.stathis.mobile.features.exercise.data.model.BackendPostureAnalysis
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.material.chip.Chip
import com.google.mlkit.vision.pose.PoseLandmark
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ExerciseScreen(
    navController: NavHostController,
    viewModel: ExerciseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val viewEvents by viewModel.events.collectAsStateWithLifecycle(initialValue = null)
    val snackbarHostState = remember { SnackbarHostState() }

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(cameraPermissionState.status) {
        if (cameraPermissionState.status.isGranted) {
            viewModel.onCameraPermissionGranted()
        }
    }

    LaunchedEffect(viewEvents) {
        viewEvents?.let { event ->
            when(event) {
                is ExerciseViewEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is ExerciseViewEvent.NavigateToExerciseSelection -> {
                    viewModel.loadExercises()
                }
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val currentUiState = uiState) {
                ExerciseScreenUiState.Initial -> {
                    LaunchedEffect(Unit) {
                        if (cameraPermissionState.status.isGranted) {
                            viewModel.onCameraPermissionGranted()
                        } else {
                            viewModel.onCameraPermissionDenied()
                        }
                    }
                    LoadingIndicator("Initializing...")
                }
                ExerciseScreenUiState.PermissionNeeded -> {
                    CameraPermissionRequestHandler(
                        onGrantPermission = { cameraPermissionState.launchPermissionRequest() }
                    )
                }
                is ExerciseScreenUiState.ExerciseSelection -> {
                    if (currentUiState.isLoading) LoadingIndicator("Fetching Exercises...")
                    else ExerciseSelectionContent(
                        exercises = currentUiState.exercises,
                        onExerciseSelected = { viewModel.selectExercise(it) }
                    )
                }
                is ExerciseScreenUiState.ExerciseIntroduction -> {
                    ExerciseIntroductionContent(
                        exercise = currentUiState.exercise,
                        onStartSession = { viewModel.startExerciseSession(currentUiState.exercise) },
                        onBackToSelection = { viewModel.loadExercises() }
                    )
                }
                is ExerciseScreenUiState.ExerciseActive -> {
                    ExerciseActiveContent(
                        activeState = currentUiState,
                        onPoseDetected = { pose, exercise -> viewModel.onPoseDetected(pose, exercise) },
                        onStopSession = { viewModel.stopExerciseSession() },
                        onToggleCamera = { viewModel.toggleCamera(currentUiState.currentCameraSelector) }
                    )
                }
                is ExerciseScreenUiState.ExerciseSummary -> {
                    ExerciseSummaryContent(
                        message = currentUiState.message,
                        onBackToSelection = { viewModel.loadExercises() }
                    )
                }
                is ExerciseScreenUiState.Error -> {
                    ErrorScreen(
                        message = currentUiState.message,
                        onRetry = {
                            if (cameraPermissionState.status.isGranted) viewModel.loadExercises()
                            else cameraPermissionState.launchPermissionRequest()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CameraPermissionRequestHandler(onGrantPermission: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.PhotoCamera, contentDescription = "Camera Permission", modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Camera permission is required to analyze your exercises.", textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onGrantPermission) { Text("Grant Permission") }
    }
}

@Composable
fun ExerciseSelectionContent(
    exercises: List<Exercise>,
    onExerciseSelected: (Exercise) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                text = "Available Exercises",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        items(exercises) { exercise ->
            ExerciseCard(exercise = exercise, onClick = { onExerciseSelected(exercise) })
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ExerciseCard(exercise: Exercise, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = exercise.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                exercise.difficulty?.let {
                    FilterChip(
                        selected = false,
                        onClick = { },
                        label = { Text(it) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = when (it) {
                                "BEGINNER" -> Color(0xFF4CAF50)
                                "INTERMEDIATE" -> Color(0xFFFFA000)
                                "ADVANCED" -> Color(0xFFF44336)
                                else -> MaterialTheme.colorScheme.primary
                            }.copy(alpha = 0.1f),
                            labelColor = when (it) {
                                "BEGINNER" -> Color(0xFF4CAF50)
                                "INTERMEDIATE" -> Color(0xFFFFA000)
                                "ADVANCED" -> Color(0xFFF44336)
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                    )
                }
                exercise.targetMuscles?.let { muscles ->
                    Text(
                        text = muscles.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ExerciseIntroductionContent(
    exercise: Exercise,
    onStartSession: () -> Unit,
    onBackToSelection: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        IconButton(onClick = onBackToSelection) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }
        Text(
            text = exercise.name,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Text(
            text = exercise.description,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Text(
            text = "Instructions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        exercise.instructions.forEachIndexed { index, instruction ->
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "${index + 1}.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(end = 8.dp, top = 2.dp)
                )
                Text(
                    text = instruction,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onStartSession,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text("Start Exercise")
        }
    }
}

@Composable
fun ExerciseActiveContent(
    activeState: ExerciseScreenUiState.ExerciseActive,
    onPoseDetected: (com.google.mlkit.vision.pose.Pose, Exercise) -> Unit,
    onStopSession: () -> Unit,
    onToggleCamera: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Create pose detector with optimized settings
    val options = remember {
        AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
            .build()
    }
    val poseDetector = remember { PoseDetection.getClient(options) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview in background
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
            }
        ) { previewView ->
            // Camera setup code remains the same
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                
                try {
                    val preview = CameraXPreview.Builder()
                        .build()
                        .also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                        .setTargetRotation(Surface.ROTATION_0)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(
                                ContextCompat.getMainExecutor(context)
                            ) { imageProxy ->
                                val mediaImage = imageProxy.image
                                if (mediaImage != null) {
                                    val image = InputImage.fromMediaImage(
                                        mediaImage,
                                        imageProxy.imageInfo.rotationDegrees
                                    )
                                    
                                    poseDetector.process(image)
                                        .addOnSuccessListener { pose ->
                                            onPoseDetected(pose, activeState.selectedExercise)
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("ExerciseScreen", "Pose detection failed", e)
                                        }
                                        .addOnCompleteListener {
                                            imageProxy.close()
                                        }
                                } else {
                                    imageProxy.close()
                                }
                            }
                        }

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        activeState.currentCameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    Log.e("ExerciseActiveContent", "Use case binding failed", e)
                }
            }, ContextCompat.getMainExecutor(context))
        }

        // Skeleton overlay on top with proper z-index
        Box(modifier = Modifier.fillMaxSize()) {
            val currentLandmarks = remember(activeState.currentPoseLandmarks) { activeState.currentPoseLandmarks }
            PoseSkeletonOverlay(landmarksData = currentLandmarks)
        }

        // UI controls on top
        Column(modifier = Modifier.fillMaxSize()) {
            ExerciseHeader(
                exerciseName = activeState.selectedExercise.name,
                timerMs = activeState.sessionTimerMs,
                onToggleCamera = onToggleCamera,
                currentCameraSelector = activeState.currentCameraSelector
            )
            Spacer(modifier = Modifier.weight(1f))
            ExerciseFeedbackAndStatsFooter(
                backendAnalysis = activeState.backendAnalysis,
                repCount = activeState.repCount,
                onStopSession = onStopSession
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            poseDetector.close()
        }
    }
}

@Composable
fun ExerciseHeader(exerciseName: String, timerMs: Long, onToggleCamera: () -> Unit, currentCameraSelector: CameraSelector) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.3f)).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(exerciseName, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(formatDuration(timerMs), style = MaterialTheme.typography.titleLarge, color = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onToggleCamera) {
                Icon(
                    imageVector = if (currentCameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) Icons.Filled.Cameraswitch else Icons.Filled.FlipCameraAndroid,
                    contentDescription = "Toggle Camera",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun PoseSkeletonOverlay(landmarksData: PoseLandmarksData?) {
    if (landmarksData == null) return

    // Cache colors and stroke widths
    val colors = remember {
        object {
            val skeletonLine = Color.Green.copy(alpha = 0.8f)
            val jointPoint = Color.Red.copy(alpha = 0.9f)
            val confidenceThreshold = 0.5f
            val strokeWidth = 8f  // Increased for better visibility
            val jointRadius = 8.dp // Increased for better visibility
        }
    }

    // Cache connections with their visualization properties
    val connections = remember {
        listOf(
            // Torso
            Connection(PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER, colors.skeletonLine, 8f),
            Connection(PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP, colors.skeletonLine, 8f),
            Connection(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP, colors.skeletonLine, 8f),
            Connection(PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP, colors.skeletonLine, 8f),
            // Arms
            Connection(PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW, colors.skeletonLine, 6f),
            Connection(PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST, colors.skeletonLine, 6f),
            Connection(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW, colors.skeletonLine, 6f),
            Connection(PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST, colors.skeletonLine, 6f),
            // Legs
            Connection(PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE, colors.skeletonLine, 6f),
            Connection(PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE, colors.skeletonLine, 6f),
            Connection(PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE, colors.skeletonLine, 6f),
            Connection(PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE, colors.skeletonLine, 6f)
        )
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(alpha = 1f) // Removed blur effect for better visibility
    ) {
        // Draw connections first (skeleton lines)
        connections.forEach { connection ->
            val startPoint = landmarksData.landmarkPoints.find { it.type == connection.start }
            val endPoint = landmarksData.landmarkPoints.find { it.type == connection.end }

            if (startPoint != null && endPoint != null &&
                startPoint.inFrameLikelihood > colors.confidenceThreshold &&
                endPoint.inFrameLikelihood > colors.confidenceThreshold
            ) {
                drawLine(
                    color = connection.color,
                    start = Offset(startPoint.x * size.width, startPoint.y * size.height),
                    end = Offset(endPoint.x * size.width, endPoint.y * size.height),
                    strokeWidth = connection.strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }

        // Draw joints on top
        landmarksData.landmarkPoints.forEach { point ->
            if (point.inFrameLikelihood > colors.confidenceThreshold) {
                drawCircle(
                    color = colors.jointPoint,
                    radius = colors.jointRadius.toPx(),
                    center = Offset(point.x * size.width, point.y * size.height)
                )
            }
        }
    }
}

private data class Connection(
    val start: Int,
    val end: Int,
    val color: Color,
    val strokeWidth: Float
)

@Composable
fun ExerciseFeedbackAndStatsFooter(
    backendAnalysis: BackendPostureAnalysis?,
    repCount: Int,
    onStopSession: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Reps: $repCount",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                backendAnalysis?.let {
                    Text(
                        text = "Form Score: ${(it.postureScore * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyLarge,
                        color = when {
                            it.postureScore >= 0.8f -> Color(0xFF4CAF50) // Green
                            it.postureScore >= 0.6f -> Color(0xFFFFA000) // Orange
                            else -> Color(0xFFF44336) // Red
                        }
                    )
                }
            }
            Button(
                onClick = onStopSession,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Stop")
            }
        }
    }
}

@Composable
fun ExerciseSummaryContent(message: String, onBackToSelection: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.CheckCircle, contentDescription = "Session Ended", modifier = Modifier.size(64.dp), tint = Color(0xFF4CAF50))
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBackToSelection) {
            Text("Back to Exercises")
        }
    }
}

@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.ErrorOutline, contentDescription = "Error", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
fun LoadingIndicator(message: String = "Loading...") {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(message)
    }
}

fun formatDuration(milliseconds: Long): String {
    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}

