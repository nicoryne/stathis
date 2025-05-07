package citu.edu.stathis.mobile.features.posture.ui

import android.content.Context
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import citu.edu.stathis.mobile.features.posture.data.PostureDetector
import citu.edu.stathis.mobile.features.posture.data.PostureResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import javax.inject.Inject

@HiltViewModel
class PostureViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val TAG = "PostureViewModel"

    // Create executor for ML Kit operations
    private val mlExecutor = Executors.newSingleThreadExecutor()

    // Create pose detector
    private val postureDetector = PostureDetector(context, mlExecutor)

    // State flows
    private val _postureState = MutableStateFlow<PostureState>(PostureState.Initial)
    val postureState: StateFlow<PostureState> = _postureState.asStateFlow()

    private val _cameraState = MutableStateFlow<CameraState>(CameraState.Inactive)
    val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()

    private val _postureStats = MutableStateFlow(PostureStats())
    val postureStats: StateFlow<PostureStats> = _postureStats.asStateFlow()

    // Track session time
    private var sessionStartTimeMs = 0L
    private var goodPostureTimeMs = 0L
    private var badPostureTimeMs = 0L
    private var lastUpdateTimeMs = 0L

    /**
     * Process image from camera
     */
    fun processImage(imageProxy: ImageProxy) {
        viewModelScope.launch {
            try {
                val result = postureDetector.processImageProxy(imageProxy)
                if (result != null) {
                    updatePostureState(result)
                    updatePostureStats(result)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing image", e)
                _postureState.value = PostureState.Error("Failed to analyze posture: ${e.message}")
            }
        }
    }

    /**
     * Start camera
     */
    fun startCamera() {
        _cameraState.value = CameraState.Active
        sessionStartTimeMs = System.currentTimeMillis()
        lastUpdateTimeMs = sessionStartTimeMs
        goodPostureTimeMs = 0
        badPostureTimeMs = 0
        _postureStats.value = PostureStats()
    }

    /**
     * Stop camera
     */
    fun stopCamera() {
        _cameraState.value = CameraState.Inactive
        _postureState.value = PostureState.Initial
    }

    /**
     * Update posture state based on detection result
     */
    private fun updatePostureState(result: PostureResult) {
        _postureState.value = if (result.confidence > 0.7f) {
            if (result.isGoodPosture) {
                PostureState.Good(result)
            } else {
                PostureState.Bad(result)
            }
        } else {
            PostureState.Detecting
        }
    }

    /**
     * Update posture statistics
     */
    private fun updatePostureStats(result: PostureResult) {
        val currentTime = System.currentTimeMillis()
        val timeDelta = currentTime - lastUpdateTimeMs

        if (result.confidence > 0.7f) {
            if (result.isGoodPosture) {
                goodPostureTimeMs += timeDelta
            } else {
                badPostureTimeMs += timeDelta
            }
        }

        lastUpdateTimeMs = currentTime

        val totalSessionTime = currentTime - sessionStartTimeMs
        val goodPosturePercentage = if (totalSessionTime > 0) {
            (goodPostureTimeMs * 100 / totalSessionTime).toInt()
        } else {
            0
        }

        _postureStats.value = PostureStats(
            sessionDurationMs = totalSessionTime,
            goodPostureTimeMs = goodPostureTimeMs,
            badPostureTimeMs = badPostureTimeMs,
            goodPosturePercentage = goodPosturePercentage
        )
    }

    override fun onCleared() {
        super.onCleared()
        postureDetector.close()
        mlExecutor.shutdown()
    }
}

sealed class PostureState {
    data object Initial : PostureState()
    data object Detecting : PostureState()
    data class Good(val result: PostureResult) : PostureState()
    data class Bad(val result: PostureResult) : PostureState()
    data class Error(val message: String) : PostureState()
}

enum class CameraState {
    Active,
    Inactive
}

data class PostureStats(
    val sessionDurationMs: Long = 0,
    val goodPostureTimeMs: Long = 0,
    val badPostureTimeMs: Long = 0,
    val goodPosturePercentage: Int = 0
)