package citu.edu.stathis.mobile.features.posture.ui

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
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.navigation.NavHostController
import citu.edu.stathis.mobile.core.theme.BrandColors
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PostureScreen(
    navController: NavHostController,
    viewModel: PostureViewModel = hiltViewModel()
) {
    val TAG = "PostureScreen"
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val postureState by viewModel.postureState.collectAsState()
    val cameraState by viewModel.cameraState.collectAsState()
    val postureStats by viewModel.postureStats.collectAsState()

    // Camera permission state
    val cameraPermissionState = rememberPermissionState(
        Manifest.permission.CAMERA
    )

    // Check if camera permission is granted
    val hasCameraPermission = cameraPermissionState.status.isGranted

    // Clean up when leaving the screen
    DisposableEffect(key1 = true) {
        onDispose {
            viewModel.stopCamera()
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
                // Camera is permitted, show camera UI
                when (cameraState) {
                    CameraState.Inactive -> {
                        PostureIntroduction(
                            onStartCamera = {
                                viewModel.startCamera()
                            }
                        )
                    }
                    CameraState.Active -> {
                        PostureAnalysisScreen(
                            postureState = postureState,
                            postureStats = postureStats,
                            onStopCamera = {
                                viewModel.stopCamera()
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
            text = "To analyze your posture, we need access to your camera. Your privacy is important to us - camera data is only processed on your device and never stored or shared.",
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
fun PostureIntroduction(
    onStartCamera: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AccessibilityNew,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = BrandColors.Purple
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Posture Analysis",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Get real-time feedback on your posture using AI-powered analysis. Position yourself in front of the camera so your upper body is visible.",
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
                    text = "For best results:",
                    fontWeight = FontWeight.Bold,
                    color = BrandColors.Purple
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("• Position yourself 3-6 feet from the camera")
                Text("• Ensure your upper body is fully visible")
                Text("• Find a well-lit environment")
                Text("• Wear fitted clothing for better detection")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onStartCamera,
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
            Text("Start Posture Analysis")
        }
    }
}

@Composable
fun PostureAnalysisScreen(
    postureState: PostureState,
    postureStats: PostureStats,
    onStopCamera: () -> Unit,
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

                            // Bind use cases to camera - use front camera
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_FRONT_CAMERA,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            Log.e("PostureScreen", "Camera binding failed", e)
                        }
                    }, ContextCompat.getMainExecutor(context))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Overlay posture feedback
            PostureFeedbackOverlay(postureState)
        }

        // Posture stats and controls (takes 30% of the screen)
        PostureStatsPanel(
            postureState = postureState,
            postureStats = postureStats,
            onStopCamera = onStopCamera
        )
    }
}

@Composable
fun PostureFeedbackOverlay(postureState: PostureState) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        // Status indicator at the top
        AnimatedVisibility(
            visible = postureState !is PostureState.Initial,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth(0.9f),
                colors = CardDefaults.cardColors(
                    containerColor = when (postureState) {
                        is PostureState.Good -> Color(0xFFE8F5E9)
                        is PostureState.Bad -> Color(0xFFFFEBEE)
                        is PostureState.Detecting -> Color(0xFFF5F5F5)
                        is PostureState.Error -> Color(0xFFFFF3E0)
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
                        imageVector = when (postureState) {
                            is PostureState.Good -> Icons.Default.Check
                            is PostureState.Bad -> Icons.Default.Warning
                            is PostureState.Detecting -> Icons.Default.Info
                            is PostureState.Error -> Icons.Default.Error
                            else -> Icons.Default.Info
                        },
                        contentDescription = null,
                        tint = when (postureState) {
                            is PostureState.Good -> Color(0xFF4CAF50)
                            is PostureState.Bad -> Color(0xFFE53935)
                            is PostureState.Detecting -> Color(0xFF9E9E9E)
                            is PostureState.Error -> Color(0xFFFF9800)
                            else -> Color.Gray
                        }
                    )

                    Spacer(modifier = Modifier.size(16.dp))

                    Column {
                        Text(
                            text = when (postureState) {
                                is PostureState.Good -> "Good Posture"
                                is PostureState.Bad -> "Posture Needs Improvement"
                                is PostureState.Detecting -> "Analyzing Posture..."
                                is PostureState.Error -> "Detection Error"
                                else -> ""
                            },
                            fontWeight = FontWeight.Bold
                        )

                        if (postureState is PostureState.Bad) {
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(
                                text = postureState.result.issues.joinToString(", "),
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
fun PostureStatsPanel(
    postureState: PostureState,
    postureStats: PostureStats,
    onStopCamera: () -> Unit
) {
    val goodPosturePercentage = postureStats.goodPosturePercentage
    val animatedPercentage by animateFloatAsState(
        targetValue = goodPosturePercentage / 100f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "percentageAnimation"
    )

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
            // Session stats
            Text(
                text = "Posture Session",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Circular progress indicator for good posture percentage
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    drawArc(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 12.dp.toPx())
                    )

                    drawArc(
                        color = if (goodPosturePercentage > 70) Color(0xFF4CAF50) else Color(0xFFFF9800),
                        startAngle = -90f,
                        sweepAngle = 360f * animatedPercentage,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 12.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$goodPosturePercentage%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (goodPosturePercentage > 70) Color(0xFF4CAF50) else Color(0xFFFF9800)
                    )
                    Text(
                        text = "Good Posture",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Session time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SessionStat(
                    label = "Session Time",
                    value = formatTime(postureStats.sessionDurationMs)
                )

                SessionStat(
                    label = "Good Posture",
                    value = formatTime(postureStats.goodPostureTimeMs)
                )

                SessionStat(
                    label = "Bad Posture",
                    value = formatTime(postureStats.badPostureTimeMs)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Stop button
            OutlinedButton(
                onClick = onStopCamera,
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
                Text("End Session")
            }
        }
    }
}

@Composable
fun SessionStat(
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