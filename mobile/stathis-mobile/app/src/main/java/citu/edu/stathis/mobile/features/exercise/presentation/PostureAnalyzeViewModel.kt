package citu.edu.stathis.mobile.features.exercise.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.exercise.domain.usecase.AnalyzePostureWithBackendUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostureAnalyzeViewModel @Inject constructor(
    private val analyzePostureWithBackend: AnalyzePostureWithBackendUseCase
) : ViewModel() {

    private val _status = MutableStateFlow<String?>(null)
    val status: StateFlow<String?> = _status

    fun clearStatus() { _status.value = null }

    fun analyze(landmarks: List<List<List<Float>>>) {
        viewModelScope.launch {
            _status.value = "Analyzing posture..."
            val result = analyzePostureWithBackend(landmarks)
            _status.value = when {
                result.success && result.data != null ->
                    "Exercise: ${result.data.exerciseName}, Score: ${(result.data.postureScore * 100).toInt()}%"
                else -> result.message ?: "Failed to analyze posture"
            }
        }
    }
}


