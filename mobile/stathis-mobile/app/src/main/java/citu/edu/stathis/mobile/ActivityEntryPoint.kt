package citu.edu.stathis.mobile

import android.app.Application
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import citu.edu.stathis.mobile.features.onboarding.navigation.OnboardingNavHost
import citu.edu.stathis.mobile.core.theme.AppThemeWithProvider
import citu.edu.stathis.mobile.core.theme.ThemeViewModel
import citu.edu.stathis.mobile.core.theme.ThemePreferences
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.collect
import cit.edu.stathis.mobile.BuildConfig
import citu.edu.stathis.mobile.features.home.ui.AppShell
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ActivityEntryPoint : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            
            AppThemeWithProvider(themeViewModel = themeViewModel) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    val context = LocalContext.current
                    val prefs = remember { ThemePreferences(context) }
                    val onboarded by prefs.onboarded.collectAsState(initial = false)
                    var goHome by remember { mutableStateOf(false) }

                    if (goHome || (onboarded && !BuildConfig.ALWAYS_SHOW_ONBOARDING)) {
                        AppShell()
                    } else {
                        OnboardingNavHost(
                            navController = navController,
                            onFinished = {
                                goHome = true
                            },
                            themeViewModel = themeViewModel
                        )
                    }
                }
            }
        }
    }
}