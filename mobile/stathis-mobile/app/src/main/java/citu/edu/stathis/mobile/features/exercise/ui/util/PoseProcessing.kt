package citu.edu.stathis.mobile.features.exercise.ui.util

data class Landmark(val x: Float, val y: Float, val z: Float, val v: Float)

/**
 * Converts ML Kit pose landmarks to the format expected by the ONNX model.
 * 
 * Model expects:
 * - X, Y: [0.0, 1.0] (absolute normalized coordinates)
 * - Z: [-0.5, 0.5] (relative depth)
 * - InFrameLikelihood: [0.0, 1.0] (confidence)
 * 
 * ML Kit provides landmarks already normalized to image dimensions,
 * so we just need to ensure they're in the correct range.
 */
fun normalizeFrame(raw: List<Landmark>): FloatArray {
    val out = FloatArray(33 * 4)
    var i = 0
    for (lm in raw) {
        // X and Y are already normalized by ML Kit to [0.0, 1.0]
        // Z needs to be clamped to [-0.5, 0.5]
        // InFrameLikelihood is already in [0.0, 1.0]
        out[i++] = lm.x.coerceIn(0f, 1f)
        out[i++] = lm.y.coerceIn(0f, 1f)
        out[i++] = lm.z.coerceIn(-0.5f, 0.5f)
        out[i++] = lm.v.coerceIn(0f, 1f)
    }
    return out
}


