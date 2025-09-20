package citu.edu.stathis.mobile.features.exercise.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import citu.edu.stathis.mobile.features.exercise.data.LandmarkPoint
import citu.edu.stathis.mobile.features.exercise.data.PoseLandmarksData
import com.google.mlkit.vision.pose.PoseLandmark

/**
 * Custom view that draws the skeleton overlay based on pose landmarks detected by ML Kit.
 * This view should be positioned on top of the camera preview with the same dimensions.
 * Optimized to prevent black flickering when rendering skeletons.
 */
class PoseSkeletonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    // Track whether we're using front camera to handle mirroring correctly
    var isFrontCamera: Boolean = true
    
    // Define more visible colors for the skeleton - EXTREMELY VISIBLE
    private val landmarkPaint = Paint().apply {
        color = Color.RED
        strokeWidth = 25f // Extra thick points for extreme visibility
        style = Paint.Style.FILL
        setShadowLayer(8f, 0f, 0f, Color.BLACK) // Strong shadow for contrast
        isAntiAlias = true // Smoother rendering
    }
    
    // Updated line paint for MAXIMUM visibility
    private val linePaint = Paint().apply {
        color = Color.YELLOW // Yellow is very visible on most backgrounds
        strokeWidth = 18f // Much thicker lines for extreme visibility
        style = Paint.Style.STROKE
        setShadowLayer(8f, 0f, 0f, Color.BLACK) // Strong shadow for contrast
        isAntiAlias = true // Smoother rendering
    }
    
    // Special paint for torso connections (central body part)
    private val torsoPaint = Paint().apply {
        color = Color.WHITE // White for torso to distinguish it
        strokeWidth = 22f // Extra thick for the central body part
        style = Paint.Style.STROKE
        setShadowLayer(8f, 0f, 0f, Color.BLACK) // Strong shadow for contrast
        isAntiAlias = true
    }
    
    // Make our view transparent by default
    init {
        // Make the view background transparent - critical for preventing black flashing
        setBackgroundColor(Color.TRANSPARENT)
        // Make the view layer type hardware for better performance with transparency
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }
    
    private val confidenceThreshold = 0.01f // Very low threshold to ensure points are displayed
    
    private var landmarks: List<LandmarkPoint>? = null
    private var viewWidth = 0
    private var viewHeight = 0
    
    // No debouncing - we'll use double-buffering instead for smoother updates
    private var pendingLandmarks: List<LandmarkPoint>? = null
    private var lastDrawnLandmarks: List<LandmarkPoint>? = null
    
    // Use a background thread to process pose updates
    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private val backgroundHandler = android.os.Handler(android.os.HandlerThread("PoseProcessingThread").apply { start() }.looper)
    private var pendingUpdate = false
    
    // Track whether we have valid landmarks to display
    private var hasValidLandmarks = false
    
    fun updatePose(poseLandmarksData: PoseLandmarksData?, isFrontCam: Boolean = true) {
        // Process landmarks in background thread only
        backgroundHandler.post {
            // Store landmarks for next render cycle (double-buffering)
            pendingLandmarks = poseLandmarksData?.landmarkPoints
            isFrontCamera = isFrontCam
            
            // Just trigger a redraw - no other processing on UI thread
            postInvalidateOnAnimation()
        }
    }
    
    fun updateColors(landmarkColor: Int, lineColor: Int) {
        landmarkPaint.color = landmarkColor
        linePaint.color = lineColor
        invalidate()
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w
        viewHeight = h
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Don't clear the canvas - this is critical to prevent black flashing
        // The view is already set to transparent background in init
        
        // Check if we have new landmarks to draw
        if (pendingLandmarks != null) {
            // Update our landmarks with the pending ones
            landmarks = pendingLandmarks
            pendingLandmarks = null  // Clear pending landmarks
        }
        
        // Only log when landmarks change to reduce spam
        if (landmarks != lastDrawnLandmarks) {
            Log.d("PoseSkeletonView", "🎨 Drawing ${landmarks?.size ?: 0} landmarks")
            lastDrawnLandmarks = landmarks
        }
        
        // Process landmarks if they exist
        landmarks?.let { points ->
            if (points.isNotEmpty()) {
                // Get only landmarks with acceptable confidence
                val visibleLandmarks = points.filter { it.inFrameLikelihood >= confidenceThreshold }
                
                if (visibleLandmarks.isNotEmpty()) {
                    // Draw skeleton connections first (lines between landmarks)
                    drawSkeletonConnections(canvas, visibleLandmarks)
                    
                    // Then draw the landmark points on top - using a more efficient method
                    val tempPoint = PointF(0f, 0f) // Reuse point object to reduce GC pressure
                    
                    for (landmark in visibleLandmarks) {
                        convertCoordinatesToScreen(landmark.x, landmark.y, tempPoint)
                        
                        // Determine point size based on landmark type
                        val radius = when (landmark.type) {
                            // Major joints get larger circles
                            PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER,
                            PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP,
                            PoseLandmark.LEFT_KNEE, PoseLandmark.RIGHT_KNEE,
                            PoseLandmark.LEFT_ANKLE, PoseLandmark.RIGHT_ANKLE,
                            PoseLandmark.LEFT_ELBOW, PoseLandmark.RIGHT_ELBOW,
                            PoseLandmark.LEFT_WRIST, PoseLandmark.RIGHT_WRIST -> 14f
                            
                            // Face landmarks get smaller circles
                            PoseLandmark.NOSE, PoseLandmark.LEFT_EYE, PoseLandmark.RIGHT_EYE,
                            PoseLandmark.LEFT_EAR, PoseLandmark.RIGHT_EAR,
                            PoseLandmark.LEFT_MOUTH, PoseLandmark.RIGHT_MOUTH -> 8f
                            
                            // Default size for all other points
                            else -> 10f
                        }
                        
                        // Adjust alpha based on confidence
                        landmarkPaint.alpha = (255 * (0.5f + landmark.inFrameLikelihood * 0.5f)).toInt()
                        
                        // Draw the landmark point
                        canvas.drawCircle(tempPoint.x, tempPoint.y, radius, landmarkPaint)
                    }
                    
                    // Reset alpha for next draw
                    landmarkPaint.alpha = 255
                }
            }
        }
    }
    
    private fun drawSkeletonConnections(canvas: Canvas, landmarks: List<LandmarkPoint>) {
        // Create a map for easier lookup
        val landmarkMap = landmarks.associateBy { it.type }
        
        // Draw torso connections with torso paint (different color/thickness)
        drawConnection(canvas, landmarkMap, PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER, torsoPaint)
        drawConnection(canvas, landmarkMap, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP, torsoPaint)
        drawConnection(canvas, landmarkMap, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP, torsoPaint)
        drawConnection(canvas, landmarkMap, PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP, torsoPaint)
        
        // Draw head connections
        drawConnection(canvas, landmarkMap, PoseLandmark.LEFT_EAR, PoseLandmark.LEFT_EYE)
        drawConnection(canvas, landmarkMap, PoseLandmark.RIGHT_EAR, PoseLandmark.RIGHT_EYE)
        drawConnection(canvas, landmarkMap, PoseLandmark.LEFT_EYE, PoseLandmark.NOSE)
        drawConnection(canvas, landmarkMap, PoseLandmark.RIGHT_EYE, PoseLandmark.NOSE)
        drawConnection(canvas, landmarkMap, PoseLandmark.LEFT_MOUTH, PoseLandmark.RIGHT_MOUTH)
        
        // Draw left arm
        drawConnection(canvas, landmarkMap, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW)
        drawConnection(canvas, landmarkMap, PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST)
        drawConnection(canvas, landmarkMap, PoseLandmark.LEFT_WRIST, PoseLandmark.LEFT_THUMB)
        drawConnection(canvas, landmarkMap, PoseLandmark.LEFT_WRIST, PoseLandmark.LEFT_PINKY)
        drawConnection(canvas, landmarkMap, PoseLandmark.LEFT_WRIST, PoseLandmark.LEFT_INDEX)
        
        // Draw right arm
        drawConnection(canvas, landmarkMap, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW)
        drawConnection(canvas, landmarkMap, PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST)
        drawConnection(canvas, landmarkMap, PoseLandmark.RIGHT_WRIST, PoseLandmark.RIGHT_THUMB)
        drawConnection(canvas, landmarkMap, PoseLandmark.RIGHT_WRIST, PoseLandmark.RIGHT_PINKY)
        drawConnection(canvas, landmarkMap, PoseLandmark.RIGHT_WRIST, PoseLandmark.RIGHT_INDEX)
        
        // Draw left leg
        drawConnection(canvas, landmarkMap, PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE)
        drawConnection(canvas, landmarkMap, PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE)
        drawConnection(canvas, landmarkMap, PoseLandmark.LEFT_ANKLE, PoseLandmark.LEFT_HEEL)
        drawConnection(canvas, landmarkMap, PoseLandmark.LEFT_ANKLE, PoseLandmark.LEFT_FOOT_INDEX)
        
        // Draw right leg
        drawConnection(canvas, landmarkMap, PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE)
        drawConnection(canvas, landmarkMap, PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE)
        drawConnection(canvas, landmarkMap, PoseLandmark.RIGHT_ANKLE, PoseLandmark.RIGHT_HEEL)
        drawConnection(canvas, landmarkMap, PoseLandmark.RIGHT_ANKLE, PoseLandmark.RIGHT_FOOT_INDEX)
    }
    
    private fun drawConnection(
        canvas: Canvas, 
        landmarkMap: Map<Int, LandmarkPoint>,
        startLandmarkType: Int,
        endLandmarkType: Int,
        paint: Paint = linePaint // Default to regular line paint
    ) {
        val startLandmark = landmarkMap[startLandmarkType]
        val endLandmark = landmarkMap[endLandmarkType]
        
        if (startLandmark != null && endLandmark != null) {
            // Only draw connections if both landmarks have sufficient confidence
            if (startLandmark.inFrameLikelihood >= confidenceThreshold && endLandmark.inFrameLikelihood>= confidenceThreshold) {
                // Reuse point objects to reduce garbage collection
                val startPoint = PointF()
                val endPoint = PointF()
                
                convertCoordinatesToScreen(startLandmark.x, startLandmark.y, startPoint)
                convertCoordinatesToScreen(endLandmark.x, endLandmark.y, endPoint)
                
                // Calculate average confidence to adjust line opacity
                val avgConfidence = (startLandmark.inFrameLikelihood + endLandmark.inFrameLikelihood) / 2f
                val alpha = (255 * (0.2f + avgConfidence * 0.8f)).toInt()
                paint.alpha = alpha
                
                canvas.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y, paint)
                
                // Reset alpha
                paint.alpha = 255
            }
        }
    }
    
    /**
     * Convert normalized coordinates (0-1) to screen coordinates
     */
    // Version with PointF creation for compatibility with existing code
    private fun convertCoordinatesToScreen(normalizedX: Float, normalizedY: Float): PointF {
        val result = PointF(0f, 0f)
        convertCoordinatesToScreen(normalizedX, normalizedY, result)
        return result
    }
    
    // More efficient version that reuses a PointF object to reduce garbage collection
    private fun convertCoordinatesToScreen(normalizedX: Float, normalizedY: Float, point: PointF) {
        // ML Kit pose detection gives normalized coordinates (0-1)
        // Need to convert to screen coordinates
        // Note: we need to flip the x-axis only if using front camera
        point.x = if (isFrontCamera) {
            (1 - normalizedX) * viewWidth // Flip X for front camera
        } else {
            normalizedX * viewWidth // Don't flip for back camera
        }
        point.y = normalizedY * viewHeight
    }
}
