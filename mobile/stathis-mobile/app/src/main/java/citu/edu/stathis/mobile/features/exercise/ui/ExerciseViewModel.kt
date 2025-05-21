package citu.edu.stathis.mobile.features.exercise.ui

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import citu.edu.stathis.mobile.features.exercise.data.ExerciseDetector
import citu.edu.stathis.mobile.features.exercise.data.ExerciseResult
import citu.edu.stathis.mobile.features.exercise.data.ExerciseState
import citu.edu.stathis.mobile.features.exercise.data.ExerciseType
import citu.edu.stathis.mobile.features.posture.data.PostureDetector
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import javax.inject.Inject

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val TAG = "ExerciseViewModel"

    // Create executor for ML Kit operations
    private val mlExecutor = Executors.newSingleThreadExecutor()

    // Create pose detector
    private val postureDetector = PostureDetector(context, mlExecutor)

    // Create exercise detector
    private val exerciseDetector = ExerciseDetector()

    // State flows
    private val _exerciseState = MutableStateFlow<ExerciseUiState>(ExerciseUiState.Initial)
    val exerciseState: StateFlow<ExerciseUiState> = _exerciseState.asStateFlow()

    private val _cameraState = MutableStateFlow<CameraState>(CameraState.Inactive)
    val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()

    private val _exerciseStats = MutableStateFlow(ExerciseStats())
    val exerciseStats: StateFlow<ExerciseStats> = _exerciseStats.asStateFlow()

    // Selected exercise type
    private val _selectedExerciseType = MutableStateFlow<ExerciseType?>(null)
    val selectedExerciseType: StateFlow<ExerciseType?> = _selectedExerciseType.asStateFlow()

    // Track session time
    private var sessionStartTimeMs = 0L
    private var lastUpdateTimeMs = 0L

    /**
     * Process image from camera
     */
    @OptIn(ExperimentalGetImage::class)
    fun processImage(imageProxy: ImageProxy) {
        viewModelScope.launch {
            try {
                val mediaImage = imageProxy.image ?: return@launch
                val image = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.imageInfo.rotationDegrees
                )

                val pose = postureDetector.detectPose(image)

                // Process based on selected exercise type
                val selectedType = _selectedExerciseType.value ?: return@launch

                val result = when (selectedType) {
                    ExerciseType.SQUAT -> exerciseDetector.analyzeSquat(pose)
                    ExerciseType.PUSHUP -> exerciseDetector.analyzePushup(pose)
                }

                updateExerciseState(result)
                updateExerciseStats(result)
            } catch (e: Exception) {
                Log.e(TAG, "Error processing image", e)
                _exerciseState.value = ExerciseUiState.Error("Failed to analyze exercise: ${e.message}")
            } finally {
                imageProxy.close()
            }
        }
    }

    /**
     * Select exercise type
     */
    fun selectExerciseType(type: ExerciseType) {
        _selectedExerciseType.value = type
        exerciseDetector.resetExercise()
        _exerciseState.value = ExerciseUiState.Initial
        _exerciseStats.value = ExerciseStats()
    }

    /**
     * Start camera and exercise tracking
     */
    fun startExercise() {
        if (_selectedExerciseType.value == null) {
            _exerciseState.value = ExerciseUiState.Error("Please select an exercise type first")
            return
        }

        _cameraState.value = CameraState.Active
        sessionStartTimeMs = System.currentTimeMillis()
        lastUpdateTimeMs = sessionStartTimeMs
        _exerciseStats.value = ExerciseStats()
    }

    /**
     * Stop camera and exercise tracking
     */
    fun stopExercise() {
        _cameraState.value = CameraState.Inactive
        _exerciseState.value = ExerciseUiState.Initial
    }

    /**
     * Update exercise state based on detection result
     */
    private fun updateExerciseState(result: ExerciseResult) {
        _exerciseState.value = when {
            result.confidence < 0.7f -> ExerciseUiState.Detecting
            result.currentState == ExerciseState.INVALID -> ExerciseUiState.Invalid(result)
            else -> ExerciseUiState.Tracking(result)
        }
    }

    /**
     * Update exercise statistics
     */
    private fun updateExerciseStats(result: ExerciseResult) {
        val currentTime = System.currentTimeMillis()
        val timeDelta = currentTime - lastUpdateTimeMs
        lastUpdateTimeMs = currentTime

        val totalSessionTime = currentTime - sessionStartTimeMs

        _exerciseStats.value = ExerciseStats(
            sessionDurationMs = totalSessionTime,
            repCount = result.repCount,
            currentState = result.currentState,
            formIssues = result.formIssues
        )
    }

    override fun onCleared() {
        super.onCleared()
        postureDetector.close()
        mlExecutor.shutdown()
    }
}

sealed class ExerciseUiState {
    data object Initial : ExerciseUiState()
    data object Detecting : ExerciseUiState()
    data class Tracking(val result: ExerciseResult) : ExerciseUiState()
    data class Invalid(val result: ExerciseResult) : ExerciseUiState()
    data class Error(val message: String) : ExerciseUiState()
}

enum class CameraState {
    Active,
    Inactive
}

data class ExerciseStats(
    val sessionDurationMs: Long = 0,
    val repCount: Int = 0,
    val currentState: ExerciseState = ExerciseState.WAITING,
    val formIssues: List<String> = emptyList()
)