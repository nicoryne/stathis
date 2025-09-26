package citu.edu.stathis.mobile.features.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.auth.data.enums.UserRoles
import citu.edu.stathis.mobile.features.auth.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CreateProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    data class UiState(
        val isSubmitting: Boolean = false,
        val errorMessage: String? = null,
        val success: Boolean = false
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    fun registerStudent(email: String, password: String, firstName: String, lastName: String) {
        if (_state.value.isSubmitting) return
        // Basic client-side validation to avoid unnecessary requests
        val error = validate(firstName, lastName, email, password)
        if (error != null) {
            _state.value = _state.value.copy(errorMessage = error)
            return
        }

        _state.value = _state.value.copy(isSubmitting = true, errorMessage = null)
        viewModelScope.launch {
            // 1) Register as STUDENT
            val registerResult: ClientResponse<Unit> = authRepository.register(
                email = email,
                password = password,
                firstName = firstName,
                lastName = lastName,
                userRole = UserRoles.STUDENT
            )

            if (!registerResult.success) {
                _state.value = UiState(isSubmitting = false, errorMessage = registerResult.message ?: "Registration failed.", success = false)
                return@launch
            }

            // 2) Auto-login
            val loginResult = authRepository.login(email, password)
            if (!loginResult.success) {
                _state.value = UiState(isSubmitting = false, errorMessage = loginResult.message ?: "Login failed after registration.", success = false)
                return@launch
            }

            _state.value = UiState(isSubmitting = false, errorMessage = null, success = true)
        }
    }

    private fun validate(firstName: String, lastName: String, email: String, password: String): String? {
        if (firstName.isBlank() || lastName.isBlank()) return "Please provide your name."
        val emailOk = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        if (!emailOk) return "Please enter a valid email."
        if (password.length < 8) return "Password must be at least 8 characters."
        return null
    }
}


