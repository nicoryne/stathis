package citu.edu.stathis.mobile.features.exercise.ui.components

import android.content.Context
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import com.google.mlkit.vision.pose.Pose

/**
 * A Composable that wraps the PoseSkeletonOverlay custom view for use in Jetpack Compose UI.
 * This component visualizes the human body skeleton based on pose landmarks detected by ML Kit.
 *
 * @param pose The pose detected by ML Kit
 * @param imageWidth Width of the source image
 * @param imageHeight Height of the source image
 * @param isImageFlipped Whether the image is flipped horizontally (e.g., front camera)
 * @param landmarkColor Optional color for the landmark points
 * @param connectionColor Optional color for the connections between landmarks
 */
@Composable
fun EnhancedSkeletonOverlay(
    pose: Pose?,
    imageWidth: Int,
    imageHeight: Int,
    isImageFlipped: Boolean = false,
    landmarkColor: Int? = null,
    connectionColor: Int? = null
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    
    // Create and remember the overlay view
    val overlayView = remember {
        PoseSkeletonOverlay(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }
    
    // Apply custom colors if provided
    DisposableEffect(landmarkColor, connectionColor) {
        if (landmarkColor != null && connectionColor != null) {
            overlayView.setSkeletonColors(landmarkColor, connectionColor)
        }
        onDispose { }
    }
    
    // Update the pose data whenever it changes
    DisposableEffect(pose, imageWidth, imageHeight, isImageFlipped) {
        overlayView.updatePose(pose, imageWidth, imageHeight, isImageFlipped)
        onDispose { }
    }
    
    // Wrap the Android View in Compose
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { overlayView }
    )
}
