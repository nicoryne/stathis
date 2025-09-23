package citu.edu.stathis.mobile.features.exercise.ui

import androidx.camera.core.CameraSelector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import citu.edu.stathis.mobile.features.exercise.data.Exercise
import citu.edu.stathis.mobile.features.exercise.data.LandmarkPoint
import citu.edu.stathis.mobile.features.exercise.data.PoseLandmarksData
import citu.edu.stathis.mobile.features.exercise.data.model.BackendPostureAnalysis
import citu.edu.stathis.mobile.features.exercise.data.posedetection.PoseDetectionService
import citu.edu.stathis.mobile.features.exercise.data.toPoseLandmarksData
import citu.edu.stathis.mobile.features.exercise.domain.usecase.*
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.concurrent.Executor
import javax.inject.Inject
import kotlin.math.abs


@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val poseDetectionService: PoseDetectionService,
    private val getAvailableExercisesUseCase: GetAvailableExercisesUseCase,
    private val analyzePostureWithBackendUseCase: AnalyzePostureWithBackendUseCase,
    private val saveExerciseSessionUseCase: SaveExerciseSessionUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExerciseScreenUiState>(ExerciseScreenUiState.Initial)
    val uiState: StateFlow<ExerciseScreenUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ExerciseViewEvent>()
    val events: SharedFlow<ExerciseViewEvent> = _events.asSharedFlow()

    private var sessionStartTime: Long = 0L
    private var lastBackendAnalysisTime: Long = 0L
    private var lastFrameAnalysisTime: Long = 0L
    private val backendAnalysisIntervalMs = 2000L // Every 2 seconds
    private val frameAnalysisIntervalMs = 33L // ~30fps
    private var selectedExercise: Exercise? = null
    private var currentRepCount: Int = 0
    private var lastPostureScore: Float = 0f
    private val accumulatedPostureScores = mutableListOf<Float>()
    private var lastProcessedPose: PoseLandmarksData? = null
    
    // Cache for landmark smoothing
    private val landmarkBuffer = Array(4) { mutableListOf<LandmarkPoint>() }
    private var bufferIndex = 0

    fun onCameraPermissionGranted() {
        viewModelScope.launch {
            loadExercises()
        }
    }

    fun onCameraPermissionDenied() {
        _uiState.value = ExerciseScreenUiState.PermissionNeeded
    }

    fun loadExercises() {
        viewModelScope.launch {
            _uiState.value = ExerciseScreenUiState.ExerciseSelection(isLoading = true, exercises = emptyList())
            val response = getAvailableExercisesUseCase()
            if (response.success && response.data != null) {
                _uiState.value = ExerciseScreenUiState.ExerciseSelection(isLoading = false, exercises = response.data)
            } else {
                _uiState.value = ExerciseScreenUiState.Error(response.message ?: "Failed to load exercises")
            }
        }
    }

    fun selectExercise(exercise: Exercise) {
        selectedExercise = exercise
        _uiState.value = ExerciseScreenUiState.ExerciseIntroduction(exercise)
    }

    fun startExerciseSession(exercise: Exercise) {
        selectedExercise = exercise
        sessionStartTime = System.currentTimeMillis()
        lastBackendAnalysisTime = 0L
        currentRepCount = 0
        accumulatedPostureScores.clear()
        _uiState.value = ExerciseScreenUiState.ExerciseActive(
            selectedExercise = exercise,
            sessionTimerMs = 0L
        )
    }
    
    /**
     * Update the pose data used for rendering the skeleton overlay
     */
    fun updatePoseForRendering(pose: Pose, imageWidth: Int, imageHeight: Int, isFlipped: Boolean) {
        // This functionality is handled directly in the UI
        // No state update needed for rendering data
    }

    fun onPoseDetected(pose: Pose, exercise: Exercise) {
        val currentTime = System.currentTimeMillis()
        
        // Frame rate control - skip processing if too soon
        if (currentTime - lastFrameAnalysisTime < frameAnalysisIntervalMs) {
            return
        }
        
        lastFrameAnalysisTime = currentTime
        viewModelScope.launch(Dispatchers.Default) {
            // Convert ML Kit pose to our data structure
            val landmarksData = pose.toPoseLandmarksData()
            
            // Skip processing if no significant change
            if (lastProcessedPose != null && !hasSignificantPoseChange(lastProcessedPose!!, landmarksData)) {
                return@launch
            }
            
            // Apply advanced smoothing using multiple frames
            val smoothedLandmarks = smoothLandmarksAdvanced(landmarksData)
            lastProcessedPose = smoothedLandmarks

            val sessionTimer = System.currentTimeMillis() - sessionStartTime

            // Update UI with landmarks immediately
            withContext(Dispatchers.Main) {
                updateExerciseState(
                    exercise = exercise,
                    landmarksData = smoothedLandmarks,
                    backendAnalysis = (_uiState.value as? ExerciseScreenUiState.ExerciseActive)?.backendAnalysis,
                    sessionTimer = sessionTimer
                )
            }

            // Perform backend analysis at intervals
            if (currentTime - lastBackendAnalysisTime > backendAnalysisIntervalMs) {
                lastBackendAnalysisTime = currentTime
                performBackendAnalysis(smoothedLandmarks, exercise)
            }
        }
    }

    private fun hasSignificantPoseChange(
        oldLandmarks: PoseLandmarksData,
        newLandmarks: PoseLandmarksData,
        threshold: Float = 0.02f
    ): Boolean {
        // Check key points for significant movement
        val keyPoints = listOf(
            PoseLandmark.LEFT_SHOULDER,
            PoseLandmark.RIGHT_SHOULDER,
            PoseLandmark.LEFT_ELBOW,
            PoseLandmark.RIGHT_ELBOW,
            PoseLandmark.LEFT_HIP,
            PoseLandmark.RIGHT_HIP,
            PoseLandmark.LEFT_KNEE,
            PoseLandmark.RIGHT_KNEE
        )

        return keyPoints.any { landmarkType ->
            val oldPoint = oldLandmarks.landmarkPoints.find { it.type == landmarkType }
            val newPoint = newLandmarks.landmarkPoints.find { it.type == landmarkType }
            
            if (oldPoint != null && newPoint != null) {
                abs(newPoint.x - oldPoint.x) > threshold ||
                abs(newPoint.y - oldPoint.y) > threshold ||
                abs(newPoint.z - oldPoint.z) > threshold
            } else {
                true
            }
        }
    }

    private fun smoothLandmarksAdvanced(current: PoseLandmarksData): PoseLandmarksData {
        // Update circular buffer
        landmarkBuffer[bufferIndex] = current.landmarkPoints.toMutableList()
        bufferIndex = (bufferIndex + 1) % landmarkBuffer.size

        // Apply weighted moving average
        val smoothedPoints = current.landmarkPoints.mapIndexed { index, currentPoint ->
            var sumX = 0f
            var sumY = 0f
            var sumZ = 0f
            var totalWeight = 0f

            landmarkBuffer.forEachIndexed { bufferIdx, points ->
                if (points.isNotEmpty()) {
                    val weight = when (bufferIdx) {
                        bufferIndex -> 0.4f // Current frame
                        (bufferIndex - 1).mod(landmarkBuffer.size) -> 0.3f // Previous frame
                        (bufferIndex - 2).mod(landmarkBuffer.size) -> 0.2f // 2 frames ago
                        else -> 0.1f // 3 frames ago
                    }
                    
                    points.getOrNull(index)?.let { point ->
                        if (point.inFrameLikelihood > 0.5f) {
                            sumX += point.x * weight
                            sumY += point.y * weight
                            sumZ += point.z * weight
                            totalWeight += weight
                        }
                    }
                }
            }

            if (totalWeight > 0) {
                currentPoint.copy(
                    x = sumX / totalWeight,
                    y = sumY / totalWeight,
                    z = sumZ / totalWeight
                )
            } else {
                currentPoint
            }
        }

        return current.copy(landmarkPoints = smoothedPoints)
    }

    private fun updateExerciseState(
        exercise: Exercise,
        landmarksData: PoseLandmarksData,
        backendAnalysis: BackendPostureAnalysis?,
        sessionTimer: Long
    ) {
        (_uiState.value as? ExerciseScreenUiState.ExerciseActive)?.let { currentState ->
            // Only update if there are significant changes
            if (shouldUpdateState(currentState, landmarksData, backendAnalysis, sessionTimer)) {
                _uiState.value = ExerciseScreenUiState.ExerciseActive(
                    selectedExercise = exercise,
                    currentPoseLandmarks = landmarksData,
                    backendAnalysis = backendAnalysis,
                    sessionTimerMs = sessionTimer,
                    repCount = currentRepCount,
                    currentCameraSelector = currentState.currentCameraSelector
                )
            }
        }
    }

    private fun shouldUpdateState(
        currentState: ExerciseScreenUiState.ExerciseActive,
        newLandmarks: PoseLandmarksData,
        newAnalysis: BackendPostureAnalysis?,
        newTimer: Long
    ): Boolean {
        // Check if there are significant changes to avoid unnecessary updates
        return currentState.currentPoseLandmarks == null ||
               currentState.backendAnalysis != newAnalysis ||
               abs(currentState.sessionTimerMs - newTimer) >= 1000 || // Update timer every second
               hasSignificantPoseChange(currentState.currentPoseLandmarks, newLandmarks)
    }

    private suspend fun performBackendAnalysis(landmarksData: PoseLandmarksData, exercise: Exercise) {
        val backendResponse = analyzePostureWithBackendUseCase(landmarksData.toFloatArrayForBackend())
        if (backendResponse.success && backendResponse.data != null) {
            val backendAnalysis = BackendPostureAnalysis(
                identifiedExercise = backendResponse.data.exerciseName,
                postureScore = backendResponse.data.postureScore
            )
            
            if (backendResponse.data.exerciseName.equals(exercise.name, ignoreCase = true)) {
                lastPostureScore = backendResponse.data.postureScore
                accumulatedPostureScores.add(lastPostureScore)
                
                if (lastPostureScore > 0.7f) {
                    if ((_uiState.value as? ExerciseScreenUiState.ExerciseActive)?.repCount ?: 0 < currentRepCount + 1) {
                        currentRepCount++
                    }
                }
            }

            // Update UI with new backend analysis
            (_uiState.value as? ExerciseScreenUiState.ExerciseActive)?.let { currentState ->
                updateExerciseState(
                    exercise = exercise,
                    landmarksData = currentState.currentPoseLandmarks ?: return,
                    backendAnalysis = backendAnalysis,
                    sessionTimer = currentState.sessionTimerMs
                )
            }
        } else {
            _events.emit(ExerciseViewEvent.ShowSnackbar("Backend analysis failed: ${backendResponse.message}"))
        }
    }

    fun stopExerciseSession() {
        viewModelScope.launch {
            val currentState = _uiState.value as? ExerciseScreenUiState.ExerciseActive ?: return@launch
            val exercise = selectedExercise ?: return@launch
            val userId = getCurrentUserIdUseCase() ?: return@launch

            val endTime = LocalDateTime.now()
            val startTime = endTime.minusNanos(currentState.sessionTimerMs * 1_000_000) // Convert ms to nanos
            val averageAccuracy = accumulatedPostureScores.average().toFloat()

            val saveResponse = saveExerciseSessionUseCase(
                userId = userId,
                exercise = exercise,
                startTime = startTime,
                endTime = endTime,
                durationMs = currentState.sessionTimerMs,
                repCount = currentState.repCount,
                averageAccuracy = averageAccuracy,
                formIssues = null
            )

            if (saveResponse.success) {
                _uiState.value = ExerciseScreenUiState.ExerciseSummary(
                    "Great job! You completed ${currentState.repCount} reps with ${(averageAccuracy * 100).toInt()}% accuracy."
                )
            } else {
                _events.emit(ExerciseViewEvent.ShowSnackbar("Failed to save session: ${saveResponse.message}"))
                loadExercises()
            }
        }
    }

    fun toggleCamera(currentSelector: CameraSelector) {
        val newSelector = if (currentSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }
        
        (_uiState.value as? ExerciseScreenUiState.ExerciseActive)?.let { currentState ->
            _uiState.value = currentState.copy(
                currentCameraSelector = newSelector
            )
        }
    }


    override fun onCleared() {
        super.onCleared()
        // Clear buffers and caches
        landmarkBuffer.forEach { it.clear() }
        accumulatedPostureScores.clear()
        lastProcessedPose = null
        poseDetectionService.close()
    }
}