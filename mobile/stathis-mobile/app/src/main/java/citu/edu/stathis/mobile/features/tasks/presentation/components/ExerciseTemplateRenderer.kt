package citu.edu.stathis.mobile.features.tasks.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyTemperatureRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.RespiratoryRateRecord
import citu.edu.stathis.mobile.features.tasks.data.model.ExerciseTemplate
import citu.edu.stathis.mobile.features.tasks.data.model.ExercisePerformance
import citu.edu.stathis.mobile.features.exercise.ui.screens.ExerciseScreen
import citu.edu.stathis.mobile.features.vitals.ui.HealthConnectViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController

@Composable
fun ExerciseTemplateRenderer(
    template: ExerciseTemplate,
    onComplete: (ExercisePerformance) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExerciseStarted by remember { mutableStateOf(false) }
    var isExerciseCompleted by remember { mutableStateOf(false) }
    var exercisePerformance by remember { mutableStateOf<ExercisePerformance?>(null) }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Exercise Header
        ExerciseHeader(
            template = template,
            isStarted = isExerciseStarted,
            isCompleted = isExerciseCompleted,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Content Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (!isExerciseStarted) {
                ExerciseInstructions(
                    template = template,
                    onStart = { isExerciseStarted = true },
                    modifier = Modifier.fillMaxSize()
                )
            } else if (!isExerciseCompleted) {
                // Use the real exercise screen with camera and pose classification
                ExerciseScreen(navController = rememberNavController())
                
                // Overlay with exercise controls
                ExerciseControlsOverlay(
                    template = template,
                    onComplete = { performance ->
                        exercisePerformance = performance
                        isExerciseCompleted = true
                        onComplete(performance)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                exercisePerformance?.let { performance ->
                    ExerciseResults(
                        template = template,
                        performance = performance,
                        onRetry = {
                            isExerciseStarted = false
                            isExerciseCompleted = false
                            exercisePerformance = null
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseHeader(
    template: ExerciseTemplate,
    isStarted: Boolean,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Title
            Text(
                text = template.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Description
            Text(
                text = template.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Exercise Type and Difficulty
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = template.exerciseType.replace("_", " "),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = template.exerciseDifficulty,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseInstructions(
    template: ExerciseTemplate,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Exercise Icon
            Icon(
                imageVector = when (template.exerciseType) {
                    "PUSH_UP" -> Icons.Default.FitnessCenter
                    "SQUATS" -> Icons.Default.DirectionsRun
                    else -> Icons.Default.Sports
                },
                contentDescription = "Exercise",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Instructions Title
            Text(
                text = "Exercise Instructions",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Goals
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Your Goals:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    GoalItem(
                        icon = Icons.Default.Repeat,
                        label = "Repetitions",
                        value = "${template.goalReps} reps"
                    )
                    
                    GoalItem(
                        icon = Icons.Default.Timer,
                        label = "Time",
                        value = "${template.goalTime} seconds"
                    )
                    
                    GoalItem(
                        icon = Icons.Default.GpsFixed,
                        label = "Accuracy",
                        value = "${template.goalAccuracy}%"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Start Button
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start Exercise",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun GoalItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ExerciseInProgress(
    template: ExerciseTemplate,
    onComplete: (ExercisePerformance) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentReps by remember { mutableIntStateOf(0) }
    var currentTime by remember { mutableIntStateOf(0) }
    var currentAccuracy by remember { mutableFloatStateOf(0f) }
    var isTimerRunning by remember { mutableStateOf(false) }
    
    // Simulate exercise progress (in real app, this would come from pose detection)
    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            while (currentTime < template.goalTime && currentReps < template.goalReps) {
                kotlinx.coroutines.delay(1000)
                currentTime++
                if (currentTime % 3 == 0) { // Simulate rep every 3 seconds
                    currentReps++
                    currentAccuracy = (70f + (currentReps * 2f)).coerceAtMost(95f) // Simulate improving accuracy
                }
            }
            
            // Exercise completed
            val performance = ExercisePerformance(
                taskId = "",
                templateId = template.physicalId,
                actualReps = currentReps,
                actualAccuracy = currentAccuracy,
                actualTime = currentTime,
                goalReps = template.goalReps,
                goalAccuracy = template.goalAccuracy,
                goalTime = template.goalTime,
                isCompleted = currentReps >= template.goalReps && currentAccuracy >= template.goalAccuracy,
                score = calculateScore(currentReps, currentAccuracy, currentTime, template)
            )
            onComplete(performance)
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Exercise Status
            Text(
                text = "Exercise in Progress",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Progress Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProgressIndicator(
                    label = "Reps",
                    current = currentReps,
                    goal = template.goalReps,
                    icon = Icons.Default.Repeat
                )
                
                ProgressIndicator(
                    label = "Time",
                    current = currentTime,
                    goal = template.goalTime,
                    icon = Icons.Default.Timer
                )
                
                ProgressIndicator(
                    label = "Accuracy",
                    current = currentAccuracy.toInt(),
                    goal = template.goalAccuracy,
                    icon = Icons.Default.GpsFixed
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Start/Stop Button
            Button(
                onClick = { isTimerRunning = !isTimerRunning },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTimerRunning) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(
                    imageVector = if (isTimerRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isTimerRunning) "Stop" else "Start",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isTimerRunning) "Stop Exercise" else "Start Exercise",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun ProgressIndicator(
    label: String,
    current: Int,
    goal: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val progress = if (goal > 0) (current.toFloat() / goal).coerceAtMost(1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(300),
        label = "progress"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "$current/$goal",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LinearProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier
                .width(60.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    }
}

@Composable
private fun ExerciseControlsOverlay(
    template: ExerciseTemplate,
    onComplete: (ExercisePerformance) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentReps by remember { mutableIntStateOf(0) }
    var currentTime by remember { mutableIntStateOf(0) }
    var currentAccuracy by remember { mutableFloatStateOf(0f) }
    var isTimerRunning by remember { mutableStateOf(false) }
    
    val healthConnectViewModel: HealthConnectViewModel = hiltViewModel()
    val connectionState by healthConnectViewModel.connectionState.collectAsState()
    
    // Health Connect permission launcher
    val requiredPermissions = remember {
        setOf(
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(BloodPressureRecord::class),
            HealthPermission.getReadPermission(BodyTemperatureRecord::class),
            HealthPermission.getReadPermission(OxygenSaturationRecord::class),
            HealthPermission.getReadPermission(RespiratoryRateRecord::class)
        )
    }
    
    val permissionLauncher = rememberLauncherForActivityResult<Set<String>, Set<String>>(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        if (grantedPermissions.containsAll(requiredPermissions)) {
            healthConnectViewModel.connect()
            healthConnectViewModel.startMonitoring()
        }
    }
    
    // Real-time rep counting and posture validation would come from pose detection
    // For now, we'll simulate the progress based on the template goals
    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            while (currentTime < template.goalTime && currentReps < template.goalReps) {
                kotlinx.coroutines.delay(1000)
                currentTime++
                if (currentTime % 3 == 0) { // Simulate rep every 3 seconds
                    currentReps++
                    currentAccuracy = (70f + (currentReps * 2f)).coerceAtMost(95f) // Simulate improving accuracy
                }
            }
            
            // Exercise completed
            val performance = ExercisePerformance(
                taskId = "",
                templateId = template.physicalId,
                actualReps = currentReps,
                actualAccuracy = currentAccuracy,
                actualTime = currentTime,
                goalReps = template.goalReps,
                goalAccuracy = template.goalAccuracy,
                goalTime = template.goalTime,
                isCompleted = currentReps >= template.goalReps && currentAccuracy >= template.goalAccuracy,
                score = calculateScore(currentReps, currentAccuracy, currentTime, template)
            )
            onComplete(performance)
        }
    }
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Progress overlay at the top
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${template.exerciseType} - ${template.exerciseDifficulty}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Progress indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProgressIndicator(
                        label = "Reps",
                        current = currentReps,
                        goal = template.goalReps,
                        icon = Icons.Default.Repeat
                    )
                    
                    ProgressIndicator(
                        label = "Time",
                        current = currentTime,
                        goal = template.goalTime,
                        icon = Icons.Default.Timer
                    )
                    
                    ProgressIndicator(
                        label = "Accuracy",
                        current = currentAccuracy.toInt(),
                        goal = template.goalAccuracy,
                        icon = Icons.Default.GpsFixed
                    )
                }
            }
        }
        
        // Control buttons at the bottom
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { 
                        if (!isTimerRunning) {
                            // Prompt for Health Connect permissions when starting exercise
                            if (connectionState == citu.edu.stathis.mobile.features.vitals.data.HealthConnectManager.ConnectionState.DISCONNECTED) {
                                permissionLauncher.launch(requiredPermissions)
                            } else {
                                healthConnectViewModel.startMonitoring()
                            }
                        }
                        isTimerRunning = !isTimerRunning 
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTimerRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (isTimerRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (isTimerRunning) "Stop" else "Start"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isTimerRunning) "Stop" else "Start")
                }
                
                Button(
                    onClick = {
                        // Complete exercise manually
                        val performance = ExercisePerformance(
                            taskId = "",
                            templateId = template.physicalId,
                            actualReps = currentReps,
                            actualAccuracy = currentAccuracy,
                            actualTime = currentTime,
                            goalReps = template.goalReps,
                            goalAccuracy = template.goalAccuracy,
                            goalTime = template.goalTime,
                            isCompleted = currentReps >= template.goalReps && currentAccuracy >= template.goalAccuracy,
                            score = calculateScore(currentReps, currentAccuracy, currentTime, template)
                        )
                        onComplete(performance)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Complete"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Complete")
                }
            }
        }
    }
}

@Composable
private fun ExerciseResults(
    template: ExerciseTemplate,
    performance: ExercisePerformance,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Result Icon
            Icon(
                imageVector = if (performance.isCompleted) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = "Exercise Result",
                modifier = Modifier.size(64.dp),
                tint = if (performance.isCompleted) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Result Title
            Text(
                text = if (performance.isCompleted) "Exercise Completed!" else "Exercise Finished",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Score
            Text(
                text = "Score: ${performance.score}/100",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Performance Details
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Your Performance:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    PerformanceItem(
                        label = "Repetitions",
                        actual = performance.actualReps,
                        goal = performance.goalReps,
                        isGood = performance.actualReps >= performance.goalReps
                    )
                    
                    PerformanceItem(
                        label = "Accuracy",
                        actual = performance.actualAccuracy.toInt(),
                        goal = performance.goalAccuracy,
                        isGood = performance.actualAccuracy >= performance.goalAccuracy,
                        suffix = "%"
                    )
                    
                    PerformanceItem(
                        label = "Time",
                        actual = performance.actualTime,
                        goal = performance.goalTime,
                        isGood = performance.actualTime <= performance.goalTime,
                        suffix = "s"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Retry Button
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Again")
            }
        }
    }
}

@Composable
private fun PerformanceItem(
    label: String,
    actual: Int,
    goal: Int,
    isGood: Boolean,
    suffix: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$actual$suffix",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isGood) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
            
            Text(
                text = " / $goal$suffix",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Icon(
                imageVector = if (isGood) Icons.Default.Check else Icons.Default.Close,
                contentDescription = if (isGood) "Goal achieved" else "Goal not achieved",
                modifier = Modifier.size(16.dp),
                tint = if (isGood) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
    }
}

private fun calculateScore(
    actualReps: Int,
    actualAccuracy: Float,
    actualTime: Int,
    template: ExerciseTemplate
): Int {
    val repsScore = (actualReps.toFloat() / template.goalReps * 40f).coerceAtMost(40f)
    val accuracyScore = (actualAccuracy / template.goalAccuracy * 40f).coerceAtMost(40f)
    val timeScore = if (actualTime <= template.goalTime) 20f else {
        (template.goalTime.toFloat() / actualTime * 20f).coerceAtLeast(0f)
    }
    
    return (repsScore + accuracyScore + timeScore).toInt()
}
