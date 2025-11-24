package citu.edu.stathis.mobile.features.exercise.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import citu.edu.stathis.mobile.features.exercise.domain.usecase.ClassifyPoseUseCase
import citu.edu.stathis.mobile.features.exercise.ui.util.Landmark
import citu.edu.stathis.mobile.features.exercise.ui.util.normalizeFrame
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val classifyPose: ClassifyPoseUseCase
) : ViewModel() {

    private val T = 45 // Updated to match new ONNX model requirement (was 30)
    private val window: ArrayDeque<FloatArray> = ArrayDeque(T)
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private var lastSentMs = 0L
    private val sendIntervalMs = 300L

    fun onFrame(landmarks: List<Landmark>) {
        if (landmarks.size != 33) return
        val vec = normalizeFrame(landmarks) // 132
        if (window.size == T) window.removeFirst()
        window.addLast(vec)

        val now = System.currentTimeMillis()
        if (window.size == T && now - lastSentMs >= sendIntervalMs) {
            lastSentMs = now
            val payload = arrayOf(Array(T) { idx -> window.elementAt(idx) })
            viewModelScope.launch(Dispatchers.IO) {
                runCatching { classifyPose(payload) }
                    .onSuccess { r ->
                        _uiState.update {
                            it.copy(
                                predictedClass = r.predictedClass,
                                score = r.score,
                                probabilities = r.probabilities,
                                classNames = r.classNames,
                                formConfidence = r.formConfidence,
                                flags = r.flags ?: emptyList(),
                                messages = r.messages ?: emptyList()
                            )
                        }
                    }
            }
        }
    }

    data class UiState(
        val predictedClass: String = "",
        val score: Float = 0f,
        val probabilities: List<Float> = emptyList(),
        val classNames: List<String> = emptyList(),
        val formConfidence: Float? = null, // Form quality: 0.0-0.4 (poor), 0.5-0.7 (moderate), 0.8-1.0 (good)
        val flags: List<String> = emptyList(),
        val messages: List<String> = emptyList()
    )
}


