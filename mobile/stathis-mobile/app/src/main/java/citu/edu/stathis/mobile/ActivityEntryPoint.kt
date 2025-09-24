package citu.edu.stathis.mobile

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import citu.edu.stathis.mobile.core.navigation.CoreNavigationController
import citu.edu.stathis.mobile.core.theme.StathisTheme
import citu.edu.stathis.mobile.core.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ActivityEntryPoint : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val isDarkMode by themeViewModel.isDarkMode.collectAsState()
            val isDynamicColorEnabled by themeViewModel.isDynamicColorEnabled.collectAsState()

            StathisTheme(
                isDarkMode = isDarkMode,
                isDynamicColorEnabled = isDynamicColorEnabled
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    CoreNavigationController()
                }
            }
        }
    }
}