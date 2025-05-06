package citu.edu.stathis.mobile.features.auth.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import citu.edu.stathis.mobile.features.auth.domain.model.AuthResult
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
    private val socialSignInUseCase: SocialSignInUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

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

    private fun googleSignIn() {
        viewModelScope.launch {
            _state.update { it.copy(isGoogleLoading = true) }

            val result = socialSignInUseCase.signInWithGoogle()

            _state.update { it.copy(isGoogleLoading = false) }

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
    object TogglePasswordVisibility : LoginUiEvent()
    object Login : LoginUiEvent()
    object GoogleSignIn : LoginUiEvent()
    object MicrosoftSignIn : LoginUiEvent()
    object NavigateToRegister : LoginUiEvent()
    object NavigateToForgotPassword : LoginUiEvent()
}

sealed class LoginEvent {
    object NavigateToHome : LoginEvent()
    object NavigateToRegister : LoginEvent()
    object NavigateToForgotPassword : LoginEvent()
    data class ShowError(val message: String) : LoginEvent()
}