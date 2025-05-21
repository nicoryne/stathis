package citu.edu.stathis.mobile.features.auth.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import citu.edu.stathis.mobile.core.theme.AppColors
import citu.edu.stathis.mobile.core.theme.appColors

@Composable
fun AuthBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = appColors()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Draw the curved header
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Create a path for the curved header
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(width, 0f)
                lineTo(width, height * 0.25f)
                quadraticBezierTo(
                    width * 0.5f, height * 0.35f,
                    0f, height * 0.25f
                )
                close()
            }

            // Create a gradient brush
            val gradientBrush = Brush.linearGradient(
                colors = listOf(AppColors.PurpleGradientStart, AppColors.PurpleGradientEnd),
                start = Offset(0f, 0f),
                end = Offset(width, height * 0.3f)
            )

            // Draw the path with the gradient
            drawPath(path, gradientBrush)

            // Add some decorative circles
            drawCircle(
                color = colors.backgroundCirclePrimary,
                radius = width * 0.1f,
                center = Offset(width * 0.85f, height * 0.1f)
            )

            drawCircle(
                color = colors.backgroundCircleSecondary,
                radius = width * 0.05f,
                center = Offset(width * 0.15f, height * 0.15f)
            )
        }

        // Content goes here
        content()
    }
}