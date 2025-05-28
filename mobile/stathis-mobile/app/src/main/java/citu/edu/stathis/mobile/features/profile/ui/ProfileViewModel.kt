package citu.edu.stathis.mobile.features.profile.ui

import android.net.Uri // Keep for now, but usage will change
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import citu.edu.stathis.mobile.features.auth.data.models.UserResponseDTO // Changed
import citu.edu.stathis.mobile.features.auth.data.repository.AuthRepository // Added
import citu.edu.stathis.mobile.features.profile.data.repository.ProfileRepository // Changed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _editState = MutableStateFlow<EditProfileUiState>(EditProfileUiState.Idle)
    val editState: StateFlow<EditProfileUiState> = _editState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            val response = profileRepository.getUserProfile()
            if (response.success && response.data != null) {
                _uiState.value = ProfileUiState.Success(response.data)
            } else {
                _uiState.value = ProfileUiState.Error(response.message ?: "Failed to load profile")
            }
        }
    }

    fun updateFullProfile(
        firstName: String,
        lastName: String,
        birthdate: String?,
        profilePictureUrl: String?,
        school: String?,
        course: String?,
        yearLevel: Int?
    ) {
        viewModelScope.launch {
            _editState.value = EditProfileUiState.Loading

            val generalProfileResponse = profileRepository.updateUserProfile(
                firstName = firstName,
                lastName = lastName,
                birthdate = birthdate,
                profilePictureUrl = profilePictureUrl
            )

            if (!generalProfileResponse.success) {
                _editState.value = EditProfileUiState.Error(generalProfileResponse.message ?: "Failed to update general profile.")
                return@launch
            }

            if (school != null || course != null || yearLevel != null) {
                val studentProfileResponse = profileRepository.updateStudentProfile(
                    school = school,
                    course = course,
                    yearLevel = yearLevel
                )

                if (!studentProfileResponse.success) {
                    _editState.value = EditProfileUiState.Error(studentProfileResponse.message ?: "Failed to update student specific profile.")
                    return@launch
                }
            }

            _editState.value = EditProfileUiState.Success
            loadUserProfile()
        }
    }

    fun updateProfilePictureUrl(newUrl: String) {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState is ProfileUiState.Success) {
                val currentProfile = currentState.profile
                updateFullProfile(
                    firstName = currentProfile.firstName,
                    lastName = currentProfile.lastName,
                    birthdate = currentProfile.birthdate,
                    profilePictureUrl = newUrl,
                    school = currentProfile.school,
                    course = currentProfile.course,
                    yearLevel = currentProfile.yearLevel?.toIntOrNull()
                )
            } else {
                _editState.value = EditProfileUiState.Error("User profile not loaded. Cannot update picture.")
            }
        }
    }

    fun removeProfilePicture() {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState is ProfileUiState.Success) {
                val currentProfile = currentState.profile
                updateFullProfile(
                    firstName = currentProfile.firstName,
                    lastName = currentProfile.lastName,
                    birthdate = currentProfile.birthdate,
                    profilePictureUrl = null, // Set to null to remove
                    school = currentProfile.school,
                    course = currentProfile.course,
                    yearLevel = currentProfile.yearLevel?.toIntOrNull()
                )
            } else {
                _editState.value = EditProfileUiState.Error("User profile not loaded. Cannot remove picture.")
            }
        }
    }


    fun signOut(onSignedOut: () -> Unit) {
        viewModelScope.launch {
            val response = authRepository.logout()
            if (response.success) {
                onSignedOut()
            } else {
                _editState.value = EditProfileUiState.Error(response.message ?: "Logout failed.")
            }
        }
    }

    fun resetEditState() {
        _editState.value = EditProfileUiState.Idle
    }
}

sealed class ProfileUiState {
    data object Loading : ProfileUiState()
    data class Success(val profile: UserResponseDTO) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

sealed class EditProfileUiState {
    data object Idle : EditProfileUiState()
    data object Loading : EditProfileUiState()
    data object Success : EditProfileUiState()
    data class Error(val message: String) : EditProfileUiState()
}