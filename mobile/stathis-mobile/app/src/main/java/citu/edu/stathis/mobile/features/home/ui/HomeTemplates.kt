package citu.edu.stathis.mobile.features.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import citu.edu.stathis.mobile.core.learn.LearnPreferences
import citu.edu.stathis.mobile.features.exercise.data.templates.ExerciseTemplate
import citu.edu.stathis.mobile.features.exercise.data.templates.generateTemplatesForLevel
import citu.edu.stathis.mobile.features.onboarding.domain.model.ExperienceLevel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment

// Note: LearnScreen and PracticeScreen are now defined in their own files:
// - LearnScreen.kt (with classroom enrollment features)
// - PracticeScreen.kt (with dashboard features)

// --- Practice Subscreens ---
@Composable
fun PracticeExercisesScreen(navController: NavHostController) {
    val context = LocalContext.current
    val learnPrefs = remember { LearnPreferences(context) }
    val level by learnPrefs.levelFlow.collectAsState(initial = ExperienceLevel.BEGINNER)
    val templates: List<ExerciseTemplate> = remember(level) { generateTemplatesForLevel(level) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Exercises",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Level: ${level.name.lowercase().replaceFirstChar { it.uppercase() }}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        templates.forEachIndexed { index, t ->
            Button(
                onClick = { navController.navigate("practice_exercise_preview/${t.physicalId}") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("${t.title}")
            }
            if (index < templates.lastIndex) Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun PracticeExercisePreviewScreen(exerciseId: String, navController: NavHostController) {
    val context = LocalContext.current
    val learnPrefs = remember { LearnPreferences(context) }
    val level by learnPrefs.levelFlow.collectAsState(initial = ExperienceLevel.BEGINNER)
    val template = remember(level, exerciseId) { generateTemplatesForLevel(level).find { it.physicalId == exerciseId } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = template?.title ?: "Exercise Preview",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = template?.description ?: "ID: $exerciseId",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Make sure Health Connect is enabled to show vitals.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (template != null) {
                Text(
                    text = "Goals: " + listOfNotNull(
                        template.goalReps?.let { "${it} reps" },
                        template.goalTime?.let { "${it}s" },
                        template.goalAccuracy?.let { "${it}% accuracy" }
                    ).joinToString(" • "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Column(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { navController.navigate("practice_session/$exerciseId") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) { Text("Start Exercise") }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { navController.navigate("health_connect") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) { Text("Review Health Connect") }
        }
    }
}

@Composable
fun PracticeExerciseSessionScreen(exerciseId: String, navController: NavHostController) {
    citu.edu.stathis.mobile.features.exercise.ui.screens.ExerciseLiveScreen()
}


