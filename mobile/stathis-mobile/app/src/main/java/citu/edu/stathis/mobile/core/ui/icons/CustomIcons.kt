package citu.edu.stathis.mobile.core.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Custom icons that aren't available in the standard Material Icons
 */
object CustomIcons {
    /**
     * Returns a DirectionsRun icon as a substitute for Fitness icon
     */
    val Fitness: ImageVector
        get() = Icons.Default.DirectionsRun
}
