package citu.edu.stathis.mobile.features.exercise.ui

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.exercise.data.Exercise
import citu.edu.stathis.mobile.features.exercise.data.OnDeviceFeedback
import citu.edu.stathis.mobile.features.exercise.data.PoseLandmarksData
import citu.edu.stathis.mobile.features.exercise.data.analysis.OnDeviceExerciseAnalyzer
import citu.edu.stathis.mobile.features.exercise.data.model.BackendPostureAnalysis
import citu.edu.stathis.mobile.features.exercise.data.posedetection.PoseDetectionService
import citu.edu.stathis.mobile.features.exercise.data.toPoseLandmarksData
import citu.edu.stathis.mobile.features.exercise.domain.usecase.*
import com.google.mlkit.vision.pose.Pose
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

sealed class ExerciseScreenUiState {
    data object Initial : ExerciseScreenUiState()
    data object PermissionNeeded : ExerciseScreenUiState()
    data class ExerciseSelection(val exercises: List<Exercise>, val isLoading: Boolean = false) : ExerciseScreenUiState()
    data class ExerciseIntroduction(val exercise: Exercise) : ExerciseScreenUiState()
    data class ExerciseActive(
        val selectedExercise: Exercise,
        val currentPoseLandmarks: PoseLandmarksData? = null,
        val onDeviceFeedback: OnDeviceFeedback? = null,
        val backendAnalysis: BackendPostureAnalysis? = null,
        val sessionTimerMs: Long = 0L,
        val repCount: Int = 0,
        val currentCameraSelector: CameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
    ) : ExerciseScreenUiState()
    data class ExerciseSummary(val message: String) : ExerciseScreenUiState() // Simplified for now
    data class Error(val message: String) : ExerciseScreenUiState()
}

sealed class ExerciseViewEvent {
    data class ShowSnackbar(val message: String) : ExerciseViewEvent()
    data object NavigateToExerciseSelection : ExerciseViewEvent()
}

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val poseDetectionService: PoseDetectionService,
    private val onDeviceExerciseAnalyzer: OnDeviceExerciseAnalyzer,
    private val getAvailableExercisesUseCase: GetAvailableExercisesUseCase,
    private val analyzePostureWithBackendUseCase: AnalyzePostureWithBackendUseCase,
    private val saveExerciseSessionUseCase: SaveExerciseSessionUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExerciseScreenUiState>(ExerciseScreenUiState.Initial)
    val uiState: StateFlow<ExerciseScreenUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ExerciseViewEvent>()
    val events: SharedFlow<ExerciseViewEvent> = _events.asSharedFlow()

    private var currentUserId: String? = null
    private var sessionJob: Job? = null
    private var sessionStartTime: Long = 0L
    private var lastBackendAnalysisTime: Long = 0L
    private val backendAnalysisIntervalMs = 2000L // e.g., analyze with backend every 2 seconds

    init {
        viewModelScope.launch {
            currentUserId = getCurrentUserIdUseCase()
            if (currentUserId == null) {
                _uiState.value = ExerciseScreenUiState.Error("User not identified. Please log in.")
                _events.emit(ExerciseViewEvent.ShowSnackbar("User ID not found."))
            }
        }
    }

    fun onCameraPermissionGranted() {
        loadExercises()
    }

    fun onCameraPermissionDenied() {
        _uiState.value = ExerciseScreenUiState.Error("Camera permission is required for this feature.")
    }

    fun loadExercises() {
        viewModelScope.launch {
            _uiState.value = ExerciseScreenUiState.ExerciseSelection(emptyList(), isLoading = true)
            when (val result = getAvailableExercisesUseCase()) {
                is ClientResponse -> {
                    if (result.success) {
                        _uiState.value = ExerciseScreenUiState.ExerciseSelection(result.data ?: emptyList(), isLoading = false)
                    } else {
                        _uiState.value = ExerciseScreenUiState.Error(result.message)
                    }
                }
            }
        }
    }

    fun selectExercise(exercise: Exercise) {
        _uiState.value = ExerciseScreenUiState.ExerciseIntroduction(exercise)
        onDeviceExerciseAnalyzer.resetExerciseState()
    }

    fun startExerciseSession(exercise: Exercise) {
        if (currentUserId == null) {
            viewModelScope.launch { _events.emit(ExerciseViewEvent.ShowSnackbar("User not identified.")) }
            return
        }
        sessionStartTime = System.currentTimeMillis()
        lastBackendAnalysisTime = 0L
        _uiState.value = ExerciseScreenUiState.ExerciseActive(
            selectedExercise = exercise,
            repCount = 0,
            sessionTimerMs = 0L
        )
    }

    fun processImageProxy(imageProxy: ImageProxy, selectedExercise: Exercise) {
        if (currentUserId == null || _uiState.value !is ExerciseScreenUiState.ExerciseActive) {
            imageProxy.close()
            return
        }

        sessionJob = viewModelScope.launch {
            val pose: Pose? = poseDetectionService.processImageProxy(imageProxy)
            pose?.let { mlKitPose ->
                val landmarksData = mlKitPose.toPoseLandmarksData()
                val onDeviceFeedbackResult = onDeviceExerciseAnalyzer.analyzePose(mlKitPose, selectedExercise.type)

                val currentActiveState = _uiState.value as? ExerciseScreenUiState.ExerciseActive
                val newRepCount = onDeviceFeedbackResult
                val sessionTimer = System.currentTimeMillis() - sessionStartTime

                var backendAnalysisResult = currentActiveState?.backendAnalysis
                if (System.currentTimeMillis() - lastBackendAnalysisTime > backendAnalysisIntervalMs) {
                    lastBackendAnalysisTime = System.currentTimeMillis()
                    val backendResponse = analyzePostureWithBackendUseCase(landmarksData.toFloatArrayForBackend())
                    if (backendResponse.success && backendResponse.data != null) {
                        backendAnalysisResult = BackendPostureAnalysis(
                            identifiedExercise = backendResponse.data.exerciseName,
                            postureScore = backendResponse.data.postureScore
                        )
                    } else {
                        _events.emit(ExerciseViewEvent.ShowSnackbar("Backend analysis failed: ${backendResponse.message}"))
                    }
                }

                _uiState.value = ExerciseScreenUiState.ExerciseActive(
                    selectedExercise = selectedExercise,
                    currentPoseLandmarks = landmarksData,
                    onDeviceFeedback = onDeviceFeedbackResult,
                    backendAnalysis = backendAnalysisResult,
                    sessionTimerMs = sessionTimer,
                    repCount = newRepCount.repCount
                )
            }
        }
    }

    fun toggleCamera(currentSelector: CameraSelector) {
        val currentUiState = _uiState.value
        if (currentUiState is ExerciseScreenUiState.ExerciseActive) {
            val newSelector = if (currentSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                CameraSelector.DEFAULT_FRONT_CAMERA
            }
            _uiState.value = currentUiState.copy(currentCameraSelector = newSelector)
        }
    }

    fun stopExerciseSession() {
        sessionJob?.cancel()
        val activeState = _uiState.value
        if (activeState is ExerciseScreenUiState.ExerciseActive && currentUserId != null) {
            viewModelScope.launch {
                saveExerciseSessionUseCase(
                    userId = currentUserId!!,
                    exercise = activeState.selectedExercise,
                    startTime = LocalDateTime.now().minusSeconds(activeState.sessionTimerMs / 1000),
                    endTime = LocalDateTime.now(),
                    durationMs = activeState.sessionTimerMs,
                    repCount = activeState.repCount,
                    averageAccuracy = activeState.backendAnalysis?.postureScore,
                    formIssues = activeState.onDeviceFeedback?.formIssues
                )
                _uiState.value = ExerciseScreenUiState.ExerciseSummary("Session Ended. Reps: ${activeState.repCount}")
                _events.emit(ExerciseViewEvent.NavigateToExerciseSelection)
            }
        } else {
            _uiState.value = ExerciseScreenUiState.ExerciseSelection(emptyList(), isLoading = false)
            loadExercises()
        }
        onDeviceExerciseAnalyzer.resetExerciseState()
    }


    override fun onCleared() {
        super.onCleared()
        poseDetectionService.close()
        sessionJob?.cancel()
    }
}