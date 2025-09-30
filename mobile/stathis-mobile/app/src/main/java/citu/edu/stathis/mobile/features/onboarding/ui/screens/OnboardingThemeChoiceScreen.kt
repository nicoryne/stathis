package citu.edu.stathis.mobile.features.onboarding.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import citu.edu.stathis.mobile.core.theme.ThemeMode
import citu.edu.stathis.mobile.core.theme.ThemeViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.ui.text.font.FontStyle
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import cit.edu.stathis.mobile.R

@Composable
fun OnboardingThemeChoiceScreen(
    onContinue: () -> Unit,
    themeViewModel: ThemeViewModel = viewModel()
) {
    var selected by remember { mutableStateOf<ThemeMode?>(null) }
    val currentTheme by themeViewModel.currentTheme.collectAsState()
    val cs = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 24.dp)
            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Which theme do you prefer?",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(36.dp))

            Column(verticalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.fillMaxWidth()) {
                ThemeOption(
                    emoji = "🌞",
                    title = "Light mode",
                    container = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = {
                        themeViewModel.setThemeMode(ThemeMode.LIGHT)
                        selected = ThemeMode.LIGHT
                    }
                )
                ThemeOption(
                    emoji = "🌙",
                    title = "Dark mode",
                    container = MaterialTheme.colorScheme.surfaceVariant,
                    onClick = {
                        themeViewModel.setThemeMode(ThemeMode.DARK)
                        selected = ThemeMode.DARK
                    }
                )
            }
        }


        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "You can change this later",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontStyle = FontStyle.Italic
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))


        if (selected != null) {
            Button(
                onClick = {
                    selected?.let { themeViewModel.setThemeMode(it) }
                    onContinue()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.elevatedButtonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 2.dp,
                    focusedElevation = 8.dp
                )
            ) {
                Text(
                    text = "Confirm Selection",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                    )
                )
            }
        } else {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ThemeOption(
    emoji: String,
    title: String,
    container: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(container)
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = emoji, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}


