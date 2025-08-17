package citu.edu.stathis.mobile.features.exercise.ui

import android.Manifest
import android.graphics.Color as AndroidColor
import android.util.Log
import android.view.Surface
import android.view.ViewGroup
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview as CameraXPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import citu.edu.stathis.mobile.features.exercise.data.posedetection.PoseAnalyzer
import citu.edu.stathis.mobile.features.exercise.ui.components.EnhancedSkeletonOverlay
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.pose.Pose
import java.util.Locale
import java.util.concurrent.TimeUnit

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
                        onPoseDetected = { pose, imageWidth, imageHeight, isFlipped, exercise -> 
                            viewModel.onPoseDetected(pose, exercise)
                            // Store the pose and image dimensions for rendering
                            viewModel.updatePoseForRendering(pose, imageWidth, imageHeight, isFlipped)
                        },
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
    onPoseDetected: (Pose, Int, Int, Boolean, Exercise) -> Unit,
    onStopSession: () -> Unit,
    onToggleCamera: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Track current pose for rendering
    var currentPose by remember { mutableStateOf<Pose?>(null) }
    var imageWidth by remember { mutableStateOf(0) }
    var imageHeight by remember { mutableStateOf(0) }
    val isImageFlipped = activeState.currentCameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA

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
                        .build()

                    // Create pose analyzer
                    val poseAnalyzer = PoseAnalyzer(
                        executor = ContextCompat.getMainExecutor(context),
                        onPoseDetected = { pose, width, height, flipped ->
                            // Update local state for rendering
                            currentPose = pose
                            imageWidth = width
                            imageHeight = height
                            
                            // Pass to view model for processing
                            onPoseDetected(pose, width, height, flipped, activeState.selectedExercise)
                        },
                        isImageFlipped = isImageFlipped
                    )
                    
                    imageAnalysis.setAnalyzer(
                        ContextCompat.getMainExecutor(context),
                        poseAnalyzer
                    )

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

        // Enhanced Skeleton Overlay with our custom view
        EnhancedSkeletonOverlay(
            pose = currentPose,
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            isImageFlipped = isImageFlipped,
            landmarkColor = AndroidColor.RED,
            connectionColor = AndroidColor.GREEN
        )

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