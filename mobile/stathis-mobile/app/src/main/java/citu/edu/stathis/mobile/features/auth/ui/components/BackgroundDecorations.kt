package citu.edu.stathis.mobile.features.auth.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun BackgroundDecorations(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Draw decorative elements
            val purpleColor = Color(0xFF9334EA)
            val tealColor = Color(0xFF25ACA4)

            // Bottom right decorative circle
            drawCircle(
                color = tealColor.copy(alpha = 0.1f),
                radius = width * 0.2f,
                center = Offset(width * 0.9f, height * 0.85f)
            )

            // Bottom left decorative circle outline
            drawCircle(
                color = purpleColor.copy(alpha = 0.1f),
                radius = width * 0.15f,
                center = Offset(width * 0.1f, height * 0.75f),
                style = Stroke(width = 5f)
            )

            // Middle decorative dots
            for (i in 0..8) {
                drawCircle(
                    color = if (i % 2 == 0) purpleColor.copy(alpha = 0.1f) else tealColor.copy(alpha = 0.1f),
                    radius = 8f,
                    center = Offset(width * (0.2f + i * 0.07f), height * 0.5f)
                )
            }
        }
    }
}