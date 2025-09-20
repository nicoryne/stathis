package citu.edu.stathis.mobile.features.auth.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// Import ClientResponse if you need to explicitly type it for clarity
import citu.edu.stathis.mobile.core.data.models.ClientResponse
import citu.edu.stathis.mobile.features.auth.data.enums.UserRoles // For passing UserRole
import citu.edu.stathis.mobile.features.auth.domain.usecase.RegisterUseCase // Renamed from SignUpUseCase
// SocialSignInUseCase import removed
import citu.edu.stathis.mobile.features.auth.ui.components.PasswordStrength
import citu.edu.stathis.mobile.features.auth.ui.utils.PasswordValidator.calculatePasswordStrength
import citu.edu.stathis.mobile.features.auth.ui.utils.PasswordValidator.doPasswordsMatch
import citu.edu.stathis.mobile.features.auth.ui.utils.PasswordValidator.isValidPassword
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<RegisterEvent>()
    val events: SharedFlow<RegisterEvent> = _events.asSharedFlow()


    fun onEvent(event: RegisterUiEvent) {
        when (event) {
            is RegisterUiEvent.FirstNameChanged -> {
                _state.update { it.copy(firstName = event.firstName) }
            }
            is RegisterUiEvent.LastNameChanged -> {
                _state.update { it.copy(lastName = event.lastName) }
            }
            is RegisterUiEvent.EmailChanged -> {
                _state.update { it.copy(email = event.email) }
            }
            is RegisterUiEvent.PasswordChanged -> {
                val password = event.password
                val strength = calculatePasswordStrength(password)
                _state.update {
                    it.copy(
                        password = password,
                        passwordStrength = strength,
                        passwordsMatch = doPasswordsMatch(password, it.confirmPassword)
                    )
                }
            }
            is RegisterUiEvent.ConfirmPasswordChanged -> {
                val confirmPassword = event.confirmPassword
                _state.update {
                    it.copy(
                        confirmPassword = confirmPassword,
                        passwordsMatch = doPasswordsMatch(it.password, confirmPassword)
                    )
                }
            }
            is RegisterUiEvent.TogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            is RegisterUiEvent.ToggleConfirmPasswordVisibility -> {
                _state.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
            }
            is RegisterUiEvent.TogglePasswordRequirements -> {
                _state.update { it.copy(showPasswordRequirements = !it.showPasswordRequirements) }
            }
            is RegisterUiEvent.Register -> {
                register()
            }
            is RegisterUiEvent.NavigateToLogin -> {
                viewModelScope.launch {
                    _events.emit(RegisterEvent.NavigateToLogin)
                }
            }
            is RegisterUiEvent.ResendVerificationEmail -> {
                resendVerificationEmail()
            }
        }
    }

    private fun register() {
        val currentState = state.value

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val result: ClientResponse<Unit> = registerUseCase(
                firstName = currentState.firstName,
                lastName = currentState.lastName,
                email = currentState.email,
                password = currentState.password,
                confirmPassword = currentState.confirmPassword,
            )

            _state.update { it.copy(isLoading = false) }

            if (result.success) {
                _events.emit(RegisterEvent.RegistrationSuccess(currentState.email))
            } else {
                _events.emit(RegisterEvent.ShowError(result.message))
            }
        }
    }

    private fun resendVerificationEmail() {
        viewModelScope.launch {
            _state.update { it.copy(isResendingEmail = true) }
            val result = registerUseCase.resendVerificationEmail(state.value.email)

            if (result.success) {
                // Optionally show a success message for resend, or rely on dialog behavior
                // For example: _events.emit(RegisterEvent.ShowMessage("Verification email resent."))
            } else {
                _events.emit(RegisterEvent.ShowError(result.message))
            }
            _state.update { it.copy(isResendingEmail = false) }
        }
    }
}

data class RegisterState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val showPasswordRequirements: Boolean = false,
    val passwordStrength: PasswordStrength = PasswordStrength.EMPTY,
    val passwordsMatch: Boolean = true,
    val isLoading: Boolean = false,
    val isResendingEmail: Boolean = false
)

sealed class RegisterUiEvent {
    data class FirstNameChanged(val firstName: String) : RegisterUiEvent()
    data class LastNameChanged(val lastName: String) : RegisterUiEvent()
    data class EmailChanged(val email: String) : RegisterUiEvent()
    data class PasswordChanged(val password: String) : RegisterUiEvent()
    data class ConfirmPasswordChanged(val confirmPassword: String) : RegisterUiEvent()
    data object TogglePasswordVisibility : RegisterUiEvent()
    data object ToggleConfirmPasswordVisibility : RegisterUiEvent()
    data object TogglePasswordRequirements : RegisterUiEvent()
    data object Register : RegisterUiEvent()
    data object NavigateToLogin : RegisterUiEvent()
    data object ResendVerificationEmail : RegisterUiEvent()
}

sealed class RegisterEvent {
    data object NavigateToHome : RegisterEvent()
    data object NavigateToLogin : RegisterEvent()
    data class ShowError(val message: String) : RegisterEvent()
    data class RegistrationSuccess(val email: String) : RegisterEvent()
}