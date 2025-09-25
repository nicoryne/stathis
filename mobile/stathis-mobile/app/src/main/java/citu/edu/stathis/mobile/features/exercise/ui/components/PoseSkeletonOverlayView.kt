package citu.edu.stathis.mobile.features.exercise.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

class PoseSkeletonOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var pose: Pose? = null
    private var frameWidth: Int = 0
    private var frameHeight: Int = 0
    private var rotationDegrees: Int = 0
    private var isMirrored: Boolean = true

    private val jointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
        style = Paint.Style.FILL
        strokeWidth = 6f
    }

    private val bonePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.CYAN
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    fun updatePose(newPose: Pose?, w: Int, h: Int, rotation: Int, mirrored: Boolean) {
        pose = newPose
        frameWidth = w
        frameHeight = h
        rotationDegrees = rotation
        isMirrored = mirrored
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val p = pose ?: return
        val landmarks = p.allPoseLandmarks
        if (landmarks.isEmpty()) return

        val (scaleX, scaleY) = computeScale()

        drawConnection(canvas, p, PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER, scaleX, scaleY)
        drawConnection(canvas, p, PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP, scaleX, scaleY)
        drawConnection(canvas, p, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW, scaleX, scaleY)
        drawConnection(canvas, p, PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST, scaleX, scaleY)
        drawConnection(canvas, p, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW, scaleX, scaleY)
        drawConnection(canvas, p, PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST, scaleX, scaleY)
        drawConnection(canvas, p, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP, scaleX, scaleY)
        drawConnection(canvas, p, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP, scaleX, scaleY)
        drawConnection(canvas, p, PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE, scaleX, scaleY)
        drawConnection(canvas, p, PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE, scaleX, scaleY)
        drawConnection(canvas, p, PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE, scaleX, scaleY)
        drawConnection(canvas, p, PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE, scaleX, scaleY)
        drawConnection(canvas, p, PoseLandmark.NOSE, PoseLandmark.LEFT_EYE, scaleX, scaleY)
        drawConnection(canvas, p, PoseLandmark.NOSE, PoseLandmark.RIGHT_EYE, scaleX, scaleY)
        drawConnection(canvas, p, PoseLandmark.LEFT_EYE, PoseLandmark.LEFT_EAR, scaleX, scaleY)
        drawConnection(canvas, p, PoseLandmark.RIGHT_EYE, PoseLandmark.RIGHT_EAR, scaleX, scaleY)

        for (lm in landmarks) {
            val conf = lm.inFrameLikelihood
            if (conf >= 0.3f) {
                val (vx, vy) = mapPoint(lm.position.x, lm.position.y, scaleX, scaleY)
                canvas.drawCircle(vx, vy, 6f, jointPaint)
            }
        }
    }

    private fun drawConnection(
        canvas: Canvas,
        pose: Pose,
        startType: Int,
        endType: Int,
        scaleX: Float,
        scaleY: Float
    ) {
        val sLm = pose.getPoseLandmark(startType) ?: return
        val eLm = pose.getPoseLandmark(endType) ?: return
        if (sLm.inFrameLikelihood < 0.3f || eLm.inFrameLikelihood < 0.3f) return
        val (sx, sy) = mapPoint(sLm.position.x, sLm.position.y, scaleX, scaleY)
        val (ex, ey) = mapPoint(eLm.position.x, eLm.position.y, scaleX, scaleY)
        canvas.drawLine(sx, sy, ex, ey, bonePaint)
    }

    private fun computeScale(): Pair<Float, Float> {
        if (frameWidth == 0 || frameHeight == 0 || width == 0 || height == 0) return 1f to 1f
        val rotated = rotationDegrees % 180 != 0
        val sourceW = if (rotated) frameHeight else frameWidth
        val sourceH = if (rotated) frameWidth else frameHeight
        val scale = maxOf(width.toFloat() / sourceW, height.toFloat() / sourceH)
        return scale to scale
    }

    private fun mapPoint(x: Float, y: Float, scaleX: Float, scaleY: Float): Pair<Float, Float> {
        val sourceW = if (rotationDegrees % 180 == 0) frameWidth else frameHeight
        val sourceH = if (rotationDegrees % 180 == 0) frameHeight else frameWidth

        var rx = x
        val ry = y
        if (isMirrored) rx = sourceW - rx

        val (sx, sy) = computeScale()
        val drawnW = sourceW * sx
        val drawnH = sourceH * sy
        val offsetX = (width - drawnW) / 2f
        val offsetY = (height - drawnH) / 2f

        val vx = offsetX + rx * sx
        val vy = offsetY + ry * sy
        return vx to vy
    }
}


