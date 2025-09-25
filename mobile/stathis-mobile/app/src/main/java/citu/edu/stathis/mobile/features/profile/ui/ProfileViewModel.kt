package citu.edu.stathis.mobile.features.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.auth.data.models.UserResponseDTO
import citu.edu.stathis.mobile.core.data.AuthTokenManager
import citu.edu.stathis.mobile.features.classroom.domain.usecase.GetStudentClassroomsUseCase
import citu.edu.stathis.mobile.features.progress.domain.repository.ProgressRepository
import citu.edu.stathis.mobile.features.progress.data.api.ProgressService
import citu.edu.stathis.mobile.features.profile.data.repository.ProfileRepository
import citu.edu.stathis.mobile.features.auth.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val getStudentClassroomsUseCase: GetStudentClassroomsUseCase,
    private val progressRepository: ProgressRepository,
    private val progressService: ProgressService,
    private val authRepository: AuthRepository,
    private val authTokenManager: AuthTokenManager
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val errorMessage: String? = null,
        val profile: UserResponseDTO? = null,
        val classroomCount: Int = 0,
        val achievementsCount: Int = 0,
        val certificatesCount: Int = 0,
        val recentActivities: List<String> = emptyList()
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    init {
        refresh()
        // Observe token changes to auto-refresh profile when logging in/out
        viewModelScope.launch {
            authTokenManager.accessTokenFlow.collect { _ ->
                // Any token change should re-fetch profile so UI reflects latest auth state
                refresh()
            }
        }
        // Also observe isLoggedIn, in case tokens are same but flag changes
        viewModelScope.launch {
            authTokenManager.isLoggedInFlow.collect { _ ->
                refresh()
            }
        }
    }

    fun refresh() {
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            // Fetch profile
            val profileResp: ClientResponse<UserResponseDTO> = profileRepository.getUserProfile()
            if (profileResp.success && profileResp.data != null) {
                _state.value = _state.value.copy(profile = profileResp.data, isLoading = false)
            } else {
                _state.value = _state.value.copy(isLoading = false, errorMessage = profileResp.message ?: "Failed to load profile")
            }

            // Fetch classrooms count
            try {
                getStudentClassroomsUseCase()
                    .catch { _ -> }
                    .collect { list ->
                        _state.value = _state.value.copy(classroomCount = list.size)
                    }
            } catch (_: Exception) {
                // ignore classroom errors for now
            }

            // Fetch badges by student; map counts (certificates by badgeType = "CERTIFICATE")
            try {
                val physicalId = profileResp.data?.physicalId
                if (!physicalId.isNullOrBlank()) {
                    val badgesResp = progressService.getBadgesByStudent(studentId = physicalId)
                    if (badgesResp.isSuccessful) {
                        val badges = badgesResp.body().orEmpty()
                        val achievementsCount = badges.size
                        val certificatesCount = badges.count { (it.badgeType ?: "").equals("CERTIFICATE", ignoreCase = true) }
                        val recentActivities = badges.sortedByDescending { it.earnedAt ?: "" }
                            .take(3)
                            .map { it.description ?: "Earned a badge" }
                        _state.value = _state.value.copy(
                            achievementsCount = achievementsCount,
                            certificatesCount = certificatesCount,
                            recentActivities = recentActivities
                        )
                    }

                    val lbResp = progressService.getLeaderboardByStudent(studentId = physicalId)
                    if (lbResp.isSuccessful) {
                        // We only need to know empty vs non-empty to show empty trophy card; counts not displayed here
                        val anyTrophies = lbResp.body().orEmpty().isNotEmpty()
                        if (!anyTrophies && _state.value.recentActivities.isEmpty()) {
                            // keep the empty state message we already render
                        }
                    }
                }
            } catch (_: Exception) {}

            // Recent activities (names only) - fallback/source of truth if available
            try {
                progressRepository.getRecentActivities(limit = 3, offset = 0, type = null)
                    .catch { _ -> }
                    .collect { resp ->
                        val names = (resp.data ?: emptyList()).map { (it.title ?: it.type ?: "Activity").toString() }
                        if (names.isNotEmpty()) {
                            _state.value = _state.value.copy(recentActivities = names)
                        }
                    }
            } catch (_: Exception) {}
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            try {
                authRepository.logout()
            } catch (_: Exception) {}
            onDone()
        }
    }
}


