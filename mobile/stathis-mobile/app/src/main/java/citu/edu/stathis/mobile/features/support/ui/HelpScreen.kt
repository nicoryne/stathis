package citu.edu.stathis.mobile.features.support.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun HelpScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Help",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.navigationBars.only(sides = WindowInsetsSides.Bottom)),
            horizontalAlignment = Alignment.Start
        ) {
            SectionTitle("Getting started")
            BodyText("Make sure you're on the latest version of Stathis. If something looks off, try closing and reopening the app.")

            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle("Troubleshooting")
            Bullet("Check your internet connection.")
            Bullet("If Health Connect data isn't showing, open Settings → App Settings → Health Connect and ensure permissions are granted.")
            Bullet("To test camera, go to Settings → Camera test.")
            Bullet("Clear app cache if UI elements fail to load (device Settings → Apps → Stathis → Storage → Clear cache).")

            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle("Contact us")
            BodyTextAnnotated {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Email: ") }
                append("support@stathis.app")
            }
            BodyText("We typically respond within 1–2 business days.")
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onSurface)
}

@Composable
private fun BodyText(text: String) {
    Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun BodyTextAnnotated(content: androidx.compose.ui.text.AnnotatedString.Builder.() -> Unit) {
    Text(buildAnnotatedString(content), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun Bullet(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text("•  ", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}


