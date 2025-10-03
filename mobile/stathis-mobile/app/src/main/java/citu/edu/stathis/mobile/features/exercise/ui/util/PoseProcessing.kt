package citu.edu.stathis.mobile.features.exercise.ui.util

data class Landmark(val x: Float, val y: Float, val z: Float, val v: Float)

private const val HIP_L = 23; private const val HIP_R = 24
private const val SHOULDER_L = 11; private const val SHOULDER_R = 12

fun normalizeFrame(raw: List<Landmark>): FloatArray {
    val hipC = midpoint(raw[HIP_L], raw[HIP_R])
    val shC = midpoint(raw[SHOULDER_L], raw[SHOULDER_R])
    val torso = distance(shC, hipC).coerceAtLeast(1e-6f)
    val out = FloatArray(33 * 4)
    var i = 0
    for (lm in raw) {
        val cx = (lm.x - hipC.x) / torso
        val cy = (lm.y - hipC.y) / torso
        val cz = (lm.z - hipC.z) / torso
        out[i++] = cx; out[i++] = cy; out[i++] = cz; out[i++] = lm.v
    }
    return out
}

private fun midpoint(a: Landmark, b: Landmark) = Landmark(
    (a.x + b.x) * 0.5f, (a.y + b.y) * 0.5f, (a.z + b.z) * 0.5f, 1f
)

private fun distance(a: Landmark, b: Landmark): Float {
    val dx = a.x - b.x; val dy = a.y - b.y; val dz = a.z - b.z
    return kotlin.math.sqrt(dx*dx + dy*dy + dz*dz)
}


