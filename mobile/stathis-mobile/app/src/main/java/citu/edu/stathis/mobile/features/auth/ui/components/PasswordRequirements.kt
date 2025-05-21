package citu.edu.stathis.mobile.features.auth.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import citu.edu.stathis.mobile.features.auth.ui.utils.PasswordValidator.hasLowerCase
import citu.edu.stathis.mobile.features.auth.ui.utils.PasswordValidator.hasMinLength
import citu.edu.stathis.mobile.features.auth.ui.utils.PasswordValidator.hasNumber
import citu.edu.stathis.mobile.features.auth.ui.utils.PasswordValidator.hasSpecialChar
import citu.edu.stathis.mobile.features.auth.ui.utils.PasswordValidator.hasUpperCase
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close

@Composable
fun PasswordRequirements(
    password: String,
    isVisible: Boolean
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = "Password must have:",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            PasswordRequirementItem(
                message = "At least 8 characters",
                isMet = password.hasMinLength(8)
            )

            PasswordRequirementItem(
                message = "At least one uppercase letter",
                isMet = password.hasUpperCase()
            )

            PasswordRequirementItem(
                message = "At least one lowercase letter",
                isMet = password.hasLowerCase()
            )

            PasswordRequirementItem(
                message = "At least one number",
                isMet = password.hasNumber()
            )

            PasswordRequirementItem(
                message = "At least one special character",
                isMet = password.hasSpecialChar()
            )
        }
    }
}

@Composable
fun PasswordRequirementItem(
    message: String,
    isMet: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.Check else Icons.Default.Close,
            contentDescription = if (isMet) "Requirement met" else "Requirement not met",
            tint = if (isMet) Color(0xFF81C784) else Color(0xFFE57373),
            modifier = Modifier.size(16.dp)
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}