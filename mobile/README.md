# STATHIS Mobile Application

## Overview

The STATHIS mobile application is a key component of the STATHIS system (Smart Tracking and AI-Powered Technology for Health and Instructional Supervision), designed to revolutionize Physical Education through AI-powered posture tracking, health monitoring, and gamified learning. This README compares the features specified in the Software Requirements Specification (SRS) with the current implementation status and provides guidance for implementing missing features.

## SRS Features vs. Current Implementation

### 1. User Authentication and Management Module

| SRS Feature | Implementation Status | Files |
|-------------|------------------------|-------|
| User Registration | ✅ Implemented | `features/auth/ui/register/RegisterScreen.kt`, `RegisterViewModel.kt` |
| User Login & Authentication | ✅ Implemented | `features/auth/ui/login/LoginScreen.kt`, `LoginViewModel.kt` |
| Biometric Authentication | ✅ Implemented | `core/auth/BiometricHelper.kt`, `features/auth/domain/usecase/BiometricAuthUseCase.kt` |
| Role-Based Access Control | ✅ Implemented | `features/auth/data/enums/UserRoles.kt` |
| User Profile Management | ✅ Implemented | `features/profile/ui/ProfileScreen.kt`, `EditProfileScreen.kt` |

### 2. Posture Analysis and Feedback Module

| SRS Feature | Implementation Status | Files |
|-------------|------------------------|-------|
| Posture Data Capture & Tracking | ✅ Implemented | `features/exercise/data/posedetection/PoseDetectionService.kt` |
| Real-Time Posture Analysis | ✅ Implemented | `features/exercise/data/analysis/OnDeviceExerciseAnalyzer.kt` |
| AI-Powered Feedback | ✅ Implemented | `features/exercise/data/OnDeviceFeedback.kt`, `features/exercise/data/PostureFeedback.kt` |
| Performance Tracking | ✅ Implemented | `features/exercise/ui/ExerciseScreen.kt`, `features/progress/ui/ProgressScreen.kt` |

### 3. Health Vitals Monitoring Module

| SRS Feature | Implementation Status | Files |
|-------------|------------------------|-------|
| Smartwatch Integration | ✅ Implemented | `features/vitals/data/HealthConnectManager.kt` |
| Heart Rate & SpO₂ Monitoring | ✅ Implemented | `features/vitals/data/model/VitalSigns.kt` |
| Health Risk Detection | ✅ Implemented | `features/vitals/data/model/HealthRiskAlert.kt`, `VitalsThresholds.kt` |
| Real-Time Alerts | ✅ Implemented | `features/vitals/ui/VitalsScreen.kt`, `VitalsViewModel.kt` |

### 4. Student Task Management & Assessment Module

| SRS Feature | Implementation Status | Files |
|-------------|------------------------|-------|
| Task Assignment View | ✅ Implemented | `features/tasks/ui/TasksScreen.kt`, `features/tasks/presentation/TaskListScreen.kt` |
| Task Completion | ✅ Implemented | `features/tasks/presentation/TaskDetailScreen.kt` |
| Score Tracking | ✅ Implemented | `features/tasks/data/model/ScoreResponse.kt` |
| Progress Reporting | ✅ Implemented | `features/tasks/data/model/TaskProgressResponse.kt` |

### 5. Classroom Integration

| SRS Feature | Implementation Status | Files |
|-------------|------------------------|-------|
| Class Enrollment | ✅ Implemented | `features/classroom/ui/ClassroomScreen.kt` |
| Class Progress Tracking | ✅ Implemented | `features/classroom/data/model/ClassroomProgress.kt` |

## Implementation Guide

### 1. Setting Up the Development Environment

1. **Prerequisites:**
   - Android Studio (latest version)
   - JDK 11 or higher
   - Kotlin 2.1.20 or higher
   - Android device with Android 10.0+ (API level 30+)
   - Xiaomi Smart Band 9 Active (for testing vitals monitoring)

2. **Clone and Setup:**
   ```bash
   git clone <repository-url>
   cd stathis/mobile
   ```

3. **Configure API Endpoints:**
   - Create a `local.properties` file in the project root
   - Add the following properties:
     ```
     base.url=https://your-backend-url.com/api/
     ```

### 2. Implementing User Authentication

The authentication module is already implemented using JWT tokens and biometric authentication. Key components include:

1. **Token Management:**
   ```kotlin
   // AuthTokenManager handles JWT token storage and retrieval
   class AuthTokenManager @Inject constructor(
       private val dataStore: DataStore<Preferences>
   ) {
       // Store access token
       suspend fun storeAccessToken(token: String) {
           dataStore.edit { preferences ->
               preferences[PreferencesKeys.ACCESS_TOKEN] = token
           }
       }
       
       // Retrieve access token
       val accessToken: Flow<String?> = dataStore.data
           .map { preferences ->
               preferences[PreferencesKeys.ACCESS_TOKEN]
           }
   }
   ```

2. **Biometric Authentication:**
   ```kotlin
   // BiometricHelper handles biometric authentication
   class BiometricHelper(private val context: Context) {
       fun showBiometricPrompt(
           activity: FragmentActivity,
           onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
           onError: (Int, CharSequence) -> Unit
       ) {
           val promptInfo = BiometricPrompt.PromptInfo.Builder()
               .setTitle("Login with Biometric")
               .setSubtitle("Log in using your biometric credential")
               .setNegativeButtonText("Cancel")
               .build()
               
           val biometricPrompt = BiometricPrompt(
               activity,
               ContextCompat.getMainExecutor(context),
               object : BiometricPrompt.AuthenticationCallback() {
                   override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                       onSuccess(result)
                   }
                   
                   override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                       onError(errorCode, errString)
                   }
               }
           )
           
           biometricPrompt.authenticate(promptInfo)
       }
   }
   ```

### 3. Implementing Posture Analysis

The posture analysis module uses Google's ML Kit for pose detection. Key implementation steps include:

1. **Camera Setup:**
   ```kotlin
   // Set up CameraX and ML Kit
   private fun setupCamera() {
       val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
       cameraProviderFuture.addListener({
           val cameraProvider = cameraProviderFuture.get()
           
           val preview = Preview.Builder().build()
           val imageAnalyzer = ImageAnalysis.Builder()
               .setTargetResolution(Size(720, 1280))
               .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
               .build()
               
           imageAnalyzer.setAnalyzer(
               ContextCompat.getMainExecutor(context),
               PoseAnalyzer(poseDetector, onPoseDetected)
           )
           
           try {
               cameraProvider.unbindAll()
               cameraProvider.bindToLifecycle(
                   lifecycleOwner,
                   cameraSelector,
                   preview,
                   imageAnalyzer
               )
               preview.setSurfaceProvider(previewView.surfaceProvider)
           } catch (e: Exception) {
               Log.e("Camera", "Camera binding failed", e)
           }
       }, ContextCompat.getMainExecutor(context))
   }
   ```

2. **Pose Detection:**
   ```kotlin
   // Analyze camera frames for pose detection
   class PoseAnalyzer(
       private val poseDetector: PoseDetector,
       private val onPoseDetected: (Pose) -> Unit
   ) : ImageAnalysis.Analyzer {
       @ExperimentalGetImage
       override fun analyze(imageProxy: ImageProxy) {
           val mediaImage = imageProxy.image ?: run {
               imageProxy.close()
               return
           }
           
           val image = InputImage.fromMediaImage(
               mediaImage,
               imageProxy.imageInfo.rotationDegrees
           )
           
           poseDetector.process(image)
               .addOnSuccessListener { pose ->
                   onPoseDetected(pose)
               }
               .addOnFailureListener { e ->
                   Log.e("PoseAnalyzer", "Pose detection failed", e)
               }
               .addOnCompleteListener {
                   imageProxy.close()
               }
       }
   }
   ```

3. **Posture Analysis:**
   ```kotlin
   // Analyze pose landmarks for correct form
   class OnDeviceExerciseAnalyzer {
       fun analyzeSquatForm(pose: Pose): PostureFeedback {
           val landmarks = pose.allPoseLandmarks
           
           // Extract key landmarks
           val leftHip = landmarks.find { it.type == PoseLandmark.LEFT_HIP }
           val leftKnee = landmarks.find { it.type == PoseLandmark.LEFT_KNEE }
           val leftAnkle = landmarks.find { it.type == PoseLandmark.LEFT_ANKLE }
           
           // Calculate angles
           val kneeAngle = calculateAngle(leftHip, leftKnee, leftAnkle)
           
           // Determine if form is correct
           return when {
               kneeAngle < 90 -> PostureFeedback(
                   isCorrect = false,
                   message = "Knees too bent, try to maintain a 90-degree angle"
               )
               kneeAngle > 100 -> PostureFeedback(
                   isCorrect = false,
                   message = "Knees not bent enough"
               )
               else -> PostureFeedback(
                   isCorrect = true,
                   message = "Good form!"
               )
           }
       }
       
       private fun calculateAngle(
           first: PoseLandmark?,
           middle: PoseLandmark?,
           last: PoseLandmark?
       ): Double {
           if (first == null || middle == null || last == null) return 0.0
           
           // Calculate vectors
           val v1 = doubleArrayOf(first.position.x - middle.position.x, first.position.y - middle.position.y)
           val v2 = doubleArrayOf(last.position.x - middle.position.x, last.position.y - middle.position.y)
           
           // Calculate angle using dot product
           val dot = v1[0] * v2[0] + v1[1] * v2[1]
           val v1Mag = sqrt(v1[0] * v1[0] + v1[1] * v1[1])
           val v2Mag = sqrt(v2[0] * v2[0] + v2[1] * v2[1])
           
           val cos = dot / (v1Mag * v2Mag)
           return Math.toDegrees(acos(cos.coerceIn(-1.0, 1.0)))
       }
   }
   ```

### 4. Implementing Health Vitals Monitoring

The health vitals monitoring module uses Google's Health Connect API to access data from smartwatches. Key implementation steps include:

1. **Health Connect Setup:**
   ```kotlin
   // Initialize Health Connect
   class HealthConnectManager(private val context: Context) {
       private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }
       
       suspend fun hasPermissions(): Boolean {
           val grantedPermissions = healthConnectClient.permissionController
               .getGrantedPermissions()
           
           return PERMISSIONS.all { permission ->
               grantedPermissions.contains(permission)
           }
       }
       
       fun requestPermissions(activity: ComponentActivity, requestCode: Int) {
           val intent = healthConnectClient.permissionController
               .createRequestPermissionResultContract()
               .createIntent(activity, PERMISSIONS)
           
           activity.startActivityForResult(intent, requestCode)
       }
       
       suspend fun getLatestHeartRate(): Double? {
           val response = healthConnectClient.readRecords(
               ReadRecordsRequest(
                   recordType = HeartRateRecord::class,
                   timeRangeFilter = TimeRangeFilter.latest()
               )
           )
           
           return response.records.firstOrNull()?.samples?.firstOrNull()?.beatsPerMinute
       }
       
       companion object {
           val PERMISSIONS = setOf(
               Permission.createReadPermission(HeartRateRecord::class),
               Permission.createReadPermission(OxygenSaturationRecord::class),
               Permission.createReadPermission(StepsRecord::class)
           )
       }
   }
   ```

2. **Vitals Monitoring:**
   ```kotlin
   // Monitor vitals in real-time
   class VitalsViewModel @Inject constructor(
       private val healthConnectManager: HealthConnectManager,
       private val vitalsRepository: VitalsRepository
   ) : ViewModel() {
       private val _vitalsState = MutableStateFlow<VitalsState>(VitalsState.Loading)
       val vitalsState: StateFlow<VitalsState> = _vitalsState
       
       fun startMonitoring() {
           viewModelScope.launch {
               try {
                   if (!healthConnectManager.hasPermissions()) {
                       _vitalsState.value = VitalsState.NeedsPermission
                       return@launch
                   }
                   
                   while (true) {
                       val heartRate = healthConnectManager.getLatestHeartRate()
                       val oxygenSaturation = healthConnectManager.getLatestOxygenSaturation()
                       
                       val vitals = VitalSigns(
                           heartRate = heartRate,
                           oxygenSaturation = oxygenSaturation,
                           timestamp = System.currentTimeMillis()
                       )
                       
                       // Check for health risks
                       val alerts = checkHealthRisks(vitals)
                       
                       _vitalsState.value = VitalsState.Success(vitals, alerts)
                       
                       // Send vitals to backend
                       vitalsRepository.sendVitals(vitals)
                       
                       delay(1000) // Update every second
                   }
               } catch (e: Exception) {
                   _vitalsState.value = VitalsState.Error(e.message ?: "Unknown error")
               }
           }
       }
       
       private fun checkHealthRisks(vitals: VitalSigns): List<HealthRiskAlert> {
           val alerts = mutableListOf<HealthRiskAlert>()
           
           vitals.heartRate?.let { hr ->
               if (hr > VitalsThresholds.MAX_HEART_RATE) {
                   alerts.add(
                       HealthRiskAlert(
                           type = "heart_rate",
                           severity = "high",
                           message = "Heart rate is too high: $hr bpm"
                       )
                   )
               }
           }
           
           vitals.oxygenSaturation?.let { spo2 ->
               if (spo2 < VitalsThresholds.MIN_OXYGEN_SATURATION) {
                   alerts.add(
                       HealthRiskAlert(
                           type = "oxygen",
                           severity = "low",
                           message = "Oxygen saturation is low: $spo2%"
                       )
                   )
               }
           }
           
           return alerts
       }
   }
   ```

### 5. Implementing Task Management

The task management module allows students to view and complete assigned tasks. Key implementation steps include:

1. **Task List:**
   ```kotlin
   // Display list of tasks
   @Composable
   fun TaskListScreen(
       viewModel: TaskViewModel = hiltViewModel(),
       onTaskClick: (String) -> Unit
   ) {
       val tasks by viewModel.tasks.collectAsState()
       
       LazyColumn {
           items(tasks) { task ->
               TaskItem(
                   task = task,
                   onClick = { onTaskClick(task.physicalId) }
               )
           }
       }
   }
   
   @Composable
   fun TaskItem(task: Task, onClick: () -> Unit) {
       Card(
           modifier = Modifier
               .fillMaxWidth()
               .padding(8.dp)
               .clickable(onClick = onClick),
           elevation = CardDefaults.cardElevation(4.dp)
       ) {
           Column(
               modifier = Modifier.padding(16.dp)
           ) {
               Text(
                   text = task.name,
                   style = MaterialTheme.typography.titleMedium
               )
               Spacer(modifier = Modifier.height(4.dp))
               Text(
                   text = task.description,
                   style = MaterialTheme.typography.bodyMedium
               )
               Spacer(modifier = Modifier.height(8.dp))
               LinearProgressIndicator(
                   progress = { task.progress.toFloat() / 100f },
                   modifier = Modifier.fillMaxWidth()
               )
               Text(
                   text = "${task.progress}% Complete",
                   style = MaterialTheme.typography.bodySmall,
                   modifier = Modifier.align(Alignment.End)
               )
           }
       }
   }
   ```

2. **Task Detail and Completion:**
   ```kotlin
   // Display task details and completion options
   @Composable
   fun TaskDetailScreen(
       taskId: String,
       viewModel: TaskViewModel = hiltViewModel()
   ) {
       val taskState by viewModel.taskDetail.collectAsState()
       
       LaunchedEffect(taskId) {
           viewModel.getTaskDetail(taskId)
       }
       
       when (val state = taskState) {
           is TaskDetailState.Loading -> LoadingIndicator()
           is TaskDetailState.Error -> ErrorMessage(state.message)
           is TaskDetailState.Success -> {
               val task = state.task
               
               Column(
                   modifier = Modifier
                       .fillMaxSize()
                       .padding(16.dp)
               ) {
                   Text(
                       text = task.name,
                       style = MaterialTheme.typography.headlineMedium
                   )
                   Spacer(modifier = Modifier.height(8.dp))
                   Text(
                       text = task.description,
                       style = MaterialTheme.typography.bodyLarge
                   )
                   Spacer(modifier = Modifier.height(16.dp))
                   
                   // Task content based on type
                   when (task.type) {
                       TaskType.EXERCISE -> ExerciseTaskContent(task)
                       TaskType.QUIZ -> QuizTaskContent(task)
                       TaskType.LESSON -> LessonTaskContent(task)
                   }
                   
                   Spacer(modifier = Modifier.height(16.dp))
                   Button(
                       onClick = { viewModel.completeTask(taskId) },
                       modifier = Modifier.fillMaxWidth(),
                       enabled = !task.completed
                   ) {
                       Text("Complete Task")
                   }
               }
           }
       }
   }
   ```

### 6. Testing and Deployment

1. **Unit Testing:**
   ```kotlin
   @RunWith(AndroidJUnit4::class)
   class ExerciseAnalyzerTest {
       private lateinit var analyzer: OnDeviceExerciseAnalyzer
       
       @Before
       fun setup() {
           analyzer = OnDeviceExerciseAnalyzer()
       }
       
       @Test
       fun testSquatFormAnalysis() {
           // Create mock pose with landmarks
           val pose = mockPoseWithLandmarks()
           
           // Test analysis
           val feedback = analyzer.analyzeSquatForm(pose)
           
           // Assert results
           assertTrue(feedback.isCorrect)
           assertEquals("Good form!", feedback.message)
       }
       
       private fun mockPoseWithLandmarks(): Pose {
           // Create mock pose for testing
           // ...
       }
   }
   ```

2. **Integration Testing:**
   ```kotlin
   @RunWith(AndroidJUnit4::class)
   class VitalsMonitoringTest {
       @get:Rule
       val composeTestRule = createComposeRule()
       
       @Test
       fun testVitalsDisplay() {
           // Set up test environment
           composeTestRule.setContent {
               VitalsScreen(
                   vitalsState = VitalsState.Success(
                       vitals = VitalSigns(
                           heartRate = 75.0,
                           oxygenSaturation = 98.0,
                           timestamp = System.currentTimeMillis()
                       ),
                       alerts = emptyList()
                   )
               )
           }
           
           // Verify UI elements
           composeTestRule.onNodeWithText("Heart Rate").assertIsDisplayed()
           composeTestRule.onNodeWithText("75 BPM").assertIsDisplayed()
           composeTestRule.onNodeWithText("Oxygen Saturation").assertIsDisplayed()
           composeTestRule.onNodeWithText("98%").assertIsDisplayed()
       }
   }
   ```

3. **Deployment:**
   - Generate a signed APK or App Bundle
   - Test on various Android devices
   - Distribute through Google Play Store or enterprise distribution

## Conclusion

The STATHIS mobile application successfully implements the core features specified in the SRS document, providing a comprehensive solution for AI-powered posture tracking, health monitoring, and gamified learning in Physical Education. The application's modular architecture allows for easy maintenance and future enhancements.

## Future Enhancements

1. Support for additional smartwatch models beyond Xiaomi Smart Band 9 Active
2. Offline mode improvements for areas with limited connectivity
3. Enhanced gamification elements with more badges and achievements
4. Integration with additional exercise types and posture analysis models
5. Improved accessibility features for users with disabilities
