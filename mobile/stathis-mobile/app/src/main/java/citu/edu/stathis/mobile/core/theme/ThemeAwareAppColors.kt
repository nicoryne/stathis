package citu.edu.stathis.mobile.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme

/**
 * App-specific brand colors that are constant and don't change with theme
 */
object BrandColors {
    // Brand Colors
    val Purple = Color(0xFF9334EA)
    val Teal = Color(0xFF25ACA4)
}

/**
 * App-specific colors that can be used throughout the UI
 */
object AppColors {
    // Brand Colors
    val Purple = BrandColors.Purple
    val Teal = BrandColors.Teal

    // Gradient Colors
    val PurpleGradientStart = Purple
    val PurpleGradientEnd = Purple.copy(alpha = 0.8f)

    // UI Element Colors
    val ButtonPrimary = Purple
    val ButtonSecondary = Teal
    val IconTint = Purple
    val TextFieldBorder = Purple
    val TextFieldCursor = Purple

    // Background Colors
    val BackgroundCirclePrimary = Teal.copy(alpha = 0.2f)
    val BackgroundCircleSecondary = Color.White.copy(alpha = 0.15f)

    // Text Colors
    val TextPrimary = Purple
    val TextSecondary = Color.Gray

    // Loading Overlay
    val LoadingOverlay = Color.Black.copy(alpha = 0.5f)
}

/**
 * Composable function to get theme-aware colors
 */
@Composable
fun appColors(): AppColorsExtension {
    return AppColorsExtension(
        // Brand colors
        brandPurple = BrandColors.Purple,
        brandTeal = BrandColors.Teal,

        // UI element colors
        buttonPrimary = BrandColors.Purple,
        buttonSecondary = BrandColors.Teal,
        iconTint = BrandColors.Purple,
        textFieldBorder = BrandColors.Purple,
        textFieldCursor = BrandColors.Purple,

        // Background colors
        backgroundCirclePrimary = AppColors.BackgroundCirclePrimary,
        backgroundCircleSecondary = AppColors.BackgroundCircleSecondary,

        // Text colors
        textPrimary = BrandColors.Purple,
        textSecondary = MaterialTheme.colorScheme.onSurfaceVariant,

        // Loading overlay
        loadingOverlay = AppColors.LoadingOverlay
    )
}

/**
 * Class that holds all the custom app colors
 */
data class AppColorsExtension(
    // Brand colors
    val brandPurple: Color,
    val brandTeal: Color,

    // UI element colors
    val buttonPrimary: Color,
    val buttonSecondary: Color,
    val iconTint: Color,
    val textFieldBorder: Color,
    val textFieldCursor: Color,

    // Background colors
    val backgroundCirclePrimary: Color,
    val backgroundCircleSecondary: Color,

    // Text colors
    val textPrimary: Color,
    val textSecondary: Color,

    // Loading overlay
    val loadingOverlay: Color
)