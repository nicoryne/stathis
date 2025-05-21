package citu.edu.stathis.mobile.features.profile.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import citu.edu.stathis.mobile.features.profile.domain.model.UserProfile
import citu.edu.stathis.mobile.features.profile.domain.repository.IUserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userProfileRepository: IUserProfileRepository
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
            userProfileRepository.getUserProfile().collectLatest { profile ->
                _uiState.value = if (profile != null) {
                    ProfileUiState.Success(profile)
                } else {
                    ProfileUiState.Error("Failed to load profile")
                }
            }
        }
    }

    fun updateUserProfile(
        firstName: String,
        lastName: String,
        schoolAttending: String?,
        yearLevel: Short?,
        courseEnrolled: String?
    ) {
        viewModelScope.launch {
            _editState.value = EditProfileUiState.Loading
            userProfileRepository.updateUserProfile(
                firstName = firstName,
                lastName = lastName,
                schoolAttending = schoolAttending,
                yearLevel = yearLevel,
                courseEnrolled = courseEnrolled
            ).fold(
                onSuccess = {
                    _editState.value = EditProfileUiState.Success
                    loadUserProfile()
                },
                onFailure = {
                    _editState.value = EditProfileUiState.Error("Failed to update your profile. Please try again.")
                }
            )
        }
    }

    fun uploadProfilePicture(imageUri: Uri) {
        viewModelScope.launch {
            _editState.value = EditProfileUiState.Loading

            // Get the current profile picture URL before uploading the new one
            val currentPictureUrl = if (uiState.value is ProfileUiState.Success) {
                (uiState.value as ProfileUiState.Success).profile.pictureUrl
            } else null

            userProfileRepository.uploadProfilePicture(imageUri).fold(
                onSuccess = { newUrl ->
                    // If there was a previous picture, delete it from storage
                    if (!currentPictureUrl.isNullOrEmpty()) {
                        try {
                            userProfileRepository.deleteOldProfilePicture(currentPictureUrl)
                        } catch (e: Exception) {
                            // Log error but continue since the new picture was uploaded successfully
                        }
                    }

                    _editState.value = EditProfileUiState.Success
                    loadUserProfile()
                },
                onFailure = {
                    _editState.value = EditProfileUiState.Error("Failed to upload profile picture. Please try again.")
                }
            )
        }
    }

    fun deleteProfilePicture() {
        viewModelScope.launch {
            _editState.value = EditProfileUiState.Loading
            userProfileRepository.deleteProfilePicture().fold(
                onSuccess = {
                    _editState.value = EditProfileUiState.Success
                    loadUserProfile()
                },
                onFailure = {
                    _editState.value = EditProfileUiState.Error("Failed to delete profile picture. Please try again.")
                }
            )
        }
    }

    suspend fun signOut() {
        userProfileRepository.signOut()
    }

    fun resetEditState() {
        _editState.value = EditProfileUiState.Idle
    }
}

sealed class ProfileUiState {
    data object Loading : ProfileUiState()
    data class Success(val profile: UserProfile) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

sealed class EditProfileUiState {
    data object Idle : EditProfileUiState()
    data object Loading : EditProfileUiState()
    data object Success : EditProfileUiState()
    data class Error(val message: String) : EditProfileUiState()
}