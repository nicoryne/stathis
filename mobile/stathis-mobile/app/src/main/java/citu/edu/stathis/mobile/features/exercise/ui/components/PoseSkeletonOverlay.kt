package citu.edu.stathis.mobile.features.exercise.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.max
import kotlin.math.min

/**
 * A custom view that renders a skeleton overlay based on pose landmarks detected by ML Kit.
 * This overlay visualizes the human body skeleton by drawing lines between detected landmarks.
 */
class PoseSkeletonOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var pose: Pose? = null
    private var imageWidth = 0
    private var imageHeight = 0
    private var scaleFactor = 1.0f
    private var isImageFlipped = false

    // Paint objects for drawing
    private val landmarkPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        strokeWidth = 8f
    }

    private val connectionPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val inFramePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    // Define connections between landmarks to form the skeleton
    private val connections = listOf(
        // Face
        Pair(PoseLandmark.NOSE, PoseLandmark.LEFT_EYE_INNER),
        Pair(PoseLandmark.LEFT_EYE_INNER, PoseLandmark.LEFT_EYE),
        Pair(PoseLandmark.LEFT_EYE, PoseLandmark.LEFT_EYE_OUTER),
        Pair(PoseLandmark.LEFT_EYE_OUTER, PoseLandmark.LEFT_EAR),
        Pair(PoseLandmark.NOSE, PoseLandmark.RIGHT_EYE_INNER),
        Pair(PoseLandmark.RIGHT_EYE_INNER, PoseLandmark.RIGHT_EYE),
        Pair(PoseLandmark.RIGHT_EYE, PoseLandmark.RIGHT_EYE_OUTER),
        Pair(PoseLandmark.RIGHT_EYE_OUTER, PoseLandmark.RIGHT_EAR),
        Pair(PoseLandmark.MOUTH_LEFT, PoseLandmark.MOUTH_RIGHT),

        // Torso
        Pair(PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER),
        Pair(PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP),
        Pair(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP),
        Pair(PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP),

        // Left arm
        Pair(PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW),
        Pair(PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST),
        Pair(PoseLandmark.LEFT_WRIST, PoseLandmark.LEFT_PINKY),
        Pair(PoseLandmark.LEFT_WRIST, PoseLandmark.LEFT_INDEX),
        Pair(PoseLandmark.LEFT_WRIST, PoseLandmark.LEFT_THUMB),
        Pair(PoseLandmark.LEFT_PINKY, PoseLandmark.LEFT_INDEX),

        // Right arm
        Pair(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW),
        Pair(PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST),
        Pair(PoseLandmark.RIGHT_WRIST, PoseLandmark.RIGHT_PINKY),
        Pair(PoseLandmark.RIGHT_WRIST, PoseLandmark.RIGHT_INDEX),
        Pair(PoseLandmark.RIGHT_WRIST, PoseLandmark.RIGHT_THUMB),
        Pair(PoseLandmark.RIGHT_PINKY, PoseLandmark.RIGHT_INDEX),

        // Left leg
        Pair(PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE),
        Pair(PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE),
        Pair(PoseLandmark.LEFT_ANKLE, PoseLandmark.LEFT_HEEL),
        Pair(PoseLandmark.LEFT_ANKLE, PoseLandmark.LEFT_FOOT_INDEX),
        Pair(PoseLandmark.LEFT_HEEL, PoseLandmark.LEFT_FOOT_INDEX),

        // Right leg
        Pair(PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE),
        Pair(PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE),
        Pair(PoseLandmark.RIGHT_ANKLE, PoseLandmark.RIGHT_HEEL),
        Pair(PoseLandmark.RIGHT_ANKLE, PoseLandmark.RIGHT_FOOT_INDEX),
        Pair(PoseLandmark.RIGHT_HEEL, PoseLandmark.RIGHT_FOOT_INDEX)
    )

    /**
     * Updates the pose data to be rendered.
     *
     * @param pose The pose detected by ML Kit
     * @param imageWidth Width of the source image
     * @param imageHeight Height of the source image
     * @param isImageFlipped Whether the image is flipped horizontally (e.g., front camera)
     */
    fun updatePose(pose: Pose?, imageWidth: Int, imageHeight: Int, isImageFlipped: Boolean = false) {
        this.pose = pose
        this.imageWidth = imageWidth
        this.imageHeight = imageHeight
        this.isImageFlipped = isImageFlipped
        invalidate() // Request redraw
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        pose?.let { pose ->
            // Calculate scale factor to fit the image dimensions to the view dimensions
            val viewAspectRatio = width.toFloat() / height.toFloat()
            val imageAspectRatio = imageWidth.toFloat() / imageHeight.toFloat()
            
            val scaleX: Float
            val scaleY: Float
            
            if (viewAspectRatio > imageAspectRatio) {
                // View is wider than the image
                scaleY = height.toFloat() / imageHeight.toFloat()
                scaleX = scaleY
            } else {
                // View is taller than the image
                scaleX = width.toFloat() / imageWidth.toFloat()
                scaleY = scaleX
            }
            
            scaleFactor = min(scaleX, scaleY)
            
            // Draw connections first (lines between landmarks)
            for (connection in connections) {
                val startLandmark = pose.getPoseLandmark(connection.first)
                val endLandmark = pose.getPoseLandmark(connection.second)
                
                if (startLandmark != null && endLandmark != null) {
                    val startPoint = transformLandmarkPosition(startLandmark.position)
                    val endPoint = transformLandmarkPosition(endLandmark.position)
                    
                    canvas.drawLine(
                        startPoint.x, 
                        startPoint.y, 
                        endPoint.x, 
                        endPoint.y, 
                        connectionPaint
                    )
                }
            }
            
            // Draw landmarks (points)
            for (landmark in pose.allPoseLandmarks) {
                val point = transformLandmarkPosition(landmark.position)
                
                // Adjust point size based on landmark confidence
                val radius = 8f * landmark.inFrameLikelihood
                
                // Color based on likelihood of being in frame
                landmarkPaint.color = if (landmark.inFrameLikelihood > 0.5f) {
                    Color.RED
                } else {
                    Color.YELLOW
                }
                
                canvas.drawCircle(point.x, point.y, radius, landmarkPaint)
            }
        }
    }
    
    /**
     * Transforms landmark position from the input image coordinates to the view coordinates.
     */
    private fun transformLandmarkPosition(position: PointF): PointF {
        val scaledX = position.x * scaleFactor
        val scaledY = position.y * scaleFactor
        
        // Handle horizontal flipping if needed (e.g., for front camera)
        val x = if (isImageFlipped) width - scaledX else scaledX
        
        return PointF(x, scaledY)
    }
    
    /**
     * Set custom colors for the skeleton visualization
     */
    fun setSkeletonColors(landmarkColor: Int, connectionColor: Int) {
        landmarkPaint.color = landmarkColor
        connectionPaint.color = connectionColor
        invalidate()
    }
}
