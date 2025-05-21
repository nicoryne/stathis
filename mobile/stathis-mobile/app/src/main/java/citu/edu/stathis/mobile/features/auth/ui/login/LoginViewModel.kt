package citu.edu.stathis.mobile.features.auth.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import citu.edu.stathis.mobile.features.auth.domain.model.AuthResult
import citu.edu.stathis.mobile.features.auth.domain.model.BiometricState
import citu.edu.stathis.mobile.features.auth.domain.usecase.BiometricAuthUseCase
import citu.edu.stathis.mobile.features.auth.domain.usecase.SignInUseCase
import citu.edu.stathis.mobile.features.auth.domain.usecase.SocialSignInUseCase
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
class LoginViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val socialSignInUseCase: SocialSignInUseCase,
    private val biometricAuthUseCase: BiometricAuthUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    private val _biometricState = MutableStateFlow<BiometricState>(BiometricState.NotChecked)
    val biometricState: StateFlow<BiometricState> = _biometricState.asStateFlow()

    init {
        checkBiometricAvailability()
    }

    private fun checkBiometricAvailability() {
        viewModelScope.launch {
            _biometricState.value = biometricAuthUseCase.checkBiometricAvailability()
        }
    }

    fun onEvent(event: LoginUiEvent) {
        when (event) {
            is LoginUiEvent.EmailChanged -> {
                _state.update { it.copy(email = event.email) }
            }
            is LoginUiEvent.PasswordChanged -> {
                _state.update { it.copy(password = event.password) }
            }
            is LoginUiEvent.TogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            is LoginUiEvent.Login -> {
                login()
            }
            is LoginUiEvent.GoogleSignIn -> {
                googleSignIn()
            }
            is LoginUiEvent.MicrosoftSignIn -> {
                microsoftSignIn()
            }
            is LoginUiEvent.NavigateToRegister -> {
                viewModelScope.launch {
                    _events.emit(LoginEvent.NavigateToRegister)
                }
            }
            is LoginUiEvent.NavigateToForgotPassword -> {
                viewModelScope.launch {
                    _events.emit(LoginEvent.NavigateToForgotPassword)
                }
            }
            is LoginUiEvent.BiometricLogin -> {
                loginWithBiometrics()
            }
        }
    }

    private fun login() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val result = signInUseCase(
                email = state.value.email,
                password = state.value.password
            )

            _state.update { it.copy(isLoading = false) }

            when (result) {
                is AuthResult.Success -> {
                    _events.emit(LoginEvent.NavigateToHome)
                }
                is AuthResult.Error -> {
                    _events.emit(LoginEvent.ShowError(result.message))
                }
            }
        }
    }

    private fun loginWithBiometrics() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val result = biometricAuthUseCase.authenticateWithBiometrics()

            _state.update { it.copy(isLoading = false) }

            when (result) {
                is AuthResult.Success -> {
                    _events.emit(LoginEvent.NavigateToHome)
                    _biometricState.value = BiometricState.NotChecked

                }
                is AuthResult.Error -> {
                    _events.emit(LoginEvent.ShowError(result.message))
                    checkBiometricAvailability()
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
                    _events.emit(LoginEvent.NavigateToHome)
                    _biometricState.value = BiometricState.NotChecked

                }
                is AuthResult.Error -> {
                    _events.emit(LoginEvent.ShowError(result.message))
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
                    _events.emit(LoginEvent.NavigateToHome)
                }
                is AuthResult.Error -> {
                    _events.emit(LoginEvent.ShowError(result.message))
                }
            }
        }
    }

    fun refreshBiometricState() {
        checkBiometricAvailability()
    }
}

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isGoogleLoading: Boolean = false,
    val isMicrosoftLoading: Boolean = false
)

sealed class LoginUiEvent {
    data class EmailChanged(val email: String) : LoginUiEvent()
    data class PasswordChanged(val password: String) : LoginUiEvent()
    data object TogglePasswordVisibility : LoginUiEvent()
    data object Login : LoginUiEvent()
    data object BiometricLogin : LoginUiEvent()
    data object GoogleSignIn : LoginUiEvent()
    data object MicrosoftSignIn : LoginUiEvent()
    data object NavigateToRegister : LoginUiEvent()
    data object NavigateToForgotPassword : LoginUiEvent()
}

sealed class LoginEvent {
    data object NavigateToHome : LoginEvent()
    data object NavigateToRegister : LoginEvent()
    data object NavigateToForgotPassword : LoginEvent()
    data class ShowError(val message: String) : LoginEvent()
}