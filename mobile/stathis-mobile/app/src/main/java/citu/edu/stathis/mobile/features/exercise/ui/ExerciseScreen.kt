package citu.edu.stathis.mobile.features.exercise.ui

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import citu.edu.stathis.mobile.core.theme.BrandColors
import citu.edu.stathis.mobile.features.exercise.data.ExerciseState
import citu.edu.stathis.mobile.features.exercise.data.ExerciseType
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ExerciseScreen(
    navController: NavHostController,
    viewModel: ExerciseViewModel = hiltViewModel()
) {
    val TAG = "ExerciseScreen"
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val exerciseState by viewModel.exerciseState.collectAsState()
    val cameraState by viewModel.cameraState.collectAsState()
    val exerciseStats by viewModel.exerciseStats.collectAsState()
    val selectedExerciseType by viewModel.selectedExerciseType.collectAsState()

    // Camera permission state
    val cameraPermissionState = rememberPermissionState(
        Manifest.permission.CAMERA
    )

    // Check if camera permission is granted
    val hasCameraPermission = cameraPermissionState.status.isGranted

    // Clean up when leaving the screen
    DisposableEffect(key1 = true) {
        onDispose {
            viewModel.stopExercise()
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Camera permission request
            if (!hasCameraPermission) {
                CameraPermissionRequest(
                    onRequestPermission = {
                        cameraPermissionState.launchPermissionRequest()
                    }
                )
            } else {
                // Camera is permitted, show exercise UI
                when {
                    selectedExerciseType == null -> {
                        ExerciseTypeSelection(
                            onSelectExerciseType = { type ->
                                viewModel.selectExerciseType(type)
                            }
                        )
                    }
                    cameraState == CameraState.Inactive -> {
                        ExerciseIntroduction(
                            exerciseType = selectedExerciseType,
                            onStartExercise = {
                                viewModel.startExercise()
                            },
                            onChangeExercise = {
                                viewModel.selectExerciseType(selectedExerciseType!!)
                            }
                        )
                    }
                    cameraState == CameraState.Active -> {
                        ExerciseTrackingScreen(
                            exerciseType = selectedExerciseType!!,
                            exerciseState = exerciseState,
                            exerciseStats = exerciseStats,
                            onStopExercise = {
                                viewModel.stopExercise()
                            },
                            onImageAnalyzed = { imageProxy ->
                                viewModel.processImage(imageProxy)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPermissionRequest(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = BrandColors.Purple
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Camera Permission Required",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "To analyze your exercise form, we need access to your camera. Your privacy is important to us - camera data is only processed on your device and never stored or shared.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandColors.Purple
            ),
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Grant Camera Permission")
        }
    }
}

@Composable
fun ExerciseTypeSelection(
    onSelectExerciseType: (ExerciseType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FitnessCenter,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = BrandColors.Purple
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Choose Exercise",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Select the exercise you want to track with AI-powered form analysis",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Exercise type cards
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable { onSelectExerciseType(ExerciseType.SQUAT) },
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF0F4FF)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccessibilityNew,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = BrandColors.Purple
                )

                Spacer(modifier = Modifier.size(16.dp))

                Column {
                    Text(
                        text = "Squats",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Track proper form and count reps",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable { onSelectExerciseType(ExerciseType.PUSHUP) },
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF0F4FF)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = BrandColors.Purple
                )

                Spacer(modifier = Modifier.size(16.dp))

                Column {
                    Text(
                        text = "Push-ups",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Track proper form and count reps",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun ExerciseIntroduction(
    exerciseType: ExerciseType?,
    onStartExercise: () -> Unit,
    onChangeExercise: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = when (exerciseType) {
                ExerciseType.SQUAT -> Icons.Default.AccessibilityNew
                ExerciseType.PUSHUP -> Icons.Default.FitnessCenter
                else -> Icons.Default.FitnessCenter
            },
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = BrandColors.Purple
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = when (exerciseType) {
                ExerciseType.SQUAT -> "Squat Form Analysis"
                ExerciseType.PUSHUP -> "Push-up Form Analysis"
                else -> "Exercise Form Analysis"
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Get real-time feedback on your form using AI-powered analysis. Position yourself so your full body is visible.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF0F4FF)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = when (exerciseType) {
                        ExerciseType.SQUAT -> "For proper squat form:"
                        ExerciseType.PUSHUP -> "For proper push-up form:"
                        else -> "For best results:"
                    },
                    fontWeight = FontWeight.Bold,
                    color = BrandColors.Purple
                )

                Spacer(modifier = Modifier.height(8.dp))

                when (exerciseType) {
                    ExerciseType.SQUAT -> {
                        Text("• Keep your back straight")
                        Text("• Knees should track over toes")
                        Text("• Lower until thighs are parallel to ground")
                        Text("• Keep weight in your heels")
                    }
                    ExerciseType.PUSHUP -> {
                        Text("• Keep your body in a straight line")
                        Text("• Hands should be shoulder-width apart")
                        Text("• Lower until elbows are at 90 degrees")
                        Text("• Keep core engaged throughout")
                    }
                    else -> {
                        Text("• Position yourself 3-6 feet from the camera")
                        Text("• Ensure your full body is visible")
                        Text("• Find a well-lit environment")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onStartExercise,
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandColors.Purple
            ),
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text("Start Exercise Tracking")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onChangeExercise,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Change Exercise")
        }
    }
}

@Composable
fun ExerciseTrackingScreen(
    exerciseType: ExerciseType,
    exerciseState: ExerciseUiState,
    exerciseStats: ExerciseStats,
    onStopExercise: () -> Unit,
    onImageAnalyzed: (ImageProxy) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Camera preview (takes 70% of the screen)
        Box(
            modifier = Modifier
                .weight(0.7f)
                .fillMaxWidth()
        ) {
            // Camera preview
            AndroidView(
                factory = { context ->
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
                        val cameraProvider = cameraProviderFuture.get()

                        // Preview use case
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        // Image analysis use case
                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(
                                    Executors.newSingleThreadExecutor(),
                                    { imageProxy ->
                                        onImageAnalyzed(imageProxy)
                                    }
                                )
                            }

                        try {
                            // Unbind all use cases before rebinding
                            cameraProvider.unbindAll()

                            // For push-ups, use back camera (user will be facing down)
                            // For squats, use front camera (user will be facing the device)
                            val cameraSelector = when (exerciseType) {
                                ExerciseType.PUSHUP -> CameraSelector.DEFAULT_BACK_CAMERA
                                ExerciseType.SQUAT -> CameraSelector.DEFAULT_FRONT_CAMERA
                            }

                            // Bind use cases to camera
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            Log.e("ExerciseScreen", "Camera binding failed", e)
                        }
                    }, ContextCompat.getMainExecutor(context))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Overlay exercise feedback
            ExerciseFeedbackOverlay(exerciseState, exerciseType)
        }

        // Exercise stats and controls (takes 30% of the screen)
        ExerciseStatsPanel(
            exerciseType = exerciseType,
            exerciseState = exerciseState,
            exerciseStats = exerciseStats,
            onStopExercise = onStopExercise
        )
    }
}

@Composable
fun ExerciseFeedbackOverlay(
    exerciseState: ExerciseUiState,
    exerciseType: ExerciseType
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        // Status indicator at the top
        AnimatedVisibility(
            visible = exerciseState !is ExerciseUiState.Initial,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(0.9f),
                colors = CardDefaults.cardColors(
                    containerColor = when (exerciseState) {
                        is ExerciseUiState.Tracking -> {
                            when (exerciseState.result.currentState) {
                                ExerciseState.UP -> Color(0xFFE8F5E9) // Green for up position
                                ExerciseState.DOWN -> Color(0xFFE3F2FD) // Blue for down position
                                else -> Color(0xFFF5F5F5) // Gray for other states
                            }
                        }
                        is ExerciseUiState.Invalid -> Color(0xFFFFEBEE) // Red for invalid
                        is ExerciseUiState.Detecting -> Color(0xFFF5F5F5) // Gray for detecting
                        is ExerciseUiState.Error -> Color(0xFFFFF3E0) // Orange for error
                        else -> Color.Transparent
                    }
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (exerciseState) {
                            is ExerciseUiState.Tracking -> {
                                when (exerciseState.result.currentState) {
                                    ExerciseState.UP -> Icons.Default.Check
                                    ExerciseState.DOWN -> Icons.Default.Info
                                    else -> Icons.Default.Info
                                }
                            }
                            is ExerciseUiState.Invalid -> Icons.Default.Warning
                            is ExerciseUiState.Detecting -> Icons.Default.Info
                            is ExerciseUiState.Error -> Icons.Default.Error
                            else -> Icons.Default.Info
                        },
                        contentDescription = null,
                        tint = when (exerciseState) {
                            is ExerciseUiState.Tracking -> {
                                when (exerciseState.result.currentState) {
                                    ExerciseState.UP -> Color(0xFF4CAF50) // Green
                                    ExerciseState.DOWN -> Color(0xFF2196F3) // Blue
                                    else -> Color(0xFF9E9E9E) // Gray
                                }
                            }
                            is ExerciseUiState.Invalid -> Color(0xFFE53935) // Red
                            is ExerciseUiState.Detecting -> Color(0xFF9E9E9E) // Gray
                            is ExerciseUiState.Error -> Color(0xFFFF9800) // Orange
                            else -> Color.Gray
                        }
                    )

                    Spacer(modifier = Modifier.size(16.dp))

                    Column {
                        Text(
                            text = when (exerciseState) {
                                is ExerciseUiState.Tracking -> {
                                    when (exerciseState.result.currentState) {
                                        ExerciseState.UP -> "${exerciseType.name.lowercase().capitalize()} Position: Up"
                                        ExerciseState.DOWN -> "${exerciseType.name.lowercase().capitalize()} Position: Down"
                                        else -> "Get Ready"
                                    }
                                }
                                is ExerciseUiState.Invalid -> "Form Needs Improvement"
                                is ExerciseUiState.Detecting -> "Analyzing Form..."
                                is ExerciseUiState.Error -> "Detection Error"
                                else -> ""
                            },
                            fontWeight = FontWeight.Bold
                        )

                        if (exerciseState is ExerciseUiState.Invalid && exerciseState.result.formIssues.isNotEmpty()) {
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(
                                text = exerciseState.result.formIssues.joinToString(", "),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseStatsPanel(
    exerciseType: ExerciseType,
    exerciseState: ExerciseUiState,
    exerciseStats: ExerciseStats,
    onStopExercise: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Exercise name and rep counter
            Text(
                text = when (exerciseType) {
                    ExerciseType.SQUAT -> "Squat Counter"
                    ExerciseType.PUSHUP -> "Push-up Counter"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Rep counter
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .border(
                        width = 4.dp,
                        color = BrandColors.Purple,
                        shape = CircleShape
                    )
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${exerciseStats.repCount}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = BrandColors.Purple
                    )
                    Text(
                        text = "Reps",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current state and session time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ExerciseStat(
                    label = "Current State",
                    value = when (exerciseStats.currentState) {
                        ExerciseState.UP -> "Up"
                        ExerciseState.DOWN -> "Down"
                        ExerciseState.WAITING -> "Ready"
                        ExerciseState.INVALID -> "Check Form"
                    }
                )

                ExerciseStat(
                    label = "Session Time",
                    value = formatTime(exerciseStats.sessionDurationMs)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Stop button
            OutlinedButton(
                onClick = onStopExercise,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("End Exercise")
            }
        }
    }
}

@Composable
fun ExerciseStat(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

// Helper function to format time in mm:ss
fun formatTime(timeMs: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeMs)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeMs) - TimeUnit.MINUTES.toSeconds(minutes)
    return String.format("%02d:%02d", minutes, seconds)
}