package citu.edu.stathis.mobile.features.auth.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import citu.edu.stathis.mobile.features.auth.domain.model.AuthResult
import citu.edu.stathis.mobile.features.auth.domain.usecase.SignUpUseCase
import citu.edu.stathis.mobile.features.auth.domain.usecase.SocialSignInUseCase
import citu.edu.stathis.mobile.features.auth.ui.components.PasswordStrength
import citu.edu.stathis.mobile.features.auth.ui.utils.PasswordValidator.calculatePasswordStrength
import citu.edu.stathis.mobile.features.auth.ui.utils.PasswordValidator.doPasswordsMatch
import citu.edu.stathis.mobile.features.auth.ui.utils.PasswordValidator.isValidPassword
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
    private val signUpUseCase: SignUpUseCase,
    private val socialSignInUseCase: SocialSignInUseCase
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
                viewModelScope.launch {
                    _events.emit(RegisterEvent.RegistrationSuccess(state.value.email))
                }
            }
            is RegisterUiEvent.GoogleSignIn -> {
                googleSignIn()
            }
            is RegisterUiEvent.MicrosoftSignIn -> {
                microsoftSignIn()
            }
            is RegisterUiEvent.NavigateToLogin -> {
                viewModelScope.launch {
                    _events.emit(RegisterEvent.NavigateToLogin)
                }
            }

            is RegisterUiEvent.ResendVerificationEmail -> {
                viewModelScope.launch {
                    try {
                        _state.update { it.copy(isResendingEmail = true) }

                        // Call your authentication service to resend the verification email
                        signUpUseCase.resendVerificationEmail(state.value.email)

                        // Simulate a delay for demonstration purposes
                        delay(1500)

                        _state.update { it.copy(isResendingEmail = false) }
                    } catch (e: Exception) {
                        _state.update { it.copy(isResendingEmail = false) }
                        _events.emit(RegisterEvent.ShowError(e.message ?: "Failed to resend verification email"))
                    }
                }
            }
        }
    }

    private fun register() {
        val state = state.value

        if (state.firstName.isBlank() || state.lastName.isBlank() || state.email.isBlank()) {
            viewModelScope.launch {
                _events.emit(RegisterEvent.ShowError("Please fill in all fields"))
            }
            return
        }

        if (!isValidPassword(state.password)) {
            viewModelScope.launch {
                _events.emit(RegisterEvent.ShowError("Password does not meet requirements"))
            }
            return
        }

        if (!state.passwordsMatch) {
            viewModelScope.launch {
                _events.emit(RegisterEvent.ShowError("Passwords do not match"))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val result = signUpUseCase(
                firstName = state.firstName,
                lastName = state.lastName,
                email = state.email,
                password = state.password
            )

            _state.update { it.copy(isLoading = false) }

            when (result) {
                is AuthResult.Success -> {
                    _events.emit(RegisterEvent.NavigateToHome)
                }
                is AuthResult.Error -> {
                    _events.emit(RegisterEvent.ShowError(result.message))
                }

            }
        }
    }

    private fun googleSignIn() {
        viewModelScope.launch {
            _state.update { it.copy(isGoogleLoading = true) }

            val result = socialSignInUseCase.signInWithGoogle()

            _state.update { it.copy(isGoogleLoading = false) }

            when (result) {
                is AuthResult.Success -> {
                    _events.emit(RegisterEvent.NavigateToHome)
                }
                is AuthResult.Error -> {
                    _events.emit(RegisterEvent.ShowError(result.message))
                }
            }
        }
    }

    private fun microsoftSignIn() {
        viewModelScope.launch {
            _state.update { it.copy(isMicrosoftLoading = true) }

            val result = socialSignInUseCase.signInWithMicrosoft()

            _state.update { it.copy(isMicrosoftLoading = false) }

            when (result) {
                is AuthResult.Success -> {
                    _events.emit(RegisterEvent.NavigateToHome)
                }
                is AuthResult.Error -> {
                    _events.emit(RegisterEvent.ShowError(result.message))
                }
            }
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
    val isGoogleLoading: Boolean = false,
    val isMicrosoftLoading: Boolean = false,
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
    data object GoogleSignIn : RegisterUiEvent()
    data object MicrosoftSignIn : RegisterUiEvent()
    data object NavigateToLogin : RegisterUiEvent()
    data object ResendVerificationEmail : RegisterUiEvent()
}

sealed class RegisterEvent {
    data object NavigateToHome : RegisterEvent()
    data object NavigateToLogin : RegisterEvent()
    data class ShowError(val message: String) : RegisterEvent()
    data class RegistrationSuccess(val email: String) : RegisterEvent()
}