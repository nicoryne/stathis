package citu.edu.stathis.mobile.features.exercise.ui

import android.Manifest
import android.util.Log
import android.view.ViewGroup
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
import com.google.mlkit.vision.pose.PoseLandmark
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
                        onImageProxy = { imageProxy -> viewModel.processImageProxy(imageProxy, currentUiState.selectedExercise) },
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
fun ExerciseSelectionContent(exercises: List<Exercise>, onExerciseSelected: (Exercise) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Select an Exercise", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 16.dp))
        if (exercises.isEmpty()) {
            Text("No exercises available at the moment.")
        } else {
            LazyColumn {
                items(exercises) { exercise ->
                    ExerciseListItem(exercise = exercise, onClick = { onExerciseSelected(exercise) })
                    Divider()
                }
            }
        }
    }
}

@Composable
fun ExerciseListItem(exercise: Exercise, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.FitnessCenter, contentDescription = exercise.name, modifier = Modifier.size(40.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(exercise.name, style = MaterialTheme.typography.titleMedium)
            Text(exercise.description, style = MaterialTheme.typography.bodySmall, maxLines = 2)
        }
    }
}

@Composable
fun ExerciseIntroductionContent(exercise: Exercise, onStartSession: () -> Unit, onBackToSelection: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(exercise.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Instructions:", style = MaterialTheme.typography.titleMedium)
        exercise.instructions.forEach { instruction ->
            Text("• $instruction", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 4.dp))
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onStartSession, modifier = Modifier.fillMaxWidth(0.8f)) {
            Text("Start Session")
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onBackToSelection, modifier = Modifier.fillMaxWidth(0.8f)) {
            Text("Choose Different Exercise")
        }
    }
}

@Composable
fun ExerciseActiveContent(
    activeState: ExerciseScreenUiState.ExerciseActive,
    onImageProxy: (ImageProxy) -> Unit,
    onStopSession: () -> Unit,
    onToggleCamera: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { previewView ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = CameraXPreview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setTargetRotation(previewView.display.rotation)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                                onImageProxy(imageProxy)
                            }
                        }
                    try {
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
        )

        PoseSkeletonOverlay(landmarksData = activeState.currentPoseLandmarks)

        Column(modifier = Modifier.fillMaxSize()) {
            ExerciseHeader(
                exerciseName = activeState.selectedExercise.name,
                timerMs = activeState.sessionTimerMs,
                onToggleCamera = onToggleCamera,
                currentCameraSelector = activeState.currentCameraSelector
            )
            Spacer(modifier = Modifier.weight(1f))
            ExerciseFeedbackAndStatsFooter(
                onDeviceFeedback = activeState.onDeviceFeedback,
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
fun PoseSkeletonOverlay(landmarksData: PoseLandmarksData?) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        landmarksData?.landmarkPoints?.let { points ->
            val validPoints = points.filter { (it.inFrameLikelihood ?: 0f) > 0.5f }

            validPoints.forEach { point ->
                drawCircle(
                    color = Color.Yellow,
                    radius = 8f,
                    center = Offset(point.x * size.width, point.y * size.height)
                )
            }
            getPoseConnections().forEach { connection ->
                val start = validPoints.find { it.type == connection.first }
                val end = validPoints.find { it.type == connection.second }
                if (start != null && end != null) {
                    drawLine(
                        color = Color.Cyan,
                        start = Offset(start.x * size.width, start.y * size.height),
                        end = Offset(end.x * size.width, end.y * size.height),
                        strokeWidth = 5f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

fun getPoseConnections(): List<Pair<Int, Int>> {
    return buildList<Pair<Int, Int>> {
        add(PoseLandmark.LEFT_SHOULDER to PoseLandmark.RIGHT_SHOULDER)
        add(PoseLandmark.LEFT_HIP to PoseLandmark.RIGHT_HIP)
        add(PoseLandmark.LEFT_SHOULDER to PoseLandmark.LEFT_ELBOW)
        add(PoseLandmark.LEFT_ELBOW to PoseLandmark.LEFT_WRIST)
        add(PoseLandmark.RIGHT_SHOULDER to PoseLandmark.RIGHT_ELBOW)
        add(PoseLandmark.RIGHT_ELBOW to PoseLandmark.RIGHT_WRIST)
        add(PoseLandmark.LEFT_SHOULDER to PoseLandmark.LEFT_HIP)
        add(PoseLandmark.RIGHT_SHOULDER to PoseLandmark.RIGHT_HIP)
        add(PoseLandmark.LEFT_HIP to PoseLandmark.LEFT_KNEE)
        add(PoseLandmark.LEFT_KNEE to PoseLandmark.LEFT_ANKLE)
        add(PoseLandmark.RIGHT_HIP to PoseLandmark.RIGHT_KNEE)
        add(PoseLandmark.RIGHT_KNEE to PoseLandmark.RIGHT_ANKLE)

        add(PoseLandmark.LEFT_WRIST to PoseLandmark.LEFT_PINKY)
        add(PoseLandmark.LEFT_WRIST to PoseLandmark.LEFT_INDEX)
        add(PoseLandmark.LEFT_INDEX to PoseLandmark.LEFT_THUMB)
        add(PoseLandmark.RIGHT_WRIST to PoseLandmark.RIGHT_PINKY)
        add(PoseLandmark.RIGHT_WRIST to PoseLandmark.RIGHT_INDEX)
        add(PoseLandmark.RIGHT_INDEX to PoseLandmark.RIGHT_THUMB)

        add(PoseLandmark.LEFT_ANKLE to PoseLandmark.LEFT_HEEL)
        add(PoseLandmark.LEFT_HEEL to PoseLandmark.LEFT_FOOT_INDEX)
        add(PoseLandmark.LEFT_ANKLE to PoseLandmark.LEFT_FOOT_INDEX)
        add(PoseLandmark.RIGHT_ANKLE to PoseLandmark.RIGHT_HEEL)
        add(PoseLandmark.RIGHT_HEEL to PoseLandmark.RIGHT_FOOT_INDEX)
        add(PoseLandmark.RIGHT_ANKLE to PoseLandmark.RIGHT_FOOT_INDEX)

        add(PoseLandmark.NOSE to PoseLandmark.LEFT_EYE_INNER)
        add(PoseLandmark.LEFT_EYE_INNER to PoseLandmark.LEFT_EYE)
        add(PoseLandmark.LEFT_EYE to PoseLandmark.LEFT_EYE_OUTER)
        add(PoseLandmark.LEFT_EYE_OUTER to PoseLandmark.LEFT_EAR)
        add(PoseLandmark.NOSE to PoseLandmark.RIGHT_EYE_INNER)
        add(PoseLandmark.RIGHT_EYE_INNER to PoseLandmark.RIGHT_EYE)
        add(PoseLandmark.RIGHT_EYE to PoseLandmark.RIGHT_EYE_OUTER)
        add(PoseLandmark.RIGHT_EYE_OUTER to PoseLandmark.RIGHT_EAR)
        add(PoseLandmark.LEFT_MOUTH to PoseLandmark.RIGHT_MOUTH)
    }
}


@Composable
fun ExerciseFeedbackAndStatsFooter(
    onDeviceFeedback: OnDeviceFeedback?,
    backendAnalysis: BackendPostureAnalysis?,
    repCount: Int,
    onStopSession: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Session Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Reps", style = MaterialTheme.typography.labelMedium)
                    Text("$repCount", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = BrandColors.Purple)
                }
                backendAnalysis?.postureScore?.let {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Posture Score", style = MaterialTheme.typography.labelMedium)
                        Text(String.format(Locale.US, "%.0f%%", it * 100), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = BrandColors.Teal)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            onDeviceFeedback?.let {
                Text("Feedback:", style = MaterialTheme.typography.titleSmall)
                if (it.formIssues.isEmpty() && it.confidence > 0.7) {
                    Text("Looking Good!", color = Color(0xFF4CAF50))
                } else if (it.formIssues.isNotEmpty()) {
                    it.formIssues.forEach { issue -> Text("• $issue", color = MaterialTheme.colorScheme.error) }
                } else if (it.confidence < 0.7) {
                    Text("Move into position or ensure good lighting.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("Exercise State: ${it.exerciseState}", style = MaterialTheme.typography.bodySmall)

            }
            backendAnalysis?.identifiedExercise?.let {
                Text("Identified: $it", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onStopSession, modifier = Modifier.fillMaxWidth()) {
                Text("End Session")
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

