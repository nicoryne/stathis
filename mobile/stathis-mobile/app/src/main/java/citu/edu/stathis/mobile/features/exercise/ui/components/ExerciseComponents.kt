package citu.edu.stathis.mobile.features.exercise.ui.components

import androidx.camera.core.CameraSelector
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import citu.edu.stathis.mobile.features.exercise.data.Exercise
import citu.edu.stathis.mobile.features.exercise.data.PoseLandmarksData
import citu.edu.stathis.mobile.features.exercise.ui.ExerciseScreenUiState
import java.util.concurrent.TimeUnit

/**
 * Displays the exercise name, timer, and camera toggle button
 */
@Composable
fun ExerciseHeader(
    exerciseName: String,
    timerMs: Long,
    onToggleCamera: () -> Unit,
    currentCameraSelector: CameraSelector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = exerciseName,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Format timer display
            val minutes = TimeUnit.MILLISECONDS.toMinutes(timerMs)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(timerMs) % 60
            Text(
                text = String.format("%02d:%02d", minutes, seconds),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(onClick = onToggleCamera) {
                Icon(
                    imageVector = Icons.Filled.Cameraswitch,
                    contentDescription = "Toggle Camera",
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * Displays a visual representation of the detected pose
 */
@Composable
fun PoseSkeletonOverlay(
    landmarksData: PoseLandmarksData?,
    isFrontCamera: Boolean = true
) {
    if (landmarksData == null) return
    
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Pose detected with ${landmarksData.landmarkPoints.size} landmarks",
            color = Color.Green,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(8.dp)
        )
    }
}

/**
 * Displays the pose detection status
 */
@Composable
fun PoseDetectionStatusIndicator(
    detectionStatus: ExerciseScreenUiState.PoseDetectionStatus,
    exercise: Exercise
) {
    // Get color and icon based on status
    val (color, icon, tip) = when (detectionStatus) {
        ExerciseScreenUiState.PoseDetectionStatus.INITIALIZING -> Triple(
            Color(0xFFFFA000), // Amber
            Icons.Default.HourglassEmpty,
            "Initializing pose detection..."
        )
        ExerciseScreenUiState.PoseDetectionStatus.ACTIVE -> Triple(
            Color(0xFF4CAF50), // Green
            Icons.Default.CheckCircle,
            "Pose detected successfully"
        )
        ExerciseScreenUiState.PoseDetectionStatus.ERROR -> Triple(
            Color.Red,
            Icons.Default.Error,
            "Detection error. Try adjusting position."
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        Surface(
            modifier = Modifier
                .padding(8.dp),
            shape = RoundedCornerShape(50),
            color = color.copy(alpha = 0.2f),
            contentColor = color
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Pose detection status",
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodySmall,
                    color = color,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
