package citu.edu.stathis.mobile.features.progress.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.progress.data.api.LeaderboardEntry
import citu.edu.stathis.mobile.features.progress.data.model.Achievement
import citu.edu.stathis.mobile.features.progress.data.model.Badge
import citu.edu.stathis.mobile.features.progress.data.model.ProgressActivity
import citu.edu.stathis.mobile.features.progress.data.model.StudentProgress
import citu.edu.stathis.mobile.features.progress.domain.usecase.GetStudentProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the student progress tracking feature
 */
@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val getStudentProgressUseCase: GetStudentProgressUseCase
) : ViewModel() {

    // UI state for student progress
    private val _progressState = MutableStateFlow<ProgressState>(ProgressState.Loading)
    val progressState: StateFlow<ProgressState> = _progressState.asStateFlow()
    
    // UI state for achievements
    private val _achievementsState = MutableStateFlow<List<Achievement>>(emptyList())
    val achievementsState: StateFlow<List<Achievement>> = _achievementsState.asStateFlow()
    
    // UI state for badges
    private val _badgesState = MutableStateFlow<List<Badge>>(emptyList())
    val badgesState: StateFlow<List<Badge>> = _badgesState.asStateFlow()
    
    // UI state for recent activities
    private val _activitiesState = MutableStateFlow<List<ProgressActivity>>(emptyList())
    val activitiesState: StateFlow<List<ProgressActivity>> = _activitiesState.asStateFlow()
    
    // UI state for leaderboard
    private val _leaderboardState = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboardState: StateFlow<List<LeaderboardEntry>> = _leaderboardState.asStateFlow()
    
    /**
     * Loads the student's overall progress
     */
    fun loadStudentProgress() {
        viewModelScope.launch {
            _progressState.value = ProgressState.Loading
            
            try {
                getStudentProgressUseCase()
                    .catch { e ->
                        Timber.e(e, "Error loading student progress")
                        _progressState.value = ProgressState.Error(e.message ?: "Unknown error")
                    }
                    .collectLatest { response ->
                        if (response.success && response.data != null) {
                            val progress = response.data
                            _progressState.value = ProgressState.Success(progress)
                            _achievementsState.value = progress.achievements ?: emptyList()
                            _badgesState.value = progress.badges ?: emptyList()
                            _activitiesState.value = progress.recentActivities ?: emptyList()
                        } else {
                            _progressState.value = ProgressState.Error(response.message)
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error loading student progress")
                _progressState.value = ProgressState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Refreshes all progress data
     */
    fun refreshProgress() {
        loadStudentProgress()
    }
}

/**
 * Sealed class representing the different states of the progress UI
 */
sealed class ProgressState {
    object Loading : ProgressState()
    data class Success(val progress: StudentProgress) : ProgressState()
    data class Error(val message: String) : ProgressState()
}
