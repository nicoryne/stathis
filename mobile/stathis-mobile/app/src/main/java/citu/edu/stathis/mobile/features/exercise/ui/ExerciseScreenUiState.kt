package citu.edu.stathis.mobile.features.exercise.ui

import androidx.camera.core.CameraSelector
import citu.edu.stathis.mobile.features.exercise.data.Exercise
import citu.edu.stathis.mobile.features.exercise.data.PoseLandmarksData
import citu.edu.stathis.mobile.features.exercise.data.model.BackendPostureAnalysis

/**
 * UI state for the Exercise Screen.
 */
sealed class ExerciseScreenUiState {
    /**
     * Initial state before any data is loaded
     */
    object Initial : ExerciseScreenUiState()
    
    /**
     * State indicating that camera permission is needed
     */
    object PermissionNeeded : ExerciseScreenUiState()
    
    /**
     * State for exercise selection
     */
    data class ExerciseSelection(
        val isLoading: Boolean = false,
        val exercises: List<Exercise> = emptyList()
    ) : ExerciseScreenUiState()
    
    /**
     * State for exercise introduction screen
     */
    data class ExerciseIntroduction(
        val exercise: Exercise
    ) : ExerciseScreenUiState()
    
    /**
     * State for active exercise screen
     */
    data class ExerciseActive(
        val selectedExercise: Exercise,
        val currentCameraSelector: CameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA,
        val currentPoseLandmarks: PoseLandmarksData? = null,
        val repCount: Int = 0,
        val heartRate: Int = 0,
        val backendAnalysis: BackendPostureAnalysis? = null,
        val poseDetectionStatus: PoseDetectionStatus = PoseDetectionStatus.INITIALIZING,
        val sessionTimerMs: Long = 0L
    ) : ExerciseScreenUiState()
    
    /**
     * State for exercise summary screen
     */
    data class ExerciseSummary(
        val message: String
    ) : ExerciseScreenUiState()
    
    /**
     * State for error screen
     */
    data class Error(
        val message: String
    ) : ExerciseScreenUiState()
    
    /**
     * Enum representing the status of pose detection
     */
    enum class PoseDetectionStatus {
        INITIALIZING,
        ACTIVE,
        ERROR
    }
}
