package citu.edu.stathis.mobile.features.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.auth.data.models.UserResponseDTO
import citu.edu.stathis.mobile.features.profile.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val success: Boolean = false
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    fun updateUser(firstName: String, lastName: String, birthdate: String?, profilePictureUrl: String?) {
        _state.value = UiState(isLoading = true)
        viewModelScope.launch {
            val resp: ClientResponse<UserResponseDTO> = profileRepository.updateUserProfile(
                firstName = firstName,
                lastName = lastName,
                birthdate = birthdate,
                profilePictureUrl = profilePictureUrl
            )
            _state.value = if (resp.success) UiState(success = true) else UiState(errorMessage = resp.message)
        }
    }

    fun updateStudent(school: String?, course: String?, yearLevel: Int?) {
        _state.value = UiState(isLoading = true)
        viewModelScope.launch {
            val resp: ClientResponse<UserResponseDTO> = profileRepository.updateStudentProfile(
                school = school,
                course = course,
                yearLevel = yearLevel
            )
            _state.value = if (resp.success) UiState(success = true) else UiState(errorMessage = resp.message)
        }
    }
}



