package citu.edu.stathis.mobile.features.auth.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class PasswordStrength {
    EMPTY,
    WEAK,
    MEDIUM,
    STRONG
}

@Composable
fun PasswordStrengthIndicator(
    modifier: Modifier = Modifier,
    password: String,
    strength: PasswordStrength
) {
    val strengthColor by animateColorAsState(
        targetValue = when (strength) {
            PasswordStrength.EMPTY -> MaterialTheme.colorScheme.outline
            PasswordStrength.WEAK -> Color(0xFFE57373)  // Light Red
            PasswordStrength.MEDIUM -> Color(0xFFFFD54F)  // Amber
            PasswordStrength.STRONG -> Color(0xFF81C784)  // Light Green
        },
        animationSpec = tween(300),
        label = "strengthColor"
    )

    val strengthText = when (strength) {
        PasswordStrength.EMPTY -> ""
        PasswordStrength.WEAK -> "Weak"
        PasswordStrength.MEDIUM -> "Medium"
        PasswordStrength.STRONG -> "Strong"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (password.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(
                                when (strength) {
                                    PasswordStrength.EMPTY -> 0f
                                    PasswordStrength.WEAK -> 0.33f
                                    PasswordStrength.MEDIUM -> 0.66f
                                    PasswordStrength.STRONG -> 1f
                                }
                            )
                            .height(4.dp)
                            .background(strengthColor)
                    )
                }
            }

            if (strengthText.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strengthText,
                    style = MaterialTheme.typography.labelSmall,
                    color = strengthColor
                )
            }
        }
    }
}