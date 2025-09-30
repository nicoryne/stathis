package citu.edu.stathis.mobile.features.onboarding.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cit.edu.stathis.mobile.R
import kotlinx.coroutines.delay
import citu.edu.stathis.mobile.core.theme.ThemePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.alpha
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import android.Manifest
import android.os.Build
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import com.google.accompanist.permissions.PermissionStatus
import kotlinx.coroutines.withTimeoutOrNull
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OnboardingLoadingScreen(
    isStudent: Boolean,
    level: Int,
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    var progress by remember { mutableStateOf(0f) }
    var fading by remember { mutableStateOf(false) }
    val contentAlpha by animateFloatAsState(
        targetValue = if (fading) 0f else 1f,
        animationSpec = tween(durationMillis = 320), label = "loadingFade"
    )
    // Simulate background work then call onFinished
    // Build runtime permission list
    val requiredPermissions = remember {
        buildList {
            add(Manifest.permission.CAMERA)
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_CONNECT)
            } else {
                add(Manifest.permission.BLUETOOTH)
            }
        }
    }
    val permissionsState = rememberMultiplePermissionsState(requiredPermissions)
    // Health Connect consent is now handled in a dedicated onboarding screen.

    LaunchedEffect(isStudent, level) {
        val prefs = ThemePreferences(context)
        // Step 1: persist selections
        withContext(Dispatchers.IO) {
            prefs.setOnboardingRole(if (isStudent) "student" else "guest")
            prefs.setOnboardingLevel(level)
            prefs.setOnboarded(true)
        }
        progress = 0.25f

        // Step 2: request runtime permissions and wait for a state change
        if (!permissionsState.allPermissionsGranted) {
            val initialGrants = permissionsState.permissions.map { permissionState ->
                when (permissionState.status) {
                    is PermissionStatus.Granted -> true
                    else -> false
                }
            }
            permissionsState.launchMultiplePermissionRequest()
            // Wait for a change or timeout (prevents hanging if system suppresses dialog)
            withTimeoutOrNull(6000) {
                snapshotFlow {
                    permissionsState.permissions.map { permissionState ->
                        when (permissionState.status) {
                            is PermissionStatus.Granted -> true
                            else -> false
                        }
                    }
                }
                    .distinctUntilChanged()
                    .first { it != initialGrants }
            }
        }
        progress = 0.5f

        // Step 3: Health Connect consent handled earlier via UI flow
        progress = 0.62f

        // Step 4: network check
        val online = withContext(Dispatchers.IO) { isOnline() }
        progress = 0.82f

        // Step 5: warm-up (placeholder; replace with real prefetch)
        withContext(Dispatchers.IO) { delay(400) }
        progress = 1f
        // Gentle pause then fade out before navigating
        delay(150)
        fading = true
        delay(340)
        onFinished()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 24.dp)
            .alpha(contentAlpha),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.mascot_celebrate),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(224.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Setting things up...",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Rotating motivational quotes
        val quotes = listOf(
            "Education is movement from darkness to light.",
            "A little progress each day adds up to big results.",
            "Consistency beats intensity.",
            "Motion is lotion — keep your body moving.",
            "Strong body, sharp mind."
        )
        var quoteIndex by remember { mutableStateOf(0) }
        LaunchedEffect(Unit) {
            while (true) {
                delay(1800)
                quoteIndex = (quoteIndex + 1) % quotes.size
            }
        }
        Text(
            text = quotes[quoteIndex],
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LoadingDots() { }

private fun isOnline(): Boolean {
    return try {
        Socket().use { socket ->
            socket.connect(InetSocketAddress("8.8.8.8", 53), 800)
            true
        }
    } catch (_: Exception) {
        false
    }
}


